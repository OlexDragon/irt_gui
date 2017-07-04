package irt.tools.panel.subpanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.DumpController;
import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.translation.Translation;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.RedundancyEnablePacket;
import irt.data.packet.RedundancyEnablePacket.RedundancyEnable;
import irt.data.packet.RedundancyModePacket;
import irt.data.packet.RedundancyModePacket.RedundancyMode;
import irt.data.packet.RedundancyNamePacket;
import irt.data.packet.RedundancyNamePacket.RedundancyName;
import irt.data.packet.RedundancySetOnlinePacket;
import irt.data.packet.RedundancyStatusPacket;
import irt.data.packet.RedundancyStatusPacket.RedundancyStatus;
import irt.data.packet.interfaces.LinkedPacket;
import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;
import irt.tools.label.VarticalLabel;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

public class RedundancyPanel extends RedundancyPanelDemo implements PacketListener, Runnable{

	private static final long serialVersionUID = -3045298115182952527L;

	protected final Logger logger = LogManager.getLogger();

	private static final ImageIcon ICON_BUC_X = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_X.jpg"));
	private static final ImageIcon ICON_BUC_B = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_B.jpg"));
	private static final ImageIcon ICON_BUC_A = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_A.jpg"));

	private final 	ComPortThreadQueue 			cptq 		= GuiControllerAbstract.getComPortThreadQueue();
	public  final 	ScheduledExecutorService 	service 	= Executors.newScheduledThreadPool(1, new MyThreadFactory());
	private 		ScheduledFuture<?> 			scheduleAtFixedRate;

	private final	RedundancyEnablePacket		redundancyEnablePacket;
	private final	RedundancyModePacket		redundancyModePacket;
	private final	RedundancyNamePacket		redundancyNamePacket;
	private final	RedundancyStatusPacket		redundancyStatusPacket;
	private final	RedundancySetOnlinePacket	redundancySetOnlinePacket;

	private ItemListener redundancyListener;

	private JComboBox<RedundancyMode> cmbBxMode;
	private JComboBox<RedundancyEnable> cmbBxRedundancy;
	private JComboBox<RedundancyName> cmbBxName;

	private ItemListener modeListener;
	private ItemListener nameListener;

	private ImageLabel lblImage;
	private VarticalLabel lblSetOnline;
	private JLabel lblRedundancy;
	private JLabel lblMode;
	private JLabel lblUnitName;

	private byte unitAddress;

	//*************************************** constructor RedundancyPanel ********************************************
	public RedundancyPanel(final LinkHeader linkHeader) {

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->{
					cptq.removePacketListener(RedundancyPanel.this);
					service.shutdownNow();
				}));

		unitAddress = linkHeader.getAddr();
		redundancyEnablePacket = new RedundancyEnablePacket(unitAddress, null);
		redundancyModePacket = new RedundancyModePacket(unitAddress, null);
		redundancyNamePacket = new RedundancyNamePacket(unitAddress, null);
		redundancyStatusPacket = new RedundancyStatusPacket(unitAddress, null);
		redundancySetOnlinePacket = new RedundancySetOnlinePacket(unitAddress);

		setBackground(SystemColor.inactiveCaption);
		redundancyListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {

				if(e.getStateChange()==ItemEvent.SELECTED) {
					cptq.add(new RedundancyEnablePacket(redundancyEnablePacket.getLinkHeader().getAddr(), (RedundancyEnable) cmbBxRedundancy.getSelectedItem()));
				}
			}
		};
		modeListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED) {
					cptq.add(new RedundancyModePacket(redundancyModePacket.getLinkHeader().getAddr(), (RedundancyMode) cmbBxMode.getSelectedItem()));
				}
			}
		};
		nameListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED) {
					cptq.add(new RedundancyNamePacket(redundancyNamePacket.getLinkHeader().getAddr(), (RedundancyName) cmbBxName.getSelectedItem()));
				}
			}
		};

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				start();
			}

			public void ancestorRemoved(AncestorEvent event) {
				stop();
			}
			public void ancestorMoved(AncestorEvent event) {}
		});
		
		Font font = Translation.getFont().deriveFont(Translation.getValue(Float.class, "redundancy.lable.font.size", 14f));

		lblRedundancy = new JLabel(Translation.getValue(String.class, "redundancy", "RedundancyEnable"));
		lblRedundancy.setFont(font);
		
		lblMode = new JLabel(Translation.getValue(String.class, "redundancy.mode", "Mode"));
		lblMode.setFont(font);
		
		lblUnitName = new JLabel(Translation.getValue(String.class, "redundancy.unit_name", "Unit Name"));
		lblUnitName.setFont(font);
		
		lblImage = new ImageLabel(ICON_BUC_X, null);

		font = font.deriveFont(Translation.getValue(Float.class, "redundancy.combobox.font.size", 12f));

		DefaultComboBoxModel<RedundancyMode> modeModel = new DefaultComboBoxModel<>();
		RedundancyMode[] values = RedundancyMode.values();
		for(RedundancyMode rm:values){
			String name = rm.name();
			rm.setMode(Translation.getValue(String.class, name, name));
			modeModel.addElement(rm);
		}
		cmbBxMode = new JComboBox<>(modeModel);
		cmbBxMode.setFont(font);
		cmbBxMode.addItemListener(modeListener);
		
		DefaultComboBoxModel<RedundancyEnable> redundancyModel = new DefaultComboBoxModel<>();
		RedundancyEnable[] rs = RedundancyEnable.values();
		for(RedundancyEnable r:rs){
			String name = r.name();
			r.setRedundancy(Translation.getValue(String.class, name, name));
			redundancyModel.addElement(r);
		}
		cmbBxRedundancy = new JComboBox<>(redundancyModel);
		cmbBxRedundancy.setFont(font);
		cmbBxRedundancy.addItemListener(redundancyListener);
		
		DefaultComboBoxModel<RedundancyName> nameModel = new DefaultComboBoxModel<>();
		RedundancyName[] ns = RedundancyName.values();
		for(RedundancyName n:ns)
			nameModel.addElement(n);
		cmbBxName = new JComboBox<>(nameModel);
		cmbBxName.setFont(font);
		cmbBxName.addItemListener(nameListener);
		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.BLACK, Color.LIGHT_GRAY, SystemColor.window, SystemColor.activeCaption));

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(lblMode, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblUnitName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblRedundancy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(cmbBxName, 0, 220, Short.MAX_VALUE)
								.addComponent(cmbBxRedundancy, 0, 220, Short.MAX_VALUE)
								.addComponent(cmbBxMode, Alignment.TRAILING, 0, 220, Short.MAX_VALUE))
							.addGap(13))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(panel, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
							.addGap(8)
							.addComponent(lblImage, 0, 276, Short.MAX_VALUE)
							.addContainerGap())))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRedundancy)
						.addComponent(cmbBxRedundancy, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMode, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
						.addComponent(cmbBxMode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblUnitName, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
						.addComponent(cmbBxName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
						.addComponent(lblImage, 0, 194, Short.MAX_VALUE))
					.addContainerGap())
		);

		String text = Translation.getValue(String.class, "SET_ONLINE", "Set Online");
		lblSetOnline = new VarticalLabel(text, false);
		lblSetOnline.setEnabled(false);
		lblSetOnline.setToolTipText(text);
		lblSetOnline.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(SET_ONLINE.equals(lblSetOnline.getText()))
					cptq.add(redundancySetOnlinePacket);
			}
		});
		lblSetOnline.setHorizontalAlignment(SwingConstants.CENTER);
		lblSetOnline.setFont(font);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(lblSetOnline, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addComponent(lblSetOnline, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
		);
		panel.setLayout(gl_panel);
		setLayout(groupLayout);

		cptq.addPacketListener(this);
		if(!service.isShutdown() && (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled()))
			scheduleAtFixedRate = service.scheduleAtFixedRate(this, 0, 5000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void refresh() {
		Font font = Translation.getFont().deriveFont(Translation.getValue(Float.class, "redundancy.lable.font.size", 14f));

		lblRedundancy.setText(Translation.getValue(String.class, "redundancy", "RedundancyEnable"));
		lblRedundancy.setFont(font);

		lblMode.setText(Translation.getValue(String.class, "redundancy.mode", "Mode"));
		lblMode.setFont(font);

		lblUnitName.setText(Translation.getValue(String.class, "redundancy.unit_name", "Unit Name"));
		lblUnitName.setFont(font);

		String text = Translation.getValue(String.class, lblSetOnline.getName(), SET_ONLINE);
		lblSetOnline.setText(text);
		lblSetOnline.setToolTipText(text);
		lblSetOnline.setFont(font);

		font = font.deriveFont(Translation.getValue(Float.class, "redundancy.combobox.font.size", 12f));

		for(int i=0; i<cmbBxMode.getItemCount(); i++){
			RedundancyMode itemAt = cmbBxMode.getItemAt(i);
			String name = itemAt.name();
			itemAt.setMode(Translation.getValue(String.class, name, name));
		}
		cmbBxMode.setFont(font);

		for(int i=0; i<cmbBxRedundancy.getItemCount(); i++){
			RedundancyEnable itemAt = cmbBxRedundancy.getItemAt(i);
			String name = itemAt.name();
			itemAt.setRedundancy(Translation.getValue(String.class, name, name));
		}
		cmbBxRedundancy.setFont(font);
	}

	private void start() {
		run = true;
	}

	private void stop() {
		run = false;
	}

	@Override
	public void onPacketRecived(Packet packet) {

		try{


			final Optional<Packet> o = Optional.ofNullable(packet);

			if(!o.isPresent())
				return;

			byte addr = o.filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0);

			if(addr!=unitAddress)
				return;

			Optional<PacketHeader> sameGroupId = o.filter(LinkedPacket.class::isInstance)//converters do not have a network
													.map(LinkedPacket.class::cast)
													.filter(p->p.getLinkHeader()!=null)
													.filter(p->p.getLinkHeader().getAddr()==unitAddress)
													.map(Packet::getHeader)
													.filter(h->h.getGroupId()==PacketImp.GROUP_ID_CONFIGURATION);

			if(!sameGroupId.isPresent())
				return;

			Optional<PacketHeader> hasResponse = sameGroupId.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE);

			if(!hasResponse.isPresent()){
				logger.warn("Unit is not connected {}", packet);
				return;
			}

			final Optional<PacketHeader> noError = hasResponse.filter(h->h.getOption()==PacketImp.ERROR_NO_ERROR);

			if(!noError.isPresent()){
				logger.warn("Packet has error {}", packet);
				return;
			}

			noError
			.map(h->packet.getPayloads())
			.map(pls->pls.parallelStream())
			.ifPresent(
					stream->{
						stream.forEach(pl->{

							final byte code = pl.getParameterHeader().getCode();
							final byte index = pl.getByte();
							switch(code){
							case PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_ENABLE:

								//Set Enable status

								final RedundancyEnable redundancyEnable = RedundancyEnable.values()[index];
								cmbBxRedundancy.setSelectedItem(redundancyEnable);
								break;

							case PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_MODE:

								// Set redundancy mode

								final RedundancyMode redundancyMode = RedundancyMode.values()[index];
								cmbBxMode.setSelectedItem(redundancyMode);
								break;

							case PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_NAME:

								// Set BUC Name

								final RedundancyName redundancyName = RedundancyName.values()[index];
								cmbBxName.removeItemListener(nameListener);
								cmbBxName.setSelectedItem(redundancyName);
								cmbBxName.addItemListener(nameListener);
								break;

							case PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_STATUS:

								// Set Status and background image
		
								final RedundancyStatus redundancyStatus = RedundancyStatus.values()[index];
								final RedundancyName selectedItem = (RedundancyName) cmbBxName.getSelectedItem();

								if(redundancyStatus==RedundancyStatus.STANDBY){

									lblSetOnline.setEnabled(true);
									lblSetOnline.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

									if(selectedItem==RedundancyName.BUC_B)
										lblImage.setIcon(ICON_BUC_A);
									else
										lblImage.setIcon(ICON_BUC_B);

								}else if(redundancyStatus==RedundancyStatus.ONLINE){

									lblSetOnline.setEnabled(false);
									lblSetOnline.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

									if(selectedItem==RedundancyName.BUC_A)
										lblImage.setIcon(ICON_BUC_A);
									else
										lblImage.setIcon(ICON_BUC_B);

								}else{

									lblSetOnline.setEnabled(false);
									lblImage.setIcon(ICON_BUC_X);
								}
								break;
							}
						});
					});
		}catch (Exception e) {
			logger.catching(e);
		}
	}

	private long count;
	private boolean run;

	@Override
	public void run() {
		if(run || --count>0){
			count = DumpController.DUMP_TIME;
			return;
		}

		try{

			cptq.add(redundancyEnablePacket);
			cptq.add(redundancyModePacket);
			cptq.add(redundancyNamePacket);
			cptq.add(redundancyStatusPacket);

		}catch(Exception ex){
			logger.catching(ex);
		}
	}
}
