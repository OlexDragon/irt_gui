package irt.tools.combobox;

import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
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
import irt.data.IdValueFreq;
import irt.data.Listeners;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.LOFrequenciesPacket;
import irt.data.packet.LOPacket;
import irt.data.packet.PacketImp;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
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
	private 		PacketWork 					packetToSend;
	private final AncestorListener ancestorListener = new AncestorListener() {
		
		@Override
		public void ancestorRemoved(AncestorEvent event) {

			GuiControllerAbstract.getComPortThreadQueue().removePacketListener(LoSelectComboBox.this);
			Optional.ofNullable(scheduleAtFixedRate).filter(sch->!sch.isCancelled()).ifPresent(sch->sch.cancel(true));
		}
		@Override
		public void ancestorAdded(AncestorEvent event) {

			if(service.isShutdown() || Optional.ofNullable(scheduleAtFixedRate).map(f->!f.isCancelled()).orElse(false))
				return;

			scheduleAtFixedRate = service.scheduleAtFixedRate(LoSelectComboBox.this, 1, TimeUnit.SECONDS.toMillis(30), TimeUnit.MILLISECONDS);
			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(LoSelectComboBox.this);
		}
		@Override public void ancestorMoved(AncestorEvent event) { }
	};

	private final ItemListener iListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent itemEvent) {
			if(itemEvent.getStateChange()==ItemEvent.SELECTED){
				final IdValueFreq idValueFreq = (IdValueFreq)model.getSelectedItem();

				LOPacket packetWork;
				if(linkAddr!=0) {
					packetWork = new LOPacket(linkAddr, idValueFreq.getId());
				} else
					//converter
					packetWork = new LOPacket(idValueFreq.getValueFrequency());

				GuiControllerAbstract.getComPortThreadQueue().add(packetWork);
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
		addItemListener(iListener);
	}

	@Override
	public void run() {
		try{

			logger.trace(packetToSend);
			GuiControllerAbstract.getComPortThreadQueue().add(packetToSend);

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
				logger.entry(pl);

				final Payload payload = packet.getPayloads().get(0);

				switch(packet.getHeader().getPacketId()){
				case PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES:
					fillComboBox(payload);
					break;
				case PacketWork.PACKET_ID_CONFIGURATION_LO:
					update(payload);
				}
			});
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

			final ItemListener[] itemListeners = getItemListeners();
			Arrays.stream(itemListeners).forEach(il->removeItemListener(il));

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
			Arrays.stream(itemListeners).forEach(il->addItemListener(il));

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

	private synchronized void update(Payload payload) {
		logger.entry(payload);

		if(linkAddr != 0)
			updateBias(payload);
		else
			updateConverter(payload);
	}

	private void updateConverter(Payload payload) {
		logger.entry(payload);

		final long fr = payload.getLong();

		Optional
		.ofNullable((IdValueFreq)getSelectedItem())
		.map(IdValueFreq::getValueFrequency)
		.map(ValueFrequency::getValue)
		.filter(l->l!=fr)
		.map(l->IntStream.range(0, getItemCount()))
		.orElse(IntStream.empty())
		.mapToObj(this::getItemAt)
		.filter(item->item.getValueFrequency().getValue()==fr)
		.findAny()
		.ifPresent(this::setSelectedItem);
	}

	public void updateBias(final Payload payload) {
		logger.entry(payload);

		final byte loID = payload.getByte();
		final IdValueFreq anObject = new IdValueFreq(loID, null);
		if(!anObject.equals(model.getSelectedItem()))
			setSelectedItem(anObject);
	}

	private synchronized void setSelectedItem(final IdValueFreq itemAt) {
		logger.entry(itemAt);

		removeItemListener(iListener);
		super.setSelectedItem(itemAt);
		addItemListener(iListener);
	}

	@Override
	public void addItemListener(ItemListener aListener) {

		//if does not exists
		if(!Arrays.stream(getItemListeners()).filter(il->il==aListener).findAny().isPresent())
			super.addItemListener(aListener);
	}
}
