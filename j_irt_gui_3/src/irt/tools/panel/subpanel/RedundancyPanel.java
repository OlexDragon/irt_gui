package irt.tools.panel.subpanel;

import irt.controller.DefaultController;
import irt.controller.GuiController;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.interfaces.Refresh;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.serial_port.value.seter.Setter;
import irt.controller.translation.Translation;
import irt.data.PacketWork;
import irt.data.StringData;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;
import irt.tools.label.VarticalLabel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class RedundancyPanel extends JPanel implements Refresh{
	private static final long serialVersionUID = -3045298115182952527L;

	protected final Logger logger = (Logger) LogManager.getLogger();
//	private static final String UNKNOWN = "'Unknown',";
	private static final String STANDBY = "'Standby',";
	private static final String ONLINE = "'Online',";
	private static final String SET_ONLINE = "Set Online";

	public enum REDUNDANCY{
		DISABLE("Desable"),
		ENABLE("Enable");
		
		private String redundancy;

		private REDUNDANCY(String redundancy){
			this.redundancy = redundancy;
		}

		@Override
		public String toString() {
			return redundancy;
		}

		public void setRedundancy(String redundancy) {
			this.redundancy = redundancy;
		}
	}

	public enum REDUNDANCY_MODE {
		COLD_STANDBY("Cold Standby"),
		HOT_STANDBY("Hot Standby");

		private String mode;

		private REDUNDANCY_MODE(String mode){
			this.mode = mode;
		}
		@Override
		public String toString() {
			return mode;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
	}

	public enum REDUNDANCY_NAME {
		NO_NAME(null),
		BUC_A("BUC A"),
		BUC_B("BUC B");

		private String name;

		private REDUNDANCY_NAME(String name){
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	private ItemListener redundancyListener;

	private List<ControllerAbstract> controllers = new ArrayList<>();

	private REDUNDANCY enable;
	private REDUNDANCY_MODE mode;
	private REDUNDANCY_NAME name;
	private StringData status;

	private JComboBox<REDUNDANCY_MODE> cmbBxMode;
	private JComboBox<REDUNDANCY> cmbBxRedundancy;
	private JComboBox<REDUNDANCY_NAME> cmbBxName;

	private ItemListener modeListener;
	private ItemListener nameListener;

	private ImageLabel lblImage;

	private VarticalLabel lblSetOnline;

	private JLabel lblRedundancy;

	private JLabel lblMode;

	private JLabel lblUnitName;

	public RedundancyPanel(final LinkHeader linkHeader) {
		setBackground(SystemColor.inactiveCaption);
		redundancyListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {

				if(e.getStateChange()==ItemEvent.SELECTED) {
					enable = (REDUNDANCY) cmbBxRedundancy.getSelectedItem();
					Setter packetWork = new Setter(linkHeader,
							Packet.IRT_SLCP_PACKET_ID_CONFIGURATION,
							Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_ENABLE,
							PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_ENABLE);
					try {
						packetWork.preparePacketToSend((byte)enable.ordinal());
					} catch (Exception ex) {
						logger.catching(ex);
					}
					GuiController.getComPortThreadQueue().add(packetWork);
				}
			}
		};
		modeListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED) {
					mode = (REDUNDANCY_MODE) cmbBxMode.getSelectedItem();
					Setter packetWork = new Setter(linkHeader,
							Packet.IRT_SLCP_PACKET_ID_CONFIGURATION,
							Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_MODE,
							PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_MODE);
					try {
						packetWork.preparePacketToSend((byte)mode.ordinal());
					} catch (Exception ex) {
						logger.catching(ex);
					}
					GuiController.getComPortThreadQueue().add(packetWork);
				}
			}
		};
		nameListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED) {
					name = (REDUNDANCY_NAME) cmbBxName.getSelectedItem();
					Setter packetWork = new Setter(linkHeader,
							Packet.IRT_SLCP_PACKET_ID_CONFIGURATION,
							Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_NAME,
							PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME);
					try {
						packetWork.preparePacketToSend((byte)name.ordinal());
					} catch (Exception ex) {
						logger.catching(ex);
					}
					GuiController.getComPortThreadQueue().add(packetWork);
				}
			}
		};

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				logger.entry(event);
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						runController("Redundancy Enable",
								new Getter(linkHeader,
										Packet.IRT_SLCP_PACKET_ID_CONFIGURATION,
										Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_ENABLE,
										PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_ENABLE) {
											@Override
											public boolean set(Packet packet) {
												if(packet!=null && packet.getHeader().getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_ENABLE)
													new GetterWorker(packet);
												return true;
											}
						});
						runController("Redundancy Mode",
								new Getter(linkHeader,
										Packet.IRT_SLCP_PACKET_ID_CONFIGURATION,
										Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_MODE,
										PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_MODE){
											@Override
											public boolean set(Packet packet) {
												if(packet!=null
														&& packet.getHeader().getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_MODE)
													new GetterWorker(packet);
												return true;
											}
						});
						runController("Redundancy Name",
								new Getter(linkHeader,
										Packet.IRT_SLCP_PACKET_ID_CONFIGURATION,
										Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_NAME,
										PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME){
											@Override
											public boolean set(Packet packet) {
												if(packet!=null
														&& packet.getHeader().getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME)
													new GetterWorker(packet);
												return true;
											}
						});
						runController("Redundancy Status",
								new Getter(linkHeader,
										Packet.IRT_SLCP_PACKET_ID_CONFIGURATION,
										Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_STAT,
										PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_STAT){
											@Override
											public boolean set(Packet packet) {
												if(packet!=null
														&& packet.getHeader().getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_STAT)
													new GetterWorker(packet);
												return true;
											}
						});
						return null;
					}
				}.execute();
				logger.exit();
			}

			public void ancestorRemoved(AncestorEvent event) {
				new SwingWorker<Void, Void>(){
					@Override
					protected Void doInBackground() throws Exception {
						for(ControllerAbstract c:controllers)
							c.setRun(false);
						controllers.clear();
						return null;
					}	
				}.execute();
			}
			public void ancestorMoved(AncestorEvent event) {}

			private void runController(String controllerName, Getter packetWork) {
				DefaultController defaultController = new DefaultController(controllerName, packetWork, Style.CHECK_ALWAYS);
				defaultController.setWaitTime(10000);
				controllers.add(defaultController);

				Thread t = new Thread(defaultController);
				int priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.setDaemon(true);
				t.start();
			}
		});
		
		Font font = Translation.getFont().deriveFont(Translation.getValue(Float.class, "redundancy.lable.font.size", 14f));

		lblRedundancy = new JLabel(Translation.getValue(String.class, "redundancy", "Redundancy"));
		lblRedundancy.setFont(font);
		
		lblMode = new JLabel(Translation.getValue(String.class, "redundancy.mode", "Mode"));
		lblMode.setFont(font);
		
		lblUnitName = new JLabel(Translation.getValue(String.class, "redundancy.unit_name", "Unit Name"));
		lblUnitName.setFont(font);
		
		lblImage = new ImageLabel(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_X.jpg")), null);

		font = font.deriveFont(Translation.getValue(Float.class, "redundancy.combobox.font.size", 12f));

		DefaultComboBoxModel<REDUNDANCY_MODE> modeModel = new DefaultComboBoxModel<>();
		REDUNDANCY_MODE[] values = REDUNDANCY_MODE.values();
		for(REDUNDANCY_MODE rm:values){
			String name = rm.name();
			rm.setMode(Translation.getValue(String.class, name, name));
			modeModel.addElement(rm);
		}
		cmbBxMode = new JComboBox<>(modeModel);
		cmbBxMode.setFont(font);
		cmbBxMode.addItemListener(modeListener);
		
		DefaultComboBoxModel<REDUNDANCY> redundancyModel = new DefaultComboBoxModel<>();
		REDUNDANCY[] rs = REDUNDANCY.values();
		for(REDUNDANCY r:rs){
			String name = r.name();
			r.setRedundancy(Translation.getValue(String.class, name, name));
			redundancyModel.addElement(r);
		}
		cmbBxRedundancy = new JComboBox<>(redundancyModel);
		cmbBxRedundancy.setFont(font);
		cmbBxRedundancy.addItemListener(redundancyListener);
		
		DefaultComboBoxModel<REDUNDANCY_NAME> nameModel = new DefaultComboBoxModel<>();
		REDUNDANCY_NAME[] ns = REDUNDANCY_NAME.values();
		for(REDUNDANCY_NAME n:ns)
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
		lblSetOnline.setToolTipText(text);
		lblSetOnline.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(SET_ONLINE.equals(lblSetOnline.getText())){
					try{
						logger.debug("Click");
						Setter packetWork = new Setter(
								linkHeader,
								Packet.IRT_SLCP_PACKET_TYPE_COMMAND,
								Packet.IRT_SLCP_PACKET_ID_CONFIGURATION,
								Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_SET_ONLINE,
								PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_SET_ONLINE);
						GuiController.getComPortThreadQueue().add(packetWork);
						logger.debug(packetWork);
					}catch(Exception ex){
						logger.catching(ex);
					}
				}
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

	//*******************************************************************************
	private class GetterWorker extends SwingWorker<Void, Void>{

		private Packet packet;

		public GetterWorker(Packet packet) {
			Thread.currentThread().setName("Packet ID="+packet.getHeader().getPacketId());
			logger.trace(packet);
			this.packet = packet;
			execute();
		}

		@Override
		protected Void doInBackground() throws Exception {
			Thread.currentThread().setName(packet.getHeader().getPacketIdStr());
			try {
				switch (packet.getHeader().getPacketId()) {
				case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_ENABLE:
					setRedundancyEnable();
					break;
				case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_MODE:
					setRedundancyMode();
					break;
				case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME:
					setRedundancyName();
					break;
				case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_STAT:
					setRedundancyStatus();
					break;
				}
			} catch (Exception ex) {
				logger.catching(ex);
			}
			return null;
		}

		private void setRedundancyStatus() throws IOException {
			StringData s = packet.getPayload(0).getStringData();
			if(!s.equals(status)){
				logger.trace("old={}, new={}", status, s);
				status = s;
				Properties parseString = parseString(s);
				boolean online = parseString!=null && parseString.getProperty("ustatus").equals(ONLINE);
				setEnable(online);
				setOnlineText(parseString!=null && parseString.getProperty("ustatus").equals(STANDBY), online);
				setImage();
			}
		}

		private void setRedundancyName() throws IOException {
			REDUNDANCY_NAME n = REDUNDANCY_NAME.values()[packet.getPayload(0).getByte()];
			if(!n.equals(name)){
				logger.trace("old={}, new={}", name, n);
				cmbBxName.setSelectedItem(name = n);
				setImage();
			}
		}

		private void setRedundancyMode() throws IOException {
			REDUNDANCY_MODE m = REDUNDANCY_MODE.values()[packet.getPayload(0).getByte()];
			if(!m.equals(mode)){
				logger.trace("old={}, new={}", mode, m);
				cmbBxMode.removeItemListener(modeListener);
				cmbBxMode.setSelectedItem(mode = m);
				cmbBxMode.addItemListener(modeListener);
			}
		}

		private void setRedundancyEnable() throws IOException {
			REDUNDANCY e = REDUNDANCY.values()[packet.getPayload(0).getByte()] ;
			if(e!=enable){
				logger.trace("old={}, new={}", enable, e);
				cmbBxRedundancy.removeItemListener(redundancyListener);
				cmbBxRedundancy.setSelectedItem( enable = e);
				cmbBxRedundancy.addItemListener(redundancyListener);
			}
		}

		private void setImage() throws IOException {
			Properties p = parseString(status);
			logger.entry( p, name);
			boolean online = false;
			boolean standby = false;

			if(p!=null){
				String ustatus = p.getProperty("ustatus");
				if(ustatus!=null){
					online =  ustatus.equals(ONLINE);
					standby = ustatus.equals(STANDBY);
				}
			}
			synchronized (logger) {
				if(online && name==REDUNDANCY_NAME.BUC_A || standby && name==REDUNDANCY_NAME.BUC_B)
					lblImage.setIcon(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_A.jpg")));
				else if(standby && name==REDUNDANCY_NAME.BUC_A || online && name==REDUNDANCY_NAME.BUC_B)
					lblImage.setIcon(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_B.jpg")));
				else
					lblImage.setIcon(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_X.jpg")));
			}
		}

		private Properties parseString(StringData stringData) throws IOException {
			Properties p = null;
			if(stringData!=null){
				p = new Properties();
				p.load(new StringReader(stringData.toString()));
			}
			return p;
		}

		private void setEnable(boolean enable) {
			cmbBxRedundancy.setEnabled(enable);
			cmbBxMode.setEnabled(enable);
			cmbBxName.setEnabled(enable);
		}

		private void setOnlineText(boolean standby, boolean online) {
			if(standby){
				String text = Translation.getValue(String.class, "SET_ONLINE", SET_ONLINE);
				lblSetOnline.setText(text);
				lblSetOnline.setName("SET_ONLINE");
				lblSetOnline.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				lblSetOnline.setToolTipText(text);
			}else if(online){
				String text = Translation.getValue(String.class, "ONLINE", ONLINE);
				lblSetOnline.setText(text);
				lblSetOnline.setName("ONLINE");
				lblSetOnline.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				lblSetOnline.setToolTipText(text);
			}else{
				lblSetOnline.setText("");
				lblSetOnline.setName("");
				lblSetOnline.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				lblSetOnline.setToolTipText("");
			}
		}
	}

	@Override
	public void refresh() {
		Font font = Translation.getFont().deriveFont(Translation.getValue(Float.class, "redundancy.lable.font.size", 14f));

		lblRedundancy.setText(Translation.getValue(String.class, "redundancy", "Redundancy"));
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
			REDUNDANCY_MODE itemAt = cmbBxMode.getItemAt(i);
			String name = itemAt.name();
			itemAt.setMode(Translation.getValue(String.class, name, name));
		}
		cmbBxMode.setFont(font);

		for(int i=0; i<cmbBxRedundancy.getItemCount(); i++){
			REDUNDANCY itemAt = cmbBxRedundancy.getItemAt(i);
			String name = itemAt.name();
			itemAt.setRedundancy(Translation.getValue(String.class, name, name));
		}
		cmbBxRedundancy.setFont(font);
	}
}
