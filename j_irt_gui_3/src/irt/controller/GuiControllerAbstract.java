package irt.controller;

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
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.serial_port.ComPort;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.value.getter.DeviceInfoGetter;
import irt.controller.serial_port.value.getter.ValueChangeListenerClass;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.MyThreadFactory;
import irt.data.PacketWork;
import irt.data.RundomNumber;
import irt.data.StringData;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketAbstract.Priority;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.irt_gui.IrtGui;
import irt.tools.KeyValue;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.head.Console;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.UnitsContainer;
import irt.tools.panel.subpanel.monitor.MonitorPanelConverter;
import irt.tools.panel.subpanel.monitor.MonitorPanelSSPA;
import irt.tools.panel.subpanel.progressBar.ProgressBar;
import irt.tools.textField.UnitAddressField;
import jssc.SerialPortList;

public abstract class GuiControllerAbstract extends Thread {

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	public static final int CONNECTION	= 1;
	public static final int ALARM		= 2;
	public static final int MUTE		= 3;

	protected static final String SERIAL_PORT = "serialPort";

	public static enum Protocol{
								ALL,
								CONVERTER,
								LINKED,
								DEMO;

								private int deviceType;

								public int getDeviceType() {
									return deviceType;
								}

								public Protocol setDeviceType(int deviceType) {
									this.deviceType = deviceType;
									return this;
								}
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

	private Remover remover = new Remover(11, TimeUnit.SECONDS);

	private static Map<LinkHeader, DeviceInfo>  deviceInfos = new HashMap<>();
	public Map<LinkHeader, Boolean> mutes = new HashMap<>();
	public Map<LinkHeader, Integer> alarms = new HashMap<>();

	private byte address;

	private PacketListener packetListener = new PacketListener() {

		private boolean powerIsOff;

		@Override
		public void onPacketRecived(Packet packet) {
			logger.trace(packet);

			if (packet != null && packet.getHeader() != null) {
				PacketHeader header = packet.getHeader();

				byte packetType = header.getPacketType();
				if (packetType == PacketImp.PACKET_TYPE_RESPONSE) {

					DeviceInfo deviceInfo;
					switch (header.getGroupId()) {
					case PacketImp.GROUP_ID_DEVICE_INFO:
						deviceInfo = new DeviceInfo(packet);
						logger.debug("{}\n", deviceInfo);

						int type = deviceInfo.getType();
						switch (type) {
						case DeviceInfo.DEVICE_TYPE_L_TO_70:
						case DeviceInfo.DEVICE_TYPE_L_TO_140:
						case DeviceInfo.DEVICE_TYPE_70_TO_L:
						case DeviceInfo.DEVICE_TYPE_140_TO_L:
						case DeviceInfo.DEVICE_TYPE_L_TO_KU:
						case DeviceInfo.DEVICE_TYPE_L_TO_C:
						case DeviceInfo.DEVICE_TYPE_L_TO_KA:
						case DeviceInfo.DEVICE_TYPE_70_TO_KY:
						case DeviceInfo.DEVICE_TYPE_KU_TO_70:
						case DeviceInfo.DEVICE_TYPE_140_TO_KU:
						case DeviceInfo.DEVICE_TYPE_KU_TO_140:
						case DeviceInfo.DEVICE_TYPE_KU_TO_L:
						case DeviceInfo.DEVICE_TYPE_C_TO_L:
						case DeviceInfo.DEVICE_TYPE_SSPA_CONVERTER:
						case DeviceInfo.DEVICE_TYPE_MODUL: /* Temporarily added by Alex @ 19-09-2014 */
						case DeviceInfo.DEVICE_TYPE_L_TO_DBS:
							protocol = Protocol.CONVERTER.setDeviceType(type);
							break;
						case DeviceInfo.DEVICE_TYPE_DLRS:
						case DeviceInfo.DEVICE_TYPE_BIAS_BOARD:
						case DeviceInfo.DEVICE_TYPE_PICOBUC_L_TO_KU:
						case DeviceInfo.DEVICE_TYPE_PICOBUC_L_TO_C:
						case DeviceInfo.DEVICE_TYPE_SSPA:
						case DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR:
						case DeviceInfo.DEVICE_TYPE_HPB_L_TO_KU:
						case DeviceInfo.DEVICE_TYPE_HPB_L_TO_C:
						case DeviceInfo.DEVICE_TYPE_HPB_SSPA:
						case DeviceInfo.DEVICE_TYPE_KA_BAND:
						case DeviceInfo.DEVICE_TYPE_KA_SSPA:
							protocol = Protocol.LINKED.setDeviceType(type);
							break;
						default:
							if (type > 0) {
								JOptionPane.showMessageDialog(headPanel, "The Device is not Supported.(device Id=" + type + ")");
								logger.warn("The Device is not Supported.(device Id={})", type);
							} else
								logger.warn("Can not connect. {}", packet);
						}

						new PanelWorker(packet, deviceInfo);
						break;
					case PacketImp.GROUP_ID_ALARM:
						if(header.getPacketId()==PacketWork.PACKET_ID_ALARMS_SUMMARY && header.getOption()==0) 
							new AlarmWorker(packet);
						break;
					case PacketImp.GROUP_ID_MEASUREMENT:
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
		guiController = this;

		DevicePanel.DEBUG_PANEL.setGuiController(this);

		comPortThreadQueue.setSerialPort(new ComPort(prefs.get(SERIAL_PORT, "COM1")));
		console = new Console(gui, "Console");

		JPanel contentPane = (JPanel) gui.getContentPane();
		Component[] components = contentPane.getComponents();
		for(Component c:components){
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
		}
		comPortThreadQueue.addPacketListener(packetListener);
	}

	public static DeviceInfo getDeviceInfo(LinkHeader linkHeader) {
		return deviceInfos.get(linkHeader);
	}

	protected SoftReleaseChecker getSoftReleaseChecker() {
		return null;
	}

	protected abstract DevicePanel getConverterPanel(DeviceInfo di);
	protected abstract DevicePanel getNewBiasPanel(LinkHeader linkHeader, DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight);

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

				String portName = ComPortThreadQueue.getSerialPort().getPortName();
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
				ComPortThreadQueue.getSerialPort().closePort();

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

		logger.trace("reset();");
		comPortThreadQueue.clear();
		gui.setConnected(false);
		remover.removeAll();

		protocol = getDefaultProtocol();
		logger.trace("protocol={}", protocol);
	}
//
//	protected boolean removePanel(LinkHeader linkHeader) {
////		comPortThreadQueue.getSerialPort().setRun(false, "Remove Panel");
//		boolean removed;
//		if (removed = unitsPanel.remove(linkHeader)) {
//			logger.warn("removePanel({})", linkHeader);
//			unitsPanel.revalidate();
//			unitsPanel.getParent().getParent().repaint();
//		}
//		return removed;
//	}

	protected JComboBox<String> getSerialPortSelection() {
		return serialPortSelection;
	}

	public void addChangeListener(ValueChangeListener valueChangeListener){
		vclc.addVlueChangeListener(valueChangeListener);
	}

	public byte getAddress() {
		return address;
	}

	public void setAddress(byte address) {
		this.address = address;
		reset();
	}

	protected boolean isSerialPortSet(){
		boolean set = false;
		if (serialPortSelection != null) {
			Object selectedItem = serialPortSelection.getSelectedItem();
			if (selectedItem != null && ComPortThreadQueue.getSerialPort().getPortName().equals(selectedItem.toString())) {
				set = true;
			}
		}
		return set;
	}

	protected void getConverterInfo() {
		if(!protocol.equals(Protocol.LINKED)){
			logger.trace("protocol = {}", protocol);

			comPortThreadQueue.add(new DeviceInfoGetter() {
				@Override
				public Priority getPriority() {
					return Priority.ALARM;
				}
			});
		}
	}

	private static byte[] addresses = UnitAddressField.DEFAULT_ADDRESS;
	public static byte[] getAddresses() {
		return addresses;
	}

	private static GuiControllerAbstract guiController;

	public static void setAddresses(byte[] addresses) {
		GuiControllerAbstract.addresses = addresses;
		Optional.ofNullable(guiController).ifPresent(gui->gui.reset());
	}

	protected void getUnitsInfo() {
		if (!protocol.equals(Protocol.CONVERTER)) {

			for(Byte b:addresses){
				DeviceInfoGetter packetWork = new DeviceInfoGetter(new LinkHeader(b, (byte) 0, (short) 0)) {
					@Override
					public Priority getPriority() {
						return Priority.ALARM;
					}
				};
				comPortThreadQueue.add(packetWork);
			}
		}
	}

	public LinkHeader getLinkHeader(Packet packet) {
		LinkHeader linkHeader = null;

		if (packet instanceof LinkedPacket) 
			linkHeader = ((LinkedPacket) packet).getLinkHeader();

		if(linkHeader==null)
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

		private long waitTime;
		private volatile Set<LinkHeaderTread> controllers = new HashSet<>();
		private Map<LinkHeader, String> serialNumbers = new TreeMap<>();

		public Remover(long duration, TimeUnit timeUnit ) {
			this.waitTime = timeUnit.toMillis(duration);
		}

		public void doDump(LinkHeader linkHeader) {
			LinkHeaderTread controller = getController(linkHeader);
			if(controller!=null)
				controller.doDump();
		}

		public void doDump(LinkHeader linkHeader , String text) {
			LinkHeaderTread controller = getController(linkHeader);
			if(controller!=null)
				controller.doDump(text);
		}

		public List<LinkHeader> getLinkHeaders() {
			List<LinkHeader> linkSheaders = new ArrayList<>();
			for(LinkHeaderTread c:getControllers())
				linkSheaders.add(c.getLinkHeader());

			return linkSheaders;
		}

		public void removeAll() {
			for(LinkHeaderTread c:getControllers())
				c.stopThread();

			if (unitsPanel != null) {
				unitsPanel.removeAll();
				unitsPanel.revalidate();
				unitsPanel.getParent().getParent();
			}
		}

		public boolean isMute(Packet packet) {
			LinkHeaderTread controller = getController(getLinkHeader(packet));
			return controller!=null ? controller.isMute() : true;
		}

		private LinkHeaderTread getController(LinkHeader linkHeader) {
			LinkHeaderTread controller = null;

			for(LinkHeaderTread c:getControllers()){
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
			for(Iterator<LinkHeaderTread> i = controllers.iterator(); i.hasNext();){
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

				if (header != null
						&& header.getGroupId()==PacketImp.GROUP_ID_MEASUREMENT
						&& (header.getPacketId()==PacketWork.PACKET_ID_MEASUREMENT_STATUS || header.getPacketId()==PacketWork.PACKET_ID_MEASUREMENT_ALL)
						&& header.getOption()==0) {

					logger.debug(packet);
					byte mesurementStatus = packet.getClass().equals(PacketImp.class) || header.getPacketId()==PacketWork.PACKET_ID_MEASUREMENT_STATUS
							? PacketImp.PARAMETER_MEASUREMENT_FCM_STATUS
									: PacketImp.PARAMETER_MEASUREMENT_STATUS;

					Payload payload = packet.getPayload(mesurementStatus);
					if (payload != null) {
						LinkHeader linkHeader = getLinkHeader(packet);

						for (LinkHeaderTread c : getControllers()) {
							LinkHeader lh = c.getLinkHeader();
							if (linkHeader.equals(lh)) {
								c.setMute((payload.getInt(0)&(linkHeader.getAddr()==0 || header.getPacketId()==PacketWork.PACKET_ID_MEASUREMENT_STATUS ? MonitorPanelConverter.MUTE : MonitorPanelSSPA.MUTE))>0);
							}
						}
					}
				}
			}
		}

		public LinkHeaderTread[] getControllers() {
			int size = controllers.size();

			if(size<0)
				size = 0;

			LinkHeaderTread[] lhc = new LinkHeaderTread[size];
			controllers.toArray(lhc);
			return lhc;
		}

		public int getAlarm() {
			int alarm = 0;
			for(Iterator<LinkHeaderTread> i = controllers.iterator(); i.hasNext();){
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

						for (LinkHeaderTread c : getControllers()) {
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

			LinkHeaderTread controller = new LinkHeaderTread(linkHeader, deviceInfo);
			if(controllers.add(controller)){
				new MyThreadFactory().newThread(controller).start();
				gui.setConnected(true);
			}else{
				for (LinkHeaderTread c:getControllers()) {
					if (c.equals(controller)){
						c.reset();
					}
				}
			}
		}

		public void update(LinkHeaderTread linkHeaderTread) {
			logger.trace(linkHeaderTread);

			final LinkHeader linkHeader = linkHeaderTread.getLinkHeader();

			if(unitsPanel.remove(linkHeader))
					gui.repaint();

			controllers.remove(linkHeaderTread);
			serialNumbers.remove(linkHeader);

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

		// ******************************* class LinkHeaderTread *************************************
		private class LinkHeaderTread extends Thread {
			private LinkHeader linkHeader;
			private volatile boolean reset;
			protected DumpControllers dumpControllers;
			private int alarm;
			private boolean isMute;
			private DeviceInfo deviceInfo;

			// LinkHeaderTread start in class Remover setLinkHeader(LinkHeader linkHeader);
			public LinkHeaderTread(LinkHeader linkHeader, DeviceInfo deviceInfo){
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
					logger.trace("reset = {}", reset);

				}while(reset);

				dumpControllers.stop();
				update(this);
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
				return "LinkHeaderTread [linkHeader=" + linkHeader + "]";
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
			new MyThreadFactory().newThread(this).start();
		}

		@Override
		public void run() {
			remover.setAlarm(packet);
			int alarm = remover.getAlarm();
			LinkHeader linkHeader = getLinkHeader(packet);
//			Integer a = alarms.get(linkHeader);

//TODO			if(a==null || !a.equals(alarm)){
				vclc.fireValueChangeListener(new ValueChangeEvent(alarm, ALARM));
				DevicePanel unitPanel = unitsPanel.getDevicePanel(linkHeader);
				if(unitPanel!=null){
					final Remover.LinkHeaderTread controller = remover.getController(linkHeader);

					if(controller==null)
						return;

					final int a = controller.getAlarm();
					unitPanel.setAlarm(a);
					alarms.put(linkHeader, alarm);
				}
//TODO			}
		}

	}

	//************************************ class MeasurementWorker **********************************************
	private class MeasurementWorker extends Thread{
		private final Logger logger = LogManager.getLogger();

		private Packet packet;

		public MeasurementWorker(Packet packet) {
			this.packet = packet;
			new MyThreadFactory().newThread(this).start();
		}

		@Override
		public void run() {
			logger.entry(packet);
			remover.setMute(packet);
			boolean isMute = remover.isMute();
			
			LinkHeader linkHeader = getLinkHeader(packet);
//			Boolean get = mutes.get(linkHeader);
//TODO			if(get==null || get.equals(isMute)){
				vclc.fireValueChangeListener(new ValueChangeEvent(isMute, MUTE));
				mutes.put(linkHeader, isMute);
//TODO			}
			DevicePanel unitPanel = unitsPanel.getDevicePanel(linkHeader);
			if(unitPanel!=null)
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
			new MyThreadFactory().newThread(this).start();
		}

		@Override
		public void run() {

			LinkHeader linkHeader = packet instanceof LinkedPacket ? ((LinkedPacket) packet).getLinkHeader() : new LinkHeader((byte)0, (byte)0, (short) 0);
			deviceInfos.put(linkHeader, deviceInfo);

			remover.setLinkHeader(linkHeader, deviceInfo);

			unitsPanel.remove("DemoPanel");

			if (protocol != Protocol.ALL && packet.getPayloads() != null && !unitsPanel.contains(linkHeader)) {
				GuiControllerAbstract.this.gui.setConnected(true);

				remover.setSerialNumbers(linkHeader, deviceInfo.getSerialNumber()); ;

				DevicePanel unitPanel;
				if (protocol == Protocol.CONVERTER)
					unitPanel = getConverterPanel(deviceInfo);
				else
					unitPanel = getNewBiasPanel(linkHeader, deviceInfo, 0, 0, 0, 0, unitsPanel.getHeight());

				unitsPanel.add(unitPanel);

				softReleaseChecker =getSoftReleaseChecker();
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
}
