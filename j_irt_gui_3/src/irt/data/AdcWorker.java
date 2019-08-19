package irt.data;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketWork.DeviceDebugPacketIds;
import irt.data.packet.denice_debag.DeviceDebugPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.value.Value;

public class AdcWorker {

	protected final Logger logger = LogManager.getLogger();

	private final DeviceDebugPacket packetToSend;
													public DeviceDebugPacket getPacketToSend() {
														return packetToSend;
													}
	private final JLabel label;
	private final DeviceDebugPacketIds deviceDebugPacketIds;
	private final double multiplier;
	private final DecimalFormat format;

	public AdcWorker(JLabel label, byte linkAddr, Value value, DeviceDebugPacketIds packetID, double multiplier, String pattern) {
		packetToSend = new DeviceDebugPacket(linkAddr, value, packetID);

		this.label = label;
		this.deviceDebugPacketIds = packetID;
		this.multiplier = multiplier;
		this.format = new DecimalFormat(pattern);


		label.addMouseListener(new MouseListener() {
	
			@Override
			public void mouseClicked(MouseEvent e) {
				final int clickCount = e.getClickCount();
				if(clickCount==2){
					label.setText("");
				}
			}
			@Override public void mouseReleased(MouseEvent e) { }
			@Override public void mousePressed(MouseEvent e) { }
			@Override public void mouseExited(MouseEvent e) { }
			@Override public void mouseEntered(MouseEvent e) { }
		});
	}

	public void update(final Packet packet) {

		final Optional<PacketHeader> oMatch = Optional
		.ofNullable(packet)
		.map(Packet::getHeader)
		.filter(h->deviceDebugPacketIds.getPacketId().match(h.getPacketId()));

		if(!oMatch.isPresent())
			return;

		logger.traceEntry("{}{}", packet, packetToSend);

		final Thread currentThread = Thread.currentThread();
		new MyThreadFactory(
				()->{

					final Optional<PacketHeader> oNoError = oMatch
					.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
					.filter(h->h.getOption()==PacketImp.ERROR_NO_ERROR);

					if(!oNoError.isPresent()) {
						logger.warn("No response or error: {}");
						return;
					}

					oNoError
					.map(h->packet.getPayloads().stream())
					.orElse(Stream.empty())
					.findAny()
					.ifPresent(pl->{
						final RegisterValue registerValue = pl.getRegisterValue();
						Optional
						.ofNullable(registerValue.getValue())
						.map(Value::getValue)
						.ifPresent(v->{

							final boolean useMultiplier = Double.compare(multiplier, 0)!=0;

							double value = useMultiplier ? multiplier*v/1000 : v;

							String text = useMultiplier ? format.format(value) : Long.toString(Math.round(value));

							final String t = text;

							SwingUtilities.invokeLater(
									()-> {

										if(!label.getText().equals(t)){
											label.setText(t);
											label.setToolTipText(Long.toString(v));
										}
									});
						});
					});

				}, deviceDebugPacketIds.name() + ":" + currentThread.getId());
	}

	@Override
	public String toString() {
		return "AdcWorker [packetToSend=" + packetToSend + ", label=" + label + ", packetId=" + deviceDebugPacketIds
				+ ", multiplier=" + multiplier + ", format=" + format + "]";
	}

	public DeviceDebugPacketIds getDeviceDebugPacketIds() {
		return deviceDebugPacketIds;
	}
}
