package irt.controller;

import irt.controller.serial_port.ComPort;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.value.getter.ValueChangeListenerClass;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.StringData;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.tools.KeyValue;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.head.Console;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.UnitsContainer;
import irt.tools.panel.subpanel.progressBar.ProgressBar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jssc.SerialPortException;
import jssc.SerialPortList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public abstract class GuiControllerAbstract extends Thread {

	protected final Logger logger = (Logger) LogManager.getLogger(getClass().getName());

	public static final int CONNECTION = 1;

	protected static final String SERIAL_PORT = "serialPort";

	public static enum Protocol{
								ALL,
								CONVERTER,
								LINKED,
								DEMO
							};

	protected static Preferences prefs = Preferences.userRoot().node("IRT Technologies inc.");

	protected static ComPortThreadQueue comPortThreadQueue = new ComPortThreadQueue();

	private Console console;

	protected UnitsContainer unitsPanel;

	protected JComboBox<String> serialPortSelection;
	protected JComboBox<KeyValue<String, String>> languageComboBox;
	protected HeadPanel headPanel;

	protected VCLC vclc =  new VCLC();
	protected static DumpControllers dumpControllers;
	protected SoftReleaseChecker softReleaseChecker = getSoftReleaseChecker();
	protected Protocol protocol = Protocol.ALL;
	private static DeviceInfo deviceInfo;

	private byte address;

	@SuppressWarnings("unchecked")
	public GuiControllerAbstract(String threadName, JFrame gui) {
		super(threadName);

		address = (byte) prefs.getInt("address", 254);

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
		comPortThreadQueue.addPacketListener(new PacketListener() {

			private RemoveComponent remover = new RemoveComponent(16000);

			@Override
			public void packetRecived(Packet packet) {
				logger.trace(packet);

				if (packet != null && packet.getHeader()!=null) {
					ComPort serialPort = comPortThreadQueue.getSerialPort();
					switch (packet.getHeader().getGroupId()) {
					case Packet.IRT_SLCP_PACKET_ID_DEVICE_INFO:
						DevicePanel unitPanel = null;
						deviceInfo = new DeviceInfo(packet);

						int type = deviceInfo.getType();
						if(dumpControllers!=null)
							dumpControllers.setInfo(deviceInfo);
						switch(type){
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
							unitPanel = getConverterPanel(deviceInfo);
							protocol = Protocol.CONVERTER;
							break;
						case DeviceInfo.DEVICE_TYPE_BAIS_BOARD:
						case DeviceInfo.DEVICE_TYPE_PICOBUC:
						case DeviceInfo.DEVICE_TYPE_PICOBUC_L_TO_KU:
						case DeviceInfo.DEVICE_TYPE_PICOBUC_L_TO_C:
							protocol = Protocol.LINKED;
							unitPanel = getNewBaisPanel(((LinkedPacket)packet).getLinkHeader(), "("+deviceInfo.getSerialNumber()+") "+deviceInfo.getUnitName(), 0, 0, 0, 0, unitsPanel.getHeight());
							break;
						default:
							if(type>0){
								JOptionPane.showMessageDialog(headPanel, "The Device is not Supported.(device Id="+type+")");
								logger.warn("The Device is not Supported.(device Id={})", type);
							}else
								logger.warn("Can not connect");
						}

						if(packet.getHeader().getType()==Packet.IRT_SLCP_PACKET_TYPE_RESPONSE){
							logger.trace(packet.getHeader());

							remover.setLinkHeader(packet instanceof LinkedPacket ? ((LinkedPacket)packet).getLinkHeader() : null);

							unitsPanel.remove("DemoPanel");

							if(unitPanel!=null && packet.getPayloads()!=null && unitsPanel.add(unitPanel)==unitPanel){

								getSoftReleaseChecker();
								if(softReleaseChecker!=null)
									softReleaseChecker.check(deviceInfo);

								deviceInfo.setInfoPanel(unitPanel.getInfoPanel());
								unitsPanel.revalidate();
								unitsPanel.repaint();
								if(headPanel!=null)
									unitPanel.addStatusChangeListener(headPanel.getStatusChangeListener());

								if(dumpControllers!=null)
									dumpControllers.stop();
								dumpControllers = new DumpControllers(unitsPanel, packet instanceof LinkedPacket ? ((LinkedPacket)packet).getLinkHeader() : null, deviceInfo);
								dumpControllers.addVlueChangeListener(headPanel.getStatusChangeListener());

								StringData unitPartNumber = deviceInfo.getUnitPartNumber();
								if(protocol == Protocol.LINKED){
									if(!unitPartNumber.equals("N/A")){
										logger.trace("protocol={}, unitPartNumber={}", protocol, unitPartNumber);
										ProgressBar.setMinMaxValue("330", unitPartNumber.toString().substring(7, 11));
									}
								}else if(protocol == Protocol.CONVERTER){
									logger.trace(protocol);
									ProgressBar.setMinMaxValue("-80", "120");
								}
							}

						}else{
							if(unitsPanel!=null && unitsPanel.getComponentCount()>0) {
								synchronized (this) {
									notify();
								}
							}
						}

						break;
					default:
						if(packet.getHeader().getType()==Packet.IRT_SLCP_PACKET_TYPE_REQUEST && serialPort.isRun()){
							synchronized (GuiControllerAbstract.this) {
								GuiControllerAbstract.this.notify();
							}
						}
						unitPanel = null;
//						System.out.println(packet);
					}

					if(unitsPanel!=null && unitsPanel.getComponentCount()>0 && unitsPanel.getComponent(DevicePanel.class)!=null)
						vclc.fireValueChangeListener(new ValueChangeEvent(new Boolean(true), CONNECTION));
					else
						vclc.fireValueChangeListener(new ValueChangeEvent(new Boolean(false), CONNECTION));
				}else
					vclc.fireValueChangeListener(new ValueChangeEvent(new Boolean(false), CONNECTION));
			}
		});
	}


	public static DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

	protected SoftReleaseChecker getSoftReleaseChecker() {
		return null;
	}

	protected abstract DevicePanel getConverterPanel(DeviceInfo di);
	protected abstract DevicePanel getNewBaisPanel(LinkHeader linkHeader, String text, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight);

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


	public static DumpControllers getDumpControllers() {
		return dumpControllers;
	}

	protected void setSerialPort() {
		setSerialPort(serialPortSelection.getSelectedItem().toString());
	}

	protected void setSerialPort(String serialPortName) {

		if (dumpControllers != null) {
			dumpControllers.stop();
			dumpControllers = null;
		}

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

		if (unitsPanel != null) {
			unitsPanel.removeAll();
			unitsPanel.revalidate();
			unitsPanel.getParent().getParent().repaint();
		}

		protocol = Protocol.ALL;
		logger.debug("protocol={}", protocol);
	}

	protected boolean removePanel(LinkHeader linkHeader) {
		comPortThreadQueue.getSerialPort().setRun(false);
		boolean removed;
		if (removed = unitsPanel.remove(linkHeader)) {
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

	// ***********************************************************************
	protected class VCLC extends ValueChangeListenerClass {

		@Override
		public void fireValueChangeListener(ValueChangeEvent valueChangeEvent) {
			super.fireValueChangeListener(valueChangeEvent);
		}

	}

	// ************************************************************************************************************
	public class RemoveComponent extends Thread {


		protected final Logger logger = (Logger) LogManager.getLogger();

		private int waitTime;
		private LinkHeader linkHeader;
		private volatile boolean packetReceived;

		public RemoveComponent(int waitTime) {
			super("RemoveComponent");
			logger.info("* Start Remove Controller. *");

			this.waitTime = waitTime;

			int priority = getPriority();
			if (priority > Thread.MIN_PRIORITY)
				setPriority(priority - 1);
			start();
		}

		public void setLinkHeader(LinkHeader linkHeader) {
			logger.entry(this.linkHeader = linkHeader);
			synchronized (this) {
				logger.debug("notify();");
				packetReceived = true;
				notify();
			}
			logger.exit();
		}

		@Override
		public void run() {
			logger.entry();
			while (true) {
				logger.trace("while entry");

				synchronized (this) {
					try {
						wait(waitTime);
					} catch (InterruptedException e) {
						logger.catching(e);
					}
				}
				logger.trace("packetReceived={}", packetReceived);
				if (packetReceived){
					packetReceived = false;
					continue;
				}

				synchronized (this) {
					if (!packetReceived && removePanel(linkHeader)) {
						logger.trace("Remove Panel( {} )", linkHeader);
						protocol = Protocol.ALL;
						if (dumpControllers != null) {
							dumpControllers.stop();
							dumpControllers = null;
						}
						softReleaseChecker = null;
					}
				}
			}
		}
	}
}
