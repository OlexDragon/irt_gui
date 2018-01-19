package irt.data;

import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JLabel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import irt.data.packet.DeviceDebugPacket;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.Packet;
import irt.data.value.Value;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.subpanel.BIASsPanel.FixedSizeBuffer;

public class AdcWorker {

	private final DeviceDebugPacket packetToSend;
													public DeviceDebugPacket getPacketToSend() {
														return packetToSend;
													}
	private final JLabel label;
	private final short packetId;
	private final double multiplier;
	private final DecimalFormat format;

	private boolean mousePressed;
	private final List<Double> average = new  FixedSizeBuffer<>(100);

	public AdcWorker(JLabel label, byte linkAddr, RegisterValue registerValue, short packetId, byte parameterId, double multiplier, String pattern) {
		packetToSend = new DeviceDebugPacket(linkAddr, registerValue, packetId, parameterId);

		this.label = label;
		this.packetId = packetId;
		this.multiplier = multiplier;
		this.format = new DecimalFormat(pattern);

		label.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				mousePressed = false;
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				mousePressed = true;
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				final int clickCount = e.getClickCount();
				if(clickCount==2){
					label.setText("");
					synchronized (this) {
						average.clear();
					}
				}
			}
			@Override public void mouseExited(MouseEvent e) { }
			@Override public void mouseEntered(MouseEvent e) { }
		});

		label.addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->clear()));

		label.addAncestorListener(new AncestorListener() {
			public void ancestorRemoved(AncestorEvent arg0) {
				clear();
			}
			public void ancestorAdded(AncestorEvent arg0) { }
			public void ancestorMoved(AncestorEvent arg0) {}
		});

	}

	public void clear(){
		average.clear();
	}

	public void update(final Packet packet) {
		Optional
		.ofNullable(packet)
		.map(Packet::getHeader)
		.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
		.filter(h->h.getOption()==PacketImp.ERROR_NO_ERROR)
		.filter(h->h.getPacketId()==packetId)
		.map(h->packet.getPayloads())
		.map(pls->pls.parallelStream())
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

					synchronized (this) {
						average.add(value);
					}

					if(mousePressed){
						final int size = average.size();
						value = average.parallelStream().map(BigDecimal::new).reduce((a,b)->a.add(b)).map(sum->sum.divide(new BigDecimal(size), BigDecimal.ROUND_HALF_UP)).map(BigDecimal::doubleValue).get();
					}

					String text = useMultiplier ? format.format(value) : Long.toString(Math.round(value));

					if(mousePressed)
						text = "x" + text;

					if(!label.getText().equals(text)){
						label.setText(text);
						label.setToolTipText(Long.toString(v));
					}
				});
			});
	}

	@Override
	public String toString() {
		return "AdcWorker [packetToSend=" + packetToSend + ", label=" + label + ", packetId=" + packetId
				+ ", multiplier=" + multiplier + ", format=" + format + ", mousePressed=" + mousePressed + ", average="
				+ average + "]";
	}
}
