package irt.tools.panel.subpanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;
import irt.tools.label.VarticalLabel;

public class RedundancyPanel extends RedundancyPanelDemo implements PacketListener, Runnable{

	private static final long serialVersionUID = -3045298115182952527L;

	protected final Logger logger = LogManager.getLogger();

	private static final ImageIcon ICON_BUC_X = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_X.jpg"));
	private static final ImageIcon ICON_BUC_B = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_B.jpg"));
	private static final ImageIcon ICON_BUC_A = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_A.jpg"));

	private final 	ComPortThreadQueue 			cptq 		= GuiControllerAbstract.getComPortThreadQueue();
	public  final 	ScheduledExecutorService 	services 	= Executors.newScheduledThreadPool(1, new MyThreadFactory());
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

	//*************************************** constructor RedundancyPanel ********************************************
	public RedundancyPanel(final int deviceType, final LinkHeader linkHeader) {

		final byte addr = linkHeader.getAddr();
		redundancyEnablePacket = new RedundancyEnablePacket(addr, null);
		redundancyModePacket = new RedundancyModePacket(addr, null);
		redundancyNamePacket = new RedundancyNamePacket(addr, null);
		redundancyStatusPacket = new RedundancyStatusPacket(addr, null);
		redundancySetOnlinePacket = new RedundancySetOnlinePacket(addr);

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
		cptq.addPacketListener(this);
		if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())
			scheduleAtFixedRate = services.scheduleAtFixedRate(this, 1, 5000, TimeUnit.MILLISECONDS);
	}

	private void stop() {
		cptq.removePacketListener(this);
		if(scheduleAtFixedRate==null)
			scheduleAtFixedRate.cancel(true);
	}

	@Override
	public void packetRecived(Packet packet) {

		final PacketHeader header = packet.getHeader();

		if(header.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE  && header.getOption()==PacketImp.ERROR_NO_ERROR){

			byte b = packet.getPayload(0).getByte();
			if(packet.equals(redundancyEnablePacket)){

				//Set Enable status

				final RedundancyEnable redundancyEnable = RedundancyEnable.values()[b];
				cmbBxRedundancy.setSelectedItem(redundancyEnable);

			}else if(packet.equals(redundancyModePacket)){
			
				// Set redundancy mode

				final RedundancyMode redundancyMode = RedundancyMode.values()[b];
				cmbBxMode.setSelectedItem(redundancyMode);

			}else if(packet.equals(redundancyNamePacket)){

				// Set BUC Name
				final RedundancyName redundancyName = RedundancyName.values()[b];
				cmbBxName.setSelectedItem(redundancyName);
			
			}else if(packet.equals(redundancyStatusPacket)){
			
				// Set Status and background image
	
				final RedundancyStatus redundancyStatus = RedundancyStatus.values()[b];
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
			}
		}

	}

	@Override
	public void run() {
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
