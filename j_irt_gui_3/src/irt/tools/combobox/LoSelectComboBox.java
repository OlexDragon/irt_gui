package irt.tools.combobox;

import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.data.IdValueFreq;
import irt.data.Listeners;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketID;
import irt.data.packet.Packets;
import irt.data.packet.configuration.LOFrequenciesPacket;
import irt.data.packet.configuration.LOPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.value.ValueFrequency;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

public class LoSelectComboBox extends JComboBox<IdValueFreq> implements Runnable, PacketListener{
	private static final long serialVersionUID = -419940816764892955L;

	private final Logger logger = LogManager.getLogger();

	private 		ScheduledFuture<?> 			scheduleAtFixedRate;
	private final 	ScheduledExecutorService	service 	= Executors.newScheduledThreadPool(1, new ThreadWorker("LoSelectComboBox"));

	private final 	DefaultComboBoxModel<IdValueFreq> model = new DefaultComboBoxModel<>();
	private 		PacketSuper 					packetToSend;
	private final AncestorListener ancestorListener = new AncestorListener() {

		@Override
		public void ancestorRemoved(AncestorEvent event) {

			GuiControllerAbstract.getComPortThreadQueue().removePacketListener(LoSelectComboBox.this);
			Optional.ofNullable(scheduleAtFixedRate).filter(sch->!sch.isCancelled()).ifPresent(sch->sch.cancel(true));
			Optional.of(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
		}
		@Override
		public void ancestorAdded(AncestorEvent event) {

			if(service.isShutdown() || Optional.ofNullable(scheduleAtFixedRate).filter(f->!f.isDone()).isPresent())
				return;

			scheduleAtFixedRate = service.scheduleAtFixedRate(LoSelectComboBox.this, 1, 30, TimeUnit.SECONDS);
			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(LoSelectComboBox.this);
		}
		@Override public void ancestorMoved(AncestorEvent event) { }
	};

	private final ItemListener iListener = new ItemListener() {

		@Override
		public void itemStateChanged(ItemEvent itemEvent) {
			Optional
			.of(itemEvent.getStateChange())
			.filter(state->state==ItemEvent.SELECTED)
			.ifPresent(state->{
				
				final IdValueFreq idValueFreq = (IdValueFreq)model.getSelectedItem();
				LOPacket packetWork = new LOPacket(linkAddr, idValueFreq);
				GuiControllerAbstract.getComPortThreadQueue().add(packetWork);
			});
		}
	};

	private final Byte linkAddr;

	public LoSelectComboBox(final Byte linkAddr) {

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
	public void onPacketReceived(Packet packet) {

		Optional<Short> oId = Optional

				.ofNullable(packet)
				.map(Packet::getHeader)
				.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
				.map(PacketHeader::getPacketId);

		// Fill JComboBox
		oId
		.filter(PacketID.CONFIGURATION_LO_FREQUENCIES::match)
		.ifPresent(
				id->
				new ThreadWorker(()->{

					final Optional<List<?>> oValue = cast(packet).map(v->(List<?>)v);

					if(!oValue.isPresent()){
						final LoSelectComboBox cb = LoSelectComboBox.this;
						cb.getParent().remove(cb);
						return;
					}

					oValue
					.map(List::stream)
					.orElseGet(()->Stream.empty())
					.map(IdValueFreq.class::cast)
					.forEach(v->{
						ValueFrequency vf = v.getValueFrequency();
						SwingUtilities.invokeLater(()->{
							final boolean present = IntStream.range(0, model.getSize()).mapToObj(index->model.getElementAt(index)).map(e->e.getValueFrequency()).filter(e->e.equals(vf)).findAny().isPresent();
							if(!present){
								removeItemListener(iListener);
								model.addElement(v);
								addItemListener(iListener);
							}
						});

						packetToSend = new LOPacket(linkAddr, null);
						LoSelectComboBox.this.run();
					});
				}, "LoSelectComboBox: Fill JComboBox"));

		// Select JComboBox item
		oId
		.filter(PacketID.CONFIGURATION_LO::match)
		.ifPresent(
				id->
				new ThreadWorker(()->{

					Optional<?> oValue = cast(packet);

					// Converter
					oValue
					.filter(ValueFrequency.class::isInstance)
					.map(ValueFrequency.class::cast)
					.ifPresent(
							vf->
							SwingUtilities
							.invokeLater(
									()->
									IntStream
									.range(0, model.getSize())
									.filter(index->model.getElementAt(index).getValueFrequency().equals(vf))
									.findAny()
									.ifPresent(
											index->{
												removeItemListener(iListener);
												setSelectedIndex(index);
												addItemListener(iListener);
											})));

					// Bias board
					oValue
					.filter(Byte.class::isInstance)
					.map(Byte.class::cast)
					.ifPresent(
							idToSet->
							SwingUtilities.invokeLater(
									()->{
										IntStream.range(0, model.getSize()).mapToObj(model::getElementAt).filter(s->s.getId()==idToSet).findAny()
										.ifPresent(
												o->{
													removeItemListener(iListener);
													model.setSelectedItem(o);
													addItemListener(iListener);
												});
									}));
				}, "LoSelectComboBox: Select JComboBox item"));
	}

	private Optional<?> cast(Packet packet) {
		return Packets.cast(packet).map(PacketSuper::getValue).flatMap(v->(Optional<?>)v);
	}

	@Override
	public void addItemListener(ItemListener aListener) {

		//if does not exists
		if(!Arrays.stream(getItemListeners()).filter(il->il==aListener).findAny().isPresent())
			super.addItemListener(aListener);
	}
}
