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
														return attempts<3 ? packetToSend : null;	// if more than 3 attempts do nothing.
													}
	private final JLabel label;
	private final DeviceDebugPacketIds deviceDebugPacketIds;
	private final double multiplier;
	private final DecimalFormat format;

	private int attempts;

	public AdcWorker(JLabel label, byte linkAddr, Value value, DeviceDebugPacketIds packetID, double multiplier, String pattern) {
		packetToSend = new DeviceDebugPacket(linkAddr, value, packetID);
		logger.debug("packetToSend: {}", packetToSend);

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
					attempts = 0;
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

		logger.traceEntry("{}", packet);

		final Thread currentThread = Thread.currentThread();
		new ThreadWorker(
				()->{

					final Optional<PacketHeader> oResponse = oMatch
					.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE);
					final Optional<PacketHeader> oNoError = oResponse
					.filter(h->h.getOption()==PacketImp.ERROR_NO_ERROR);

					if(!oNoError.isPresent()) {
						++attempts;
						logger.warn("No response or error: {}", packet);
						label.setText(":");
						oResponse.ifPresent(p->label.setToolTipText(p.getOptionStr()));
						return;
					}
					attempts = 0;
					oNoError
					.map(h->packet.getPayloads().stream())
					.orElse(Stream.empty())
					.findAny()
					.ifPresent(
							pl->{
								final RegisterValue registerValue = pl.getRegisterValue();
								Optional
								.ofNullable(registerValue.getValue())
								.map(Value::getValue)
								.ifPresent(v->{

									final boolean useMultiplier = Double.compare(multiplier, 0)!=0;

									double value = useMultiplier ? multiplier*v/1000 : v;

									final String text = useMultiplier ? format.format(value) : Long.toString(Math.round(value));

									logger.debug("Text to set: '{}'", text);

									SwingUtilities.invokeLater(
											()-> {
												label.setText(text);
												label.setToolTipText(Long.toString(v));
											});
								});
							});

				}, deviceDebugPacketIds.name() + ":" + currentThread.getId());
	}

	public JLabel getLabel() {
		return label;
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
