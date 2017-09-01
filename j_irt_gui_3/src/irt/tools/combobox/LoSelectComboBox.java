package irt.tools.combobox;

import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.IdValueFreq;
import irt.data.Listeners;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.LOFrequenciesPacket;
import irt.data.packet.LOPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.ValueFrequency;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.head.UnitsContainer;

public class LoSelectComboBox extends JComboBox<IdValueFreq> implements Runnable, PacketListener{
	private static final long serialVersionUID = -419940816764892955L;

	private final Logger logger = LogManager.getLogger();

	private 		ScheduledFuture<?> 			scheduleAtFixedRate;
	private final 	ScheduledExecutorService	service 	= Executors.newScheduledThreadPool(1, new MyThreadFactory());

	private final 	DefaultComboBoxModel<IdValueFreq> model = new DefaultComboBoxModel<>();
	private final 	ComPortThreadQueue 			cptq 					= GuiControllerAbstract.getComPortThreadQueue();
	private 		PacketWork 					packetToSend;
	private final AncestorListener ancestorListener = new AncestorListener() {
		
		@Override
		public void ancestorRemoved(AncestorEvent event) {

			cptq.removePacketListener(LoSelectComboBox.this);
			Optional.ofNullable(scheduleAtFixedRate).filter(sch->!sch.isCancelled()).ifPresent(sch->sch.cancel(true));
		}
		@Override
		public void ancestorAdded(AncestorEvent event) {

			if(service.isShutdown())
				return;

			scheduleAtFixedRate = service.scheduleAtFixedRate(LoSelectComboBox.this, 1, 5000, TimeUnit.MILLISECONDS);
			cptq.addPacketListener(LoSelectComboBox.this);
		}
		@Override public void ancestorMoved(AncestorEvent event) { }
	};

	private final ItemListener aListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent itemEvent) {
			if(itemEvent.getStateChange()==ItemEvent.SELECTED){
				final IdValueFreq idValueFreq = (IdValueFreq)model.getSelectedItem();

				if(linkAddr!=0)
					//BUC
					cptq.add(new LOPacket(linkAddr, idValueFreq.getId()));
				else
					//converter
					cptq.add(new LOPacket(idValueFreq.getValueFrequency()));
			}
		}
	};

	private final byte linkAddr;

	public LoSelectComboBox(final byte linkAddr) {

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(
						c->{
							GuiControllerAbstract.getComPortThreadQueue().removePacketListener(LoSelectComboBox.this);
							service.shutdownNow();
						}));

		this.linkAddr = linkAddr;
		setModel(model);

		setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ private static final long serialVersionUID = 1L; @Override public int getWidth() { return 0; }};}});
		addPopupMenuListener(Listeners.popupMenuListener);
		((JLabel)getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		packetToSend = new LOFrequenciesPacket(linkAddr);

		addAncestorListener(ancestorListener);
	}

	@Override
	public void run() {
		try{

			cptq.add(packetToSend);

		}catch(Exception e){
			logger.catching(e);
		}
	}

	@Override
	public void onPacketRecived(Packet packet) {

		try{
			Optional
			.ofNullable(packet)
			.map(Packet::getHeader)
			.filter(h->h.getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES || h.getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_LO)
			.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
			.map(h->packet.getPayloads())
			.map(pls->pls.parallelStream())
			.orElse(Stream.empty())
			.findAny()
			.ifPresent(pl->{

				final Payload payload = packet.getPayloads().get(0);

				switch(packet.getHeader().getPacketId()){
				case PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES:
					fillComboBox(payload);
					break;
				case PacketWork.PACKET_ID_CONFIGURATION_LO:
					update(payload);
				}
			});

			final PacketHeader h = packet.getHeader();
			final short pID = h.getPacketId();
			if(!(pID==PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES || pID==PacketWork.PACKET_ID_CONFIGURATION_LO))
				return;

			if(h.getPacketType()==PacketImp.PACKET_TYPE_REQUEST || h.getOption()!=0){
				logger.warn("Packet is wrong or no connection: {}", packet);
				return;
			}

			logger.trace(packet);
			final Payload payload = packet.getPayloads().get(0);

			switch(pID){
			case PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES:
				fillComboBox(payload);
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_LO:
				update(payload);
			}
		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	public void fillComboBox(Payload payload) {

		final ParameterHeader h = payload.getParameterHeader();
		final int size = h.getSize();
		final LoSelectComboBox cb = LoSelectComboBox.this;

		switch(size){
		case 0:
			cb.getParent().remove(cb);
			break;
		default:

			if(linkAddr != 0)
				setBiasBoardLOs(payload);
			else
				if(!setConverterLOs(payload)){
					new SwingWorker<Void, Void>(){

						@Override
						protected Void doInBackground() throws Exception {
							synchronized (UnitsContainer.class) {
								cb.getParent().remove(cb);									
							}
							return null;
						}
					}.execute();
				}

			addItemListener(aListener);
			packetToSend = new LOPacket(linkAddr);
			LoSelectComboBox.this.run();
		}
	}

	private boolean setConverterLOs(Payload payload) {
		logger.entry(payload);

		final long[] frs = payload.getArrayLong();
		logger.trace("{}", frs);
		Set<ValueFrequency> vf = new TreeSet<>();
		boolean isLO = false;

		for(long fr:frs)
			if(!vf.add(new ValueFrequency(fr, fr, fr)))//if frequency start == frequency stop, so it is LO
				isLO = true;

		logger.trace("{}", vf);
		if(isLO){
			byte id = 0;
			final Iterator<ValueFrequency> iterator = vf.iterator();
			while(iterator.hasNext()){
				IdValueFreq ivf = new IdValueFreq(++id, iterator.next());
				if(model.getIndexOf(ivf)<0)
					addItem(ivf);
			}
		}
		return isLO;
	}

	private void setBiasBoardLOs(Payload payload) {
		logger.entry(payload);

		final byte[] frs = payload.getBuffer();
		for(int i=0; i< frs.length; i+=8){

			byte id = frs[i];
			long v = payload.getLong((byte)++i);
			IdValueFreq ivf = new IdValueFreq(id, new ValueFrequency(v, v, v));
			if(model.getIndexOf(ivf)<0)
				addItem(ivf);
		}
	}

	private void update(Payload payload) {

		if(linkAddr != 0)
			updateBias(payload);
		else
			updateConverter(payload);
	}

	private void updateConverter(Payload payload) {
		logger.entry(payload);

		final long fr = payload.getLong();
		final int itemCount = getItemCount();
		IdValueFreq ivf = (IdValueFreq)getSelectedItem();

		if(ivf==null)
			return;

		final ValueFrequency valueFrequency = ivf.getValueFrequency();
		if(valueFrequency.getValue()!=fr)
			for(int i=0; i<itemCount; i++){
				final IdValueFreq itemAt = getItemAt(i);
				if(itemAt.getValueFrequency().getValue()==fr)
					setSelectedItem(itemAt);
			}
	}

	public void updateBias(final Payload payload) {
		final byte loID = payload.getByte();
		final IdValueFreq anObject = new IdValueFreq(loID, null);
		if(!anObject.equals(model.getSelectedItem()))
			setSelectedItem(anObject);
	}

	private void setSelectedItem(final IdValueFreq itemAt) {
		removeItemListener(aListener);
		super.setSelectedItem(itemAt);
		addItemListener(aListener);
	}
}
