package irt.controller;

import irt.controller.serial_port.ComPort;
import irt.controller.serial_port.ComPortPriorities;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.value.getter.DeviceInfoGetter;
import irt.controller.serial_port.value.getter.ValueChangeListenerClass;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.RundomNumber;
import irt.data.StringData;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;
import irt.irt_gui.IrtGui;
import irt.tools.KeyValue;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.head.Console;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.head.UnitsContainer;
import irt.tools.panel.subpanel.monitor.MonitorPanelConverter;
import irt.tools.panel.subpanel.monitor.MonitorPanelSSPA;
import irt.tools.panel.subpanel.progressBar.ProgressBar;
import irt.tools.panel.wizards.address.AddressWizard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jssc.SerialPortException;
import jssc.SerialPortList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public abstract class GuiControllerAbstract extends Thread {

	protected final Logger logger = (Logger) LogManager.getLogger(getClass().getName());

	public static final int CONNECTION	= 1;
	public static final int ALARM		= 2;
	public static final int MUTE		= 3;

	protected static final String SERIAL_PORT = "serialPort";

	public static enum Protocol{
								ALL,
								CONVERTER,
								LINKED,
								DEMO
							};

	protected static Preferences prefs = Preferences.userRoot().node("IRT Technologies inc.");

	protected static ComPortThreadQueue comPortThreadQueue = new ComPortThreadQueue();

	private IrtGui gui;
	private Console console;
	protected volatile UnitsContainer unitsPanel;
	protected JComboBox<String> serialPortSelection;
	protected JComboBox<KeyValue<String, String>> languageComboBox;
	protected HeadPanel headPanel;

	protected VCLC vclc =  new VCLC();
	protected SoftReleaseChecker softReleaseChecker = getSoftReleaseChecker();
	protected Protocol protocol = getDefaultProtocol();

	private Remover remover = new Remover(11000);

	private static Map<LinkHeader, DeviceInfo>  deviceInfos = new HashMap<>();
	public Map<LinkHeader, Boolean> mutes = new HashMap<>();
	public Map<LinkHeader, Integer> alarms = new HashMap<>();

	private byte address;

	private PacketListener packetListener = new PacketListener() {

		private boolean powerIsOff;

		@Override
		public void packetRecived(Packet packet) {
			logger.trace(packet);

			if (packet != null && packet.getHeader() != null) {
				PacketHeader header = packet.getHeader();

				byte packetType = header.getType();
				if (packetType == Packet.IRT_SLCP_PACKET_TYPE_RESPONSE) {

					DeviceInfo deviceInfo;
					switch (header.getGroupId()) {
					case Packet.IRT_SLCP_PACKET_ID_DEVICE_INFO:
						deviceInfo = new DeviceInfo(packet);

						int type = deviceInfo.getType();
						switch (type) {
						case DeviceInfo.DEVICE_TYPE_L_TO_70:
						case DeviceInfo.DEVICE_TYPE_L_TO_140:
						case DeviceInfo.DEVICE_TYPE_70_TO_L:
						case DeviceInfo.DEVICE_TYPE_140_TO_L:
						case DeviceInfo.DEVICE_TYPE_L_TO_KU:
						case DeviceInfo.DEVICE_TYPE_L_TO_C:
						case DeviceInfo.DEVICE_TYPE_70_TO_KY:
						case DeviceInfo.DEVICE_TYPE_KU_TO_70:
						case DeviceInfo.DEVICE_TYPE_140_TO_KU:
						case DeviceInfo.DEVICE_TYPE_KU_TO_140:
						case DeviceInfo.DEVICE_TYPE_SSPA_CONVERTER:
							protocol = Protocol.CONVERTER;
							break;
						case DeviceInfo.DEVICE_TYPE_BAIS_BOARD:
						case DeviceInfo.DEVICE_TYPE_PICOBUC_L_TO_KU:
						case DeviceInfo.DEVICE_TYPE_PICOBUC_L_TO_C:
						case DeviceInfo.DEVICE_TYPE_SSPA:
							protocol = Protocol.LINKED;
							break;
						default:
							if (type > 0) {
								JOptionPane.showMessageDialog(headPanel, "The Device is not Supported.(device Id=" + type + ")");
								logger.warn("The Device is not Supported.(device Id={})", type);
							} else
								logger.warn("Can not connect");
						}

						new PanelWorker(packet, deviceInfo);
						break;
					case Packet.IRT_SLCP_PACKET_ID_ALARM:
						if(header.getPacketId()==PacketWork.PACKET_ID_ALARMS_SUMMARY && header.getOption()==0) 
							new AlarmWorker(packet);
						break;
					case Packet.IRT_SLCP_PACKET_ID_MEASUREMENT:
						new MeasurementWorker(packet);
						break;
					}
				}

				boolean powerIsOff = remover.controllers.isEmpty();
				if (this.powerIsOff != powerIsOff){

					if(powerIsOff)
						vclc.fireValueChangeListener(new ValueChangeEvent(false, CONNECTION));
					else
						vclc.fireValueChangeListener(new ValueChangeEvent(true, CONNECTION));

					this.powerIsOff = powerIsOff;
				}

			} else
				vclc.fireValueChangeListener(new ValueChangeEvent(false, CONNECTION));
		}
	};

	@SuppressWarnings("unchecked")
	public GuiControllerAbstract(String threadName, IrtGui gui) {
		super(threadName);
		this.gui = gui;

		setRedundancy();
		DevicePanel.DEBUG_PANEL.setGuiController(this);

		comPortThreadQueue.setSerialPort(new ComPort(prefs.get(SERIAL_PORT, "COM1")));
		console = new Console(gui, "Console");

		JPanel contentPane = (JPanel) gui.getContentPane();
		Component[] components = contentPane.getComponents();
		for(Component c:components)
			switch(c.getClass().getSimpleName()){
			case "UnitsContainer":
				unitsPanel = (UnitsContainer) c;
				break;
			case "JComboBox":
				setComboBox((JComboBox<Object>)c);
				break;
			case "HeadPanel":
				logger.trace("set HeadPanel");
				headPanel = (HeadPanel)c;
				Component[] cms = headPanel.getComponents();
				for(Component cm:cms){
					logger.trace("HeadPanel: component class={}, name={}",cm.getClass(), cm.getName());
					String n = cm.getName();
					if(n!=null && n.equals("Language")){
						setComboBox(cm);
						break;
					}
				}
			case "IrtPanel":
				c.addMouseListener(new MouseListener() {

					@Override
					public void mouseReleased(MouseEvent e) {
						int modifiers = e.getModifiers();
						if((modifiers&InputEvent.CTRL_MASK)>0)
							console.setVisible(!console.isVisible());
					}
					@Override public void mousePressed(MouseEvent e) {}
					@Override public void mouseExited(MouseEvent e) {}
					@Override public void mouseEntered(MouseEvent e) {}
					@Override public void mouseClicked(MouseEvent e) {}
				});
			}
		comPortThreadQueue.addPacketListener(packetListener);
	}


	private void setRedundancy() {
		address = (byte) prefs.getInt("address", IrtGui.DEFAULT_ADDRESS);

		String addresses = null;
		String lastModified = null;
		String prefsLastModified = prefs.get("lastModified", null);

		Properties p = IrtPanel.PROPERTIES;
		for (Object s : p.keySet()) {
			String str = (String) s;
			if (str.equalsIgnoreCase("addresses"))
				addresses = p.getProperty(str);
			else if (str.equals("lastModified")) 
				lastModified = p.getProperty(str);
		}

		if (prefsLastModified != null ? !prefsLastModified.equals(lastModified) : lastModified != null) {

			if(lastModified==null)
				prefs.remove("lastModified");
			else
				prefs.put("lastModified", lastModified);
			logger.error("Addresses={}", addresses);

			if (addresses == null)
				prefs.remove(AddressWizard.REDUNDANCY_ADDRESSES);
			else if (addresses.isEmpty())
				prefs.put(AddressWizard.REDUNDANCY_ADDRESSES, AddressWizard.REDUNDANCY_DEFAULT_ADDRESSES);
			else
				prefs.put(AddressWizard.REDUNDANCY_ADDRESSES, addresses);
		}
	}

	public static DeviceInfo getDeviceInfo(LinkHeader linkHeader) {
		return deviceInfos.get(linkHeader);
	}

	protected SoftReleaseChecker getSoftReleaseChecker() {
		return null;
	}

	protected abstract DevicePanel getConverterPanel(DeviceInfo di);
	protected abstract DevicePanel getNewBaisPanel(LinkHeader linkHeader, DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight);

	public static ComPortThreadQueue getComPortThreadQueue() {
		return comPortThreadQueue;
	}

	public static Preferences getPrefs() {
		return prefs;
	}

	@SuppressWarnings("unchecked")
	private void setComboBox(Component c) {
		String name = c.getName();
		if(name!=null)
			if(name.equals("Unit's Serial Port")){
				logger.trace("set serialPortSelection ={}", c);
				serialPortSelection = (JComboBox<String>) c;

				float fontSize = Translation.getValue(Float.class, "serialPortSelection.font.size", 16f);
				serialPortSelection.setFont(Translation.getFont().deriveFont(fontSize));
				DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<String>(SerialPortList.getPortNames());
				defaultComboBoxModel.insertElementAt(Translation.getValue(String.class, "select_serial_port", "Select Serial Port"), 0);
				serialPortSelection.setModel(defaultComboBoxModel);
				Dimension size = serialPortSelection.getSize();
				size.width = Translation.getValue(Integer.class, "serialPortSelection.width", 200);
				serialPortSelection.setSize(size);

				String portName = comPortThreadQueue.getSerialPort().getPortName();
				if(defaultComboBoxModel.getIndexOf(portName)==-1){
					if(defaultComboBoxModel.getSize()>1)
						setSerialPort();
				}else
					serialPortSelection.setSelectedItem(portName);

				serialPortSelection.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent itemEvent) {
						if(itemEvent.getStateChange()==ItemEvent.SELECTED)
							setSerialPort();
					}
				});
			}else if(name.equals("Language")){
				logger.trace("set languageComboBox ={}", c);
				languageComboBox = (JComboBox<KeyValue<String, String>>) c;
				languageComboBox.addItemListener(new ItemListener() {

					@Override
					public void itemStateChanged(ItemEvent itemEvent) {
						if(itemEvent.getStateChange()==ItemEvent.SELECTED){
							logger.trace("languageComboBox.itemStateChanged(ItemEvent {})", itemEvent);

							Translation.setLocale(((KeyValue<String, String>)languageComboBox.getSelectedItem()).getKey());
							Font font = Translation.getFont();

							headPanel.refresh();
							if(unitsPanel!=null)
								unitsPanel.refresh();

							if(font!=null)
								serialPortSelection.setFont(font);

							float fontSize = Translation.getValue(Float.class, "serialPortSelection.font.size", 16f);
							serialPortSelection.setFont(Translation.getFont().deriveFont(fontSize));
							DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<String>(SerialPortList.getPortNames());
							defaultComboBoxModel.insertElementAt(Translation.getValue(String.class, "select_serial_port", "Select Serial Port"), 0);
							serialPortSelection.setModel(defaultComboBoxModel);
							Dimension size = serialPortSelection.getSize();
							size.width = Translation.getValue(Integer.class, "serialPortSelection.width", 200);
							serialPortSelection.setSize(size);
						}
					}
				});
			}
	}


	protected void setSerialPort() {
		setSerialPort(serialPortSelection.getSelectedItem().toString());
	}

	protected void setSerialPort(String serialPortName) {

		if (serialPortName == null || serialPortName.isEmpty())
			try {
				comPortThreadQueue.getSerialPort().closePort();
			} catch (SerialPortException e) {
				logger.catching(e);
			}
		else {
			comPortThreadQueue.setSerialPort(new ComPort(serialPortName));
			prefs.put(SERIAL_PORT, serialPortName);

			reset();
		} 

		synchronized (this) {
			notify();
		}
	}

	private void reset() {

		logger.debug("reset();");
		comPortThreadQueue.clear();
		gui.setConnected(false);
		remover.removeAll();

		protocol = getDefaultProtocol();
		logger.debug("protocol={}", protocol);
	}

	protected boolean removePanel(LinkHeader linkHeader) {
//		comPortThreadQueue.getSerialPort().setRun(false, "Remove Panel");
		boolean removed;
		if (removed = unitsPanel.remove(linkHeader)) {
			logger.warn("removePanel({})", linkHeader);
			unitsPanel.revalidate();
			unitsPanel.getParent().getParent().repaint();
		}
		return removed;
	}

	protected JComboBox<String> getSerialPortSelection() {
		return serialPortSelection;
	}

	public void addChangeListener(ValueChangeListener valueChangeListener){
		vclc.addVlueChangeListener(valueChangeListener);
	}

	public byte getAddress() {
		return logger.exit(address);
	}

	public void setAddress(byte address) {
		this.address = address;
		reset();
	}

	protected boolean isSerialPortSet(){
		boolean set = false;
		if (serialPortSelection != null) {
			Object selectedItem = serialPortSelection.getSelectedItem();
			if (selectedItem != null && comPortThreadQueue.getSerialPort().getPortName().equals(selectedItem.toString())) {
				set = true;
			}
		}
		return set;
	}

	protected void getConverterInfo() {
		if(protocol.equals(Protocol.DEMO) || protocol.equals(Protocol.ALL ) || protocol.equals(Protocol.CONVERTER)){
			logger.debug("protocol = {}", protocol);

			comPortThreadQueue.add(new DeviceInfoGetter() {
				@Override
				public Integer getPriority() {
					return ComPortPriorities.INFO_CONVERTER;
				}
			});
		}
	}

	protected void getUnitsInfo() {
		if (protocol.equals(Protocol.DEMO) || protocol.equals(Protocol.ALL ) || protocol.equals(Protocol.LINKED)) {

			Set<Byte> addresses = getAddresses(address);
			logger.debug("protocol = {}, addresses = {}", protocol, addresses);

			for(Byte b:addresses.toArray(new Byte[addresses.size()])){
				DeviceInfoGetter packetWork = new DeviceInfoGetter(new LinkHeader(b, (byte) 0, (short) 0)) {
					@Override
					public Integer getPriority() {
						return ComPortPriorities.INFO_UNIT;
					}
				};
				logger.trace(packetWork);
				comPortThreadQueue.add(packetWork);
			}
		}
	}

	public static Set<Byte> getAddresses(Byte addtess) {
		String addressesStr = prefs.get(AddressWizard.REDUNDANCY_ADDRESSES, null);
		Set<Byte> addresses = new HashSet<>();
		if(addtess!=null)
			addresses.add(addtess);
		if(addressesStr!=null){
			addressesStr = addressesStr.replaceAll("\\D+", ",");
			String[] split = addressesStr.split(",");

			for(String s:split)
				if(!s.isEmpty()) {
					int parseInt = Integer.parseInt(s);
					addresses.add((byte) parseInt);
				}
		}

		if(addresses.isEmpty())
			addresses.add((byte) IrtGui.DEFAULT_ADDRESS);

		return addresses;
	}

	public LinkHeader getLinkHeader(Packet packet) {
		LinkHeader linkHeader;
		if (packet instanceof LinkedPacket) {
			linkHeader = ((LinkedPacket) packet).getLinkHeader();
		}else
			linkHeader = new LinkHeader((byte)0, (byte)0, (short) 0);
		return linkHeader;
	}

	public void doDump(LinkHeader linkHeader) {
		remover.doDump(linkHeader);
	}

	public void doDump(LinkHeader linkHeader, String string) {
		remover.doDump(linkHeader, string);
	}

	public Protocol getDefaultProtocol() {
		return Protocol.ALL;
	}

	// ***********************************************************************
	protected class VCLC extends ValueChangeListenerClass {

		@Override
		public void fireValueChangeListener(ValueChangeEvent valueChangeEvent) {
			super.fireValueChangeListener(valueChangeEvent);
		}

	}

	public void showDebugPanel(boolean show) {
		for(LinkHeader lh: remover.getLinkHeaders()) {
			DevicePanel devicePanel = unitsPanel.getDevicePanel(lh);
			devicePanel.showDebugPanel(show);
		}
	}

	// ************************************************************************************************************
	private class Remover{

		private int waitTime;
		private volatile Set<LinkHeaderController> controllers = new HashSet<>();
		private Map<LinkHeader, String> serialNumbers = new TreeMap<>();

		public Remover(int waitTime) {
			this.waitTime = waitTime;
		}

		public void doDump(LinkHeader linkHeader) {
			LinkHeaderController controller = getController(linkHeader);
			if(controller!=null)
				controller.doDump();
		}

		public void doDump(LinkHeader linkHeader , String text) {
			LinkHeaderController controller = getController(linkHeader);
			if(controller!=null)
				controller.doDump(text);
		}

		public List<LinkHeader> getLinkHeaders() {
			List<LinkHeader> linkSheaders = new ArrayList<>();
			for(LinkHeaderController c:getControllers())
				linkSheaders.add(c.getLinkHeader());

			return linkSheaders;
		}

		public void removeAll() {
			for(LinkHeaderController c:getControllers())
				c.stopThread();

			if (unitsPanel != null) {
				unitsPanel.removeAll();
				unitsPanel.revalidate();
				unitsPanel.getParent().getParent();
			}
		}

		public boolean isMute(Packet packet) {
			LinkHeaderController controller = getController(getLinkHeader(packet));
			return controller!=null ? controller.isMute() : true;
		}

		private LinkHeaderController getController(LinkHeader linkHeader) {
			LinkHeaderController controller = null;

			for(LinkHeaderController c:getControllers()){
				LinkHeader lh = c.getLinkHeader();
				if(lh!=null ? lh.equals(linkHeader) : linkHeader==null){
					controller = c;
					break;
				}
			}

			return controller;
		}

		public boolean isMute() {
			boolean isMute = false;
			for(Iterator<LinkHeaderController> i = controllers.iterator(); i.hasNext();){
				boolean m = i.next().isMute();
				if(m==true){
					isMute = true;
					break;
				}
			}
			return isMute;
		}

		public void setMute(Packet packet) {
			if(packet!=null){
				PacketHeader header = packet.getHeader();

				if (header != null && header.getGroupId()==Packet.IRT_SLCP_PACKET_ID_MEASUREMENT && header.getOption()==0) {

					logger.debug(packet);
					byte mesurementStatus = packet.getClass().equals(Packet.class)
							? Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_STATUS
									: Packet.IRT_SLCP_PARAMETER_MEASUREMENT_PICOBUC_STATUS;

					Payload payload = packet.getPayload(mesurementStatus);
					if (payload != null) {
						LinkHeader linkHeader = getLinkHeader(packet);

						for (LinkHeaderController c : getControllers()) {
							LinkHeader lh = c.getLinkHeader();
							if (linkHeader.equals(lh)) {
								c.setMute((payload.getInt(0)&(linkHeader.getAddr()==0 ? MonitorPanelConverter.MUTE : MonitorPanelSSPA.MUTE))>0);
							}
						}
					}
				}
			}
		}

		public LinkHeaderController[] getControllers() {
			LinkHeaderController[] lhc = new LinkHeaderController[controllers.size()];
			controllers.toArray(lhc);
			return lhc;
		}

		public int getAlarm() {
			int alarm = 0;
			for(Iterator<LinkHeaderController> i = controllers.iterator(); i.hasNext();){
				int a = i.next().getAlarm();
				if(alarm<a)
					alarm = a;
			}
			return alarm;
		}

		public void setAlarm(Packet packet) {
			if(packet!=null){
				PacketHeader header = packet.getHeader();

				if (header != null && header.getPacketId()==PacketWork.PACKET_ID_ALARMS_SUMMARY && header.getOption()==0) {

					Payload payload = packet.getPayload(0);
					if (payload != null) {
						LinkHeader linkHeader = getLinkHeader(packet);

						for (LinkHeaderController c : getControllers()) {
							LinkHeader lh = c.getLinkHeader();
							if (linkHeader.equals(lh)) {
								c.setAlarm(payload.getInt(0)&7);
							}
						}
					}
				}
			}
		}

		public void setLinkHeader(LinkHeader linkHeader, DeviceInfo deviceInfo) {
			logger.entry(linkHeader);

			LinkHeaderController controller = new LinkHeaderController(linkHeader, deviceInfo);
			if(controllers.add(controller)){
				startThread(controller);
				gui.setConnected(true);
			}else{
				for (LinkHeaderController c:getControllers()) {
					if (c.equals(controller)){
						c.reset();
					}
				}
			}

			logger.exit(controller);
		}

		public void update(LinkHeaderController linkHeaderController) {
			logger.trace(linkHeaderController);
			if(unitsPanel.remove(linkHeaderController.getLinkHeader()))
					gui.repaint();

			controllers.remove(linkHeaderController);
			serialNumbers.remove(linkHeaderController.getLinkHeader());

			if(controllers.isEmpty()){
				gui.setConnected(false);
				protocol = getDefaultProtocol();
				softReleaseChecker = null;
			}
		}

		public void setSerialNumbers(LinkHeader linkHeader, StringData serialNumber) {
			if(serialNumber!=null) {
				String sn = serialNumber.toString();
				String oldSerialNumber = serialNumbers.put(linkHeader, sn);
				if (!sn.equals(oldSerialNumber)) {
					String fileName = null;
					for (LinkHeader l : serialNumbers.keySet()) {
						if (fileName == null)
							fileName = serialNumbers.get(l);
						else
							fileName += "_" + serialNumbers.get(l);
					}
					DumpControllers.setSysSerialNumber(fileName);
				}
			}
		}

		// ******************************* class LinkHeaderController *************************************
		private class LinkHeaderController extends Thread {
			private LinkHeader linkHeader;
			private volatile boolean reset;
			protected DumpControllers dumpControllers;
			private int alarm;
			private boolean isMute;
			private DeviceInfo deviceInfo;

			// LinkHeaderController start in class Remover setLinkHeader(LinkHeader linkHeader);
			public LinkHeaderController(LinkHeader linkHeader, DeviceInfo deviceInfo){
				super(GuiControllerAbstract.class.getSimpleName()+".LinkHeaderController-"+new RundomNumber());
				this.linkHeader = linkHeader!=null ? linkHeader : new LinkHeader((byte)0, (byte)0, (short) 0);
				this.deviceInfo = deviceInfo;
			}

			public void doDump(String text) {
				dumpControllers.doDump(text);
			}

			public void doDump() {
				dumpControllers.notifyAllControllers();
			}

			@Override
			public void run() {
				logger.entry(linkHeader);
				dumpControllers = new DumpControllers(linkHeader, deviceInfo);

				do{
					logger.trace("while loop");
					reset = false;
					synchronized (this) {
						try {
							wait(waitTime);
						} catch (InterruptedException e) {
							logger.catching(e);
						}
					}
					logger.debug("reset = {}", reset);

				}while(reset);

				dumpControllers.stop();
				update(this);
				logger.exit(linkHeader);
			}

			public synchronized void reset(){
				logger.trace("reset()");
				reset = true;
				notify();
			}

			@Override
			public int hashCode() {
				return linkHeader!=null ? linkHeader.getIntAddr() : -1;
			}

			@Override
			public boolean equals(Object obj) {
				return obj!=null ? obj.hashCode()==hashCode() : false;
			}

			@Override
			public String toString() {
				return "LinkHeaderController [linkHeader=" + linkHeader + "]";
			}

			public LinkHeader getLinkHeader() {
				return linkHeader;
			}

			public int getAlarm() {
				return alarm;
			}

			public void setAlarm(int alarm) {
				this.alarm = alarm;
			}

			public boolean isMute() {
				return isMute;
			}

			public void setMute(boolean isMute) {
				this.isMute = isMute;
			}

			public synchronized void stopThread(){
				
			}
		}
	}

	//************************************ class AlarmWorker **********************************************
	private class AlarmWorker extends Thread{

		private Packet packet;

		public AlarmWorker(Packet packet) {
			this.packet = packet;
			startThread(this);
		}

		@Override
		public void run() {
			remover.setAlarm(packet);
			int alarm = remover.getAlarm();
			LinkHeader linkHeader = getLinkHeader(packet);
//			Integer a = alarms.get(linkHeader);

//TODO			if(a==null || !a.equals(alarm)){
				vclc.fireValueChangeListener(new ValueChangeEvent(alarm, ALARM));
				DevicePanel unitPanel = unitsPanel.getDevicePanel(packet instanceof LinkedPacket ? linkHeader : new LinkHeader((byte)0, (byte)0, (short) 0));
				if(unitPanel!=null){
					unitPanel.setAlarm(remover.getController(linkHeader).getAlarm());
					alarms.put(linkHeader, alarm);
				}
//TODO			}
		}

	}

	//************************************ class MeasurementWorker **********************************************
	private class MeasurementWorker extends Thread{

		private Packet packet;

		public MeasurementWorker(Packet packet) {
			this.packet = packet;
			startThread(this);
		}

		@Override
		public void run() {
			remover.setMute(packet);
			boolean isMute = remover.isMute();
			
			LinkHeader linkHeader = getLinkHeader(packet);
//			Boolean get = mutes.get(linkHeader);
//TODO			if(get==null || get.equals(isMute)){
				vclc.fireValueChangeListener(new ValueChangeEvent(isMute, MUTE));
				mutes.put(linkHeader, isMute);
//TODO			}
			DevicePanel unitPanel = unitsPanel.getDevicePanel(packet instanceof LinkedPacket ? linkHeader : new LinkHeader((byte)0, (byte)0, (short) 0));
			unitPanel.setMute(remover.isMute(packet));
		}
	}

	//************************************ class PanelWorker **********************************************
	// Add Panels to the 'unitsPanel'
	public class PanelWorker extends Thread {

		private Packet packet;
		private DeviceInfo deviceInfo;

		public PanelWorker(Packet packet, DeviceInfo deviceInfo) {
			this.packet = packet;
			this.deviceInfo = deviceInfo;
			startThread(this);
		}

		@Override
		public void run() {

			LinkHeader linkHeader = packet instanceof LinkedPacket ? ((LinkedPacket) packet).getLinkHeader() : new LinkHeader((byte)0, (byte)0, (short) 0);
			deviceInfos.put(linkHeader, deviceInfo);

			remover.setLinkHeader(packet instanceof LinkedPacket ? linkHeader : null, deviceInfo);

			unitsPanel.remove("DemoPanel");

			if (protocol != Protocol.ALL && packet.getPayloads() != null && !unitsPanel.contains(linkHeader)) {
				GuiControllerAbstract.this.gui.setConnected(true);

				remover.setSerialNumbers(linkHeader, deviceInfo.getSerialNumber()); ;

				DevicePanel unitPanel;
				if (protocol == Protocol.CONVERTER)
					unitPanel = getConverterPanel(deviceInfo);
				else
					unitPanel = getNewBaisPanel(linkHeader, deviceInfo, 0, 0, 0, 0, unitsPanel.getHeight());

				unitsPanel.add(unitPanel);

				getSoftReleaseChecker();
				if (softReleaseChecker != null)
					softReleaseChecker.check(deviceInfo);

				deviceInfo.setInfoPanel(unitPanel.getInfoPanel());
				unitsPanel.revalidate();
				unitsPanel.repaint();

				StringData unitPartNumber = deviceInfo.getUnitPartNumber();
				if (protocol == Protocol.LINKED) {
					if (!unitPartNumber.equals("N/A")) {
						logger.trace("protocol={}, unitPartNumber={}", protocol, unitPartNumber);
						ProgressBar.setMinMaxValue("330", unitPartNumber.toString().substring(7, 11));
					}
				} else if (protocol == Protocol.CONVERTER) {
					logger.trace(protocol);
					ProgressBar.setMinMaxValue("-80", "120");
				}
			}
		}
	}

	private void startThread(Thread thread) {
		int priority = thread.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			thread.setPriority(priority-1);
		thread.setDaemon(true);
		thread.start();
	}
}
