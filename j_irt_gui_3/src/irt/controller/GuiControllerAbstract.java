package irt.controller;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.JsscComPort;
import irt.controller.serial_port.PureJavaComPort;
import irt.controller.serial_port.SerialPortInterface;
import irt.controller.serial_port.value.getter.ValueChangeListenerClass;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.DeviceInfo.DeviceType;
import irt.data.DeviceInfo.Protocol;
import irt.data.MyThreadFactory;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.DeviceInfoPacket;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.RetransmitPacket;
import irt.data.packet.control.ModuleListPacket;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketWork;
import irt.irt_gui.IrtGui;
import irt.tools.KeyValue;
import irt.tools.fx.module.ModuleSelectFxPanel;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.head.Console;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.UnitsContainer;
import irt.tools.textField.UnitAddressField;

public abstract class GuiControllerAbstract implements Runnable, PacketListener{

	public static final String SERIAL_PORT_CLASS = "Serial Port CLASS";

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	public static final int CONNECTION	= 1;
	public static final int ALARM		= 2;
	public static final int MUTE		= 3;

	protected static final String SERIAL_PORT = "serialPort";

	protected static Preferences prefs = Preferences.userRoot().node("IRT Technologies inc.");

	protected static ComPortThreadQueue comPortThreadQueue = new ComPortThreadQueue();

	private ScheduledFuture<?> scheduledFuture;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private Console console;
	protected JComboBox<String> serialPortSelection;
	protected JComboBox<KeyValue<String, String>> languageComboBox;
	protected HeadPanel headPanel;

	protected VCLC vclc =  new VCLC();
	protected Protocol protocol = getDefaultProtocol();

	private final PanelController panelController = new PanelController();

	private static Set<DeviceInfo>  deviceInfos = new HashSet<>();

	private byte address;

	private Class<? extends SerialPortInterface> serialPortClass;

	private IrtGui gui;
	protected volatile UnitsContainer unitsPanel;

	@SuppressWarnings("unchecked")
	public GuiControllerAbstract(String threadName, IrtGui gui) {

		this.gui = gui;
		guiController = this;

		final boolean useDefaultComPort = Optional.ofNullable(prefs.get(ComPortThreadQueue.GUI_IS_CLOSED_CORRECTLY, null)).map(Boolean::parseBoolean).orElse(true);

		try {

			if(useDefaultComPort){
				String portClassName = prefs.get(SERIAL_PORT_CLASS, "irt.controller.serial_port.JsscComPort");
				serialPortClass = (Class<JsscComPort>) Class.forName(portClassName);
			}else{
				serialPortClass = PureJavaComPort.class;
				 prefs.put(SERIAL_PORT_CLASS, serialPortClass.getName());
			}

		} catch (ClassNotFoundException e2) {
			logger.catching(e2);
			serialPortClass = JsscComPort.class;
		}


		prefs.putBoolean(ComPortThreadQueue.GUI_IS_CLOSED_CORRECTLY, false);

		console = new Console(gui, "Console");

			JPanel contentPane = (JPanel) gui.getContentPane();
			Component[] components = contentPane.getComponents();
			for (Component c : components) {
				switch (c.getClass().getSimpleName()) {
				case "UnitsContainer":
					unitsPanel = (UnitsContainer) c;
					break;
				case "JComboBox":
					setComboBox((JComboBox<Object>) c);
					break;
				case "HeadPanel":
					logger.trace("set HeadPanel");
					headPanel = (HeadPanel) c;
					Component[] cms = headPanel.getComponents();
					for (Component cm : cms) {
						String n = cm.getName();
						if (n != null && n.equals("Language")) {
							setComboBox(cm);
							break;
						}
					}
				case "IrtPanel":
					c.addMouseListener(new MouseListener() {

						@Override
						public void mouseReleased(MouseEvent e) {
							int modifiers = e.getModifiers();
							if ((modifiers & InputEvent.CTRL_MASK) > 0)
								console.setVisible(!console.isVisible());
						}

						@Override
						public void mousePressed(MouseEvent e) {
						}

						@Override
						public void mouseExited(MouseEvent e) {
						}

						@Override
						public void mouseEntered(MouseEvent e) {
						}

						@Override
						public void mouseClicked(MouseEvent e) {
						}
					});
				}
			}
			comPortThreadQueue.addPacketListener(this);
			scheduledFuture = service.scheduleAtFixedRate(this, 2, 5, TimeUnit.SECONDS);
	}

	public static Optional<DeviceInfo> getDeviceInfo(LinkHeader linkHeader) {
		return deviceInfos.parallelStream().filter(di->di.getLinkHeader().equals(linkHeader!=null ? linkHeader : new LinkHeader((byte)0, (byte)0, (short) 0))).findAny();
	}

	protected abstract DevicePanel getConverterPanel(DeviceInfo di);
	protected abstract DevicePanel getNewBiasPanel(DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight);

	public static ComPortThreadQueue getComPortThreadQueue() {
		return comPortThreadQueue;
	}

	public static Preferences getPrefs() {
		return prefs;
	}

	private ActionListener toJsscListener;
	private ActionListener toPureJavaListener;

	private Optional<PacketAbstract> oPacketToSend;

	private boolean setRetransmit = true;
	private Object moduleList;

	@SuppressWarnings("unchecked")
	private void setComboBox(Component c) {
		String name = c.getName();
		if(name==null)
			return;

		if(name.equals("Unit's Serial Port")){

			logger.trace("set serialPortSelection ={}", c);
			serialPortSelection = (JComboBox<String>) c;

			addMenuSelectPortDriver();
			DefaultComboBoxModel<String> defaultComboBoxModel = initSerialPortSelection();

				String portName = prefs.get(SERIAL_PORT, serialPortSelection.getItemAt(0));
				Optional.of(defaultComboBoxModel.getIndexOf(portName)).filter(i->i>0).ifPresent(i->serialPortSelection.setSelectedIndex(i));
				setSerialPort();

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

//							DefaultComboBoxModel<String> defaultComboBoxModel = initSerialPortSelection();
						}
					}
				});
			}
	}

	private DefaultComboBoxModel<String> initSerialPortSelection() {
		float fontSize = Translation.getValue(Float.class, "serialPortSelection.font.size", 16f);
		serialPortSelection.setFont(Translation.getFont().deriveFont(fontSize));
		DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<String>(PureJavaComPort.getPortNames().toArray(new String[0]));
		defaultComboBoxModel.insertElementAt(Translation.getValue(String.class, "select_serial_port", "Select Serial Port"), 0);
		serialPortSelection.setModel(defaultComboBoxModel);
		Dimension size = serialPortSelection.getSize();
		size.width = Translation.getValue(Integer.class, "serialPortSelection.width", 200);
		serialPortSelection.setSize(size);
		return defaultComboBoxModel;
	}

	private void addMenuSelectPortDriver() {
		final JPopupMenu popup = Optional
								.ofNullable(serialPortSelection.getComponentPopupMenu())
								.orElseGet(
										()->{
											final JPopupMenu jPopupMenu = new JPopupMenu();
											serialPortSelection.setComponentPopupMenu(jPopupMenu);
											return jPopupMenu;
										});
			final JMenuItem menuItem = new JMenuItem();
			popup.add(menuItem);

			toJsscListener = e->{
				serialPortClass = JsscComPort.class;
				 prefs.put(SERIAL_PORT_CLASS, serialPortClass.getName());
				setSerialPort();
				swapListeners(menuItem, "Set PureJava serial port driver", toJsscListener, toPureJavaListener);
			};
			toPureJavaListener = e->{
				serialPortClass = PureJavaComPort.class;
				 prefs.put(SERIAL_PORT_CLASS, serialPortClass.getName());
				setSerialPort();
				swapListeners(menuItem, "Set JSSC serial port driver", toPureJavaListener, toJsscListener);
			};

			if(Optional.ofNullable(serialPortClass).filter(PureJavaComPort.class::equals).isPresent())
				swapListeners(menuItem, "Set JSSC serial port driver", toPureJavaListener, toJsscListener);
			else
				swapListeners(menuItem, "Set PureJava serial port driver", toJsscListener, toPureJavaListener);
	}

	private void swapListeners(final JMenuItem menuItem, String text, ActionListener toRemove, ActionListener toAdd) {

		new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {

				menuItem.setText(text);
				menuItem.removeActionListener(toRemove);
				menuItem.addActionListener(toAdd);
				return null;
			}
		}.execute();
	}

	protected void setSerialPort() {
		final String portName = serialPortSelection.getSelectedItem().toString();
		setSerialPort(portName);
	}

	protected void setSerialPort(String serialPortName) {

		if (serialPortName == null || serialPortName.replaceAll("\\D", "").isEmpty()){
			comPortThreadQueue.close();
			reset();

		}else {

			new MyThreadFactory().newThread(()->{
				try {

					final Constructor<? extends SerialPortInterface> constructor = serialPortClass.getConstructor(String.class);

					SerialPortInterface serialPort = constructor.newInstance(serialPortName);
					comPortThreadQueue.setSerialPort(serialPort);

					prefs.put(SERIAL_PORT, serialPortName);

				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
//					logger.catching(e1);
					comPortThreadQueue.setSerialPort(null);
					Optional.ofNullable(serialPortSelection).ifPresent(sps->{
						final SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

							@Override
							protected Void doInBackground() throws Exception {
								sps.setSelectedItem(0);
								return null;
							}
						};
						swingWorker.execute();
					});
				}

				reset();

			}).start();
		} 

		synchronized (this) {
			notify();
		}

		if(scheduledFuture!=null && !scheduledFuture.isCancelled())
			scheduledFuture.cancel(true);

		scheduledFuture = service.scheduleAtFixedRate(this, 1, 5, TimeUnit.SECONDS);
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

	protected void getConverterInfo() {
//		logger.error("getConverterInfo");

		if(!protocol.equals(Protocol.LINKED)){
			logger.trace("protocol = {}", protocol);

			comPortThreadQueue.add(new DeviceInfoPacket(getAddress()));
		}
	}

	protected void getUnitsInfo() {
//		logger.error("getUnitsInfo; protocol: {}; packetToSend: {}", protocol, packetToSend);

		if (!protocol.equals(Protocol.CONVERTER)) {

			for(Byte addr:addresses){
				comPortThreadQueue.add(new DeviceInfoPacket(addr));

				Optional.ofNullable(oPacketToSend).filter(Optional::isPresent).map(Optional::get).map(p->p.setAddr(addr)).ifPresent(p->comPortThreadQueue.add(p));
			}
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

	public LinkHeader getLinkHeader(Packet packet) {
		return Optional.of(packet).filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).orElse(new LinkHeader((byte)0, (byte)0, (short) 0));
	}

	public Protocol getDefaultProtocol() {
		return Protocol.ALL;
	}

	@Override
	public void onPacketRecived(Packet packet) {
		logger.debug(packet);

		final Optional<Packet> oPacket = Optional.ofNullable(packet);
		final Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);
		// Return if not response
		final boolean noAnswer = oHeader.map(PacketHeader::getPacketType).map(pt->pt!=PacketImp.PACKET_TYPE_RESPONSE).orElse(true);
		if(noAnswer)
			return;

		//check retransmit packet
		final Optional<Short> oPacketId = oHeader.map(PacketHeader::getPacketId);
		final Boolean isRetransmitPacket = oPacketId.map(pid->pid==PacketWork.PACKET_ID_PROTO_RETRANSNIT).orElse(false);
		if(isRetransmitPacket){
			setRetransmit = false;
			return;
		}

		final Optional<Byte> oPacketGroup = oHeader.map(PacketHeader::getGroupId);

		//Check Control Group
		if(oPacketGroup.filter(gid->gid == PacketImp.GROUP_ID_CONTROL).isPresent()){

			final Optional<Payload> oPayload = oPacket.map(Packet::getPayloads).map(List::stream).flatMap(Stream::findAny);
			byte linkAddr = oPacket.map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0);

			oPacketId.filter(pid->pid==PacketWork.PACKET_ID_MODULE_LIST).flatMap(pid->oPayload.map(Payload::getBuffer)).ifPresent(showModuleButtons(linkAddr));
			return;
		}

		final boolean isDeviceInfo = oPacketGroup.map(gid->gid == PacketImp.GROUP_ID_DEVICE_INFO).orElse(false);

		//Return if not device info packet
		if(!isDeviceInfo)
			return;

		oPacket
		.map(DeviceInfo::new)
		.ifPresent(di->{
//			logger.error(di);


			Optional<DeviceType> oDeviceType = di.getDeviceType();
			oDeviceType
			.ifPresent(dt->{

				if(dt==DeviceType.IMPOSSIBLE)
					logger.warn("Can not connect. {}", packet);

				else{
					protocol = dt.PROTOCOL;

						//set retransmits to 0 times
					if(protocol==Protocol.LINKED ){
						oPacket
						.filter(p->setRetransmit)
						.filter(PacketAbstract.class::isInstance)
						.map(PacketAbstract.class::cast)
						.map(PacketAbstract::getLinkHeader)
						.map(LinkHeader::getAddr)
						.filter(addr->addr!=0)
						.ifPresent(addr->comPortThreadQueue.add(new RetransmitPacket(addr, (byte) 0)));

						if(moduleList==null)
							oPacketToSend = Optional.of(new ModuleListPacket(address));
					}
				}

				try{
					panelController.control(di);
				}catch(Exception e){
					logger.catching(e);
				}
			});
			if(!oDeviceType.isPresent()){

					final int typeId = di.getTypeId();
					JOptionPane.showMessageDialog(headPanel, "The Device is not Supported.(device Id=" + typeId + ")");
					logger.warn("The Device is not Supported.(device Id={})", typeId);
			}
		});
	}

	private Consumer<? super byte[]> showModuleButtons(byte linkAddr) {
		return bytes->{

			oPacketToSend = Optional.empty();
			gui.getModuleSelectFxPanel().orElseGet(
										()->{
											ModuleSelectFxPanel msf = new ModuleSelectFxPanel(linkAddr, bytes, p->{ panelController.removeAll(); comPortThreadQueue.add(p);});
											gui.setModuleSelectFxPanel(msf);
											return msf;
										});
		};//TODO
	}

	public void run() {
		try {

			final SerialPortInterface serialPort = ComPortThreadQueue.getSerialPort();
			Optional.ofNullable(serialPort).map(SerialPortInterface::isOpened).ifPresent(sp->getInfo());

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	protected abstract void getInfo();

	// ***********************************************************************
	protected class VCLC extends ValueChangeListenerClass {

		@Override
		public void fireValueChangeListener(ValueChangeEvent valueChangeEvent) {
			super.fireValueChangeListener(valueChangeEvent);
		}
	}

	//************************************ class PanelWorker **********************************************
	// Add Panels to the 'unitsPanel'
	private class PanelController{
		private Map<DeviceInfo, Timer> timers = new HashMap<>();

		public void control(DeviceInfo di) {

			final Timer timer = timers.get(di);

			if(timer!=null)
				timer.restart();
			else
				createPanel(di);
		}

		public void removeAll() {

			if (unitsPanel != null) {

				timers.entrySet().parallelStream().map(ks->ks.getValue()).forEach(t->t.stop());
				timers.clear();

				unitsPanel.removeAll();
				unitsPanel.revalidate();
			}

		}

		private boolean demoPanelRemoved;
		private Timer createPanel(DeviceInfo deviceInfo) {

			setSysSerialNumber(deviceInfo);

			deviceInfos.add(deviceInfo); //TODO check where used and remove this line of code

			//Remove Demo Panel
			if(!demoPanelRemoved){
				unitsPanel.remove("DemoPanel");
				demoPanelRemoved = true;
			}

			Component unitPanel;
			if (protocol == Protocol.CONVERTER)
				unitPanel = getConverterPanel(deviceInfo);
			else
				unitPanel = getNewBiasPanel(deviceInfo, 0, 0, 0, 0, unitsPanel.getHeight());

			unitsPanel.add(unitPanel);

			final Timer t = new Timer((int) TimeUnit.SECONDS.toMillis(11), e->removePanel(deviceInfo));
			t.setRepeats(false);
			t.start();
			return timers.put(deviceInfo, t);
		}

		private void setSysSerialNumber(DeviceInfo deviceInfo) {
			deviceInfo.getSerialNumber()
			.ifPresent(sn->{
				final String collect = timers.entrySet().stream().map(t->t.getKey()).map(di->di.getSerialNumber().get()).collect(Collectors.joining("_"));

				if(!collect.isEmpty())
					sn += "_" + collect;

				DumpControllerFull.setSysSerialNumber(sn);
			});
		}

		private void removePanel(DeviceInfo deviceInfo) {

			unitsPanel.remove(deviceInfo.getLinkHeader());
			unitsPanel.revalidate();
			unitsPanel.getParent().getParent().repaint();
			Optional.ofNullable(timers.remove(deviceInfo)).ifPresent(t->t.stop());
			comPortThreadQueue.clear();
			setSysSerialNumber(deviceInfo);
			if(timers.isEmpty())
				gui.setModuleSelectFxPanel(null);
		}
	}

	private void reset() {
		panelController.removeAll();
		protocol = getDefaultProtocol();
		oPacketToSend = null;
		setRetransmit = true;
		gui.setModuleSelectFxPanel(null);
	}

	public void stop() {

		comPortThreadQueue.stop();
		Optional.ofNullable(scheduledFuture).filter(shf->!shf.isCancelled()).ifPresent(shf->shf.cancel(true));
		Optional.of(service).filter(serv->!serv.isShutdown()).ifPresent(serv->serv.shutdownNow());
		gui.setModuleSelectFxPanel(null);
	}
}
