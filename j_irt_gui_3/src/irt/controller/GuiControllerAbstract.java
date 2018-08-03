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
import java.util.Map.Entry;
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
import javax.swing.SwingUtilities;
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
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.Payload;
import irt.data.packet.RetransmitPacket;
import irt.data.packet.control.ModuleListPacket;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
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
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory(getClass().getSimpleName()));

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

						@Override public void mousePressed(MouseEvent e) { }
						@Override public void mouseExited(MouseEvent e) { }
						@Override public void mouseEntered(MouseEvent e) { }
						@Override public void mouseClicked(MouseEvent e) { }
					});
				}
			}
			comPortThreadQueue.addPacketListener(guiController);
//			restart(3);
	}

	private void restart(long period) {
		logger.info("restart({})", period);
		Optional.ofNullable(scheduledFuture).filter(f->!f.isDone()).ifPresent(f->f.cancel(true));
		comPortThreadQueue.clear();
		scheduledFuture = service.scheduleAtFixedRate(guiController, period<0?0:period, period<=0?1:period, TimeUnit.SECONDS);
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

		SwingUtilities.invokeLater(()->{

			menuItem.setText(text);
			menuItem.removeActionListener(toRemove);
			menuItem.addActionListener(toAdd);
		});
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

			new MyThreadFactory(()->{
				try {

					final Constructor<? extends SerialPortInterface> constructor = serialPortClass.getConstructor(String.class);

					SerialPortInterface serialPort = constructor.newInstance(serialPortName);
					comPortThreadQueue.setSerialPort(serialPort);

					prefs.put(SERIAL_PORT, serialPortName);

				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
//					logger.catching(e1);
					comPortThreadQueue.setSerialPort(null);
					Optional.ofNullable(serialPortSelection).ifPresent(sps->SwingUtilities.invokeLater(()->sps.setSelectedItem(0)));
				}

				reset();

			}, getClass().getSimpleName() + ".setSerialPort(String)" );
		} 

		synchronized (guiController) {
			notify();
		}

		restart(1);
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

		if(protocol.equals(Protocol.LINKED))
			return;

		logger.trace("protocol = {}", protocol);

		new MyThreadFactory(()->comPortThreadQueue.add(new DeviceInfoPacket(getAddress())), getClass().getSimpleName() + ".getConverterInfo()");
	}

	protected void getUnitsInfo() {
//		logger.error("getUnitsInfo; protocol: {};", protocol);

		if (protocol.equals(Protocol.CONVERTER))
			return;

		new MyThreadFactory(()->{

				for(Byte addr:addresses){
					comPortThreadQueue.add(new DeviceInfoPacket(addr));
				}
		}, getClass().getSimpleName() + ".getUnitsInfo()");
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
	public void onPacketReceived(Packet packet) {
//		logger.error(packet);

		final Optional<Packet> oPacket = Optional.of(packet);
		final Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);

		// Return if not response
		final boolean noAnswer = oHeader.map(PacketHeader::getPacketType).filter(pt->pt!=PacketImp.PACKET_TYPE_RESPONSE).isPresent();
		if(noAnswer)
			return;

		// Reset timer
		LinkHeader linkHeader = oPacket.filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).orElse(new LinkHeader((byte)0, (byte)0, (byte)0));
		panelController.resetTimer(linkHeader);

		//check retransmit packet
		final Optional<Short> oPacketId = oHeader.map(PacketHeader::getPacketId);
		final Boolean isRetransmitPacket = oPacketId.filter(PacketIDs.PROTO_RETRANSNIT::match).isPresent();
		if(isRetransmitPacket){
			logger.debug("received Retransmit Packet {}", packet);
			setRetransmit = false;
			return;
		}

		final Optional<Byte> oPacketGroup = oHeader.map(PacketHeader::getGroupId);

		//Check Redundancy Controller buttons
		if(oPacketGroup.filter(PacketGroupIDs.CONTROL::match).isPresent()){

			oPacketId
			.filter(PacketIDs.CONTRO_MODULE_LIST::match)
			.flatMap(
					pid->
					oPacket.map(Packet::getPayloads)
					.map(List::stream)
					.flatMap(Stream::findAny)
					.map(Payload::getBuffer))
			.ifPresent(
					showModuleButtons(
							oPacket
							.filter(LinkedPacket.class::isInstance)
							.map(LinkedPacket.class::cast)
							.map(LinkedPacket::getLinkHeader)
							.map(LinkHeader::getAddr)
							.orElse((byte) 0)));
			return;
		}

		final boolean isDeviceInfo = oPacketGroup.filter( PacketGroupIDs.DEVICE_INFO::match).isPresent();

		logger.info(packet);
		//Return if not device info packet
		if(!isDeviceInfo)
			return;

		new MyThreadFactory(()->{			

			oPacket
			.map(DeviceInfo::new)
			.ifPresent(di->{
				logger.info(di);

				if(moduleList==null) {
					ModuleListPacket modulListPacket = new ModuleListPacket(di.getLinkHeader().getAddr());
					comPortThreadQueue.add(modulListPacket);
				}

				Optional<DeviceType> oDeviceType = di.getDeviceType();
				oDeviceType
				.ifPresent(dt->{

					if(dt==DeviceType.IMPOSSIBLE)
						logger.warn("Can not connect. {}", packet);

					else{

						protocol = dt.PROTOCOL;
						logger.debug("protocol: {}", protocol);

						//set retransmits to 0 times
						if(protocol==Protocol.LINKED ){
							oPacket
							.filter(p->setRetransmit)
							.filter(PacketSuper.class::isInstance)
							.map(PacketSuper.class::cast)
							.map(PacketSuper::getLinkHeader)
							.map(LinkHeader::getAddr)
							.filter(addr->addr!=0)
							.ifPresent(addr->comPortThreadQueue.add(new RetransmitPacket(addr, (byte) 0)));
						}
					}

					try{
						panelController.control(di);
					}catch(Exception e){
						logger.catching(e);
					}
				});
				if(!oDeviceType.isPresent()){

					new MyThreadFactory(()->Optional
							.ofNullable(scheduledFuture)
							.filter(f->!f.isDone())
							.ifPresent(
									f->{
										f.cancel(true);

										final int typeId = di.getTypeId();
										JOptionPane.showMessageDialog(headPanel, "The Device is not Supported.(device Id=" + typeId + ")");
										logger.warn("The Device is not Supported.(device Id={})", typeId);

										restart(5);
									}), getClass().getSimpleName() + ".showMessageDialog");

				}
			});
		}, getClass().getSimpleName() + ".onPacketReceived");
	}

	private Consumer<? super byte[]> showModuleButtons(byte linkAddr) {
		return bytes->{

			if(!gui.getModuleSelectFxPanel().isPresent()){
				ModuleSelectFxPanel msf = new ModuleSelectFxPanel(linkAddr, bytes,
						p->{
							panelController.removeAll();
							synchronized (this) { try { wait(5); } catch (InterruptedException e) { }}
							comPortThreadQueue.add(p);
						});
				SwingUtilities.invokeLater(()->gui.setModuleSelectFxPanel(msf));
			}
		};
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

	// ***************************************************************************************************** //
	// 																										 //
	// 									 class PanelWorker 													 //
	// 																										 //
	// ***************************************************************************************************** //

	// Add Panels to the 'unitsPanel'
	private class PanelController{
		private Map<DeviceInfo, Timer> timers = new HashMap<>();

		public void control(DeviceInfo di) {

			final Timer timer = timers.get(di);

			if(timer!=null)
				timer.restart();
			else {
				restart(8);
				createPanel(di);
			}
		}

		public void resetTimer(LinkHeader linkHeader) {
			timers.entrySet().stream().filter(e->e.getKey().getLinkHeader().equals(linkHeader)).findAny().map(Entry::getValue).ifPresent(Timer::restart);
		}

		public void removeAll() {

			if (!(unitsPanel == null || timers.isEmpty())) {

				timers.entrySet().parallelStream().map(ks->ks.getValue()).forEach(t->t.stop());
				timers.clear();

				logger.info("timers.isEmpty()-{}; Removed all panels from 'unitsPanel'. protocol: {}", timers.isEmpty(), protocol);


//				Optional.ofNullable(scheduledFuture).filter(f->!f.isDone()).ifPresent(f->f.cancel(true));
				SwingUtilities.invokeLater(
						()->{
							restart(1);
							unitsPanel.removeAll();
							unitsPanel.revalidate();
							protocol =  getDefaultProtocol(); });
			}
		}

		private boolean demoPanelRemoved;
		private Timer createPanel(DeviceInfo deviceInfo) {
			logger.info(deviceInfo);

			setSysSerialNumber(deviceInfo);

			deviceInfos.add(deviceInfo); //TODO check where used and remove this line of code

			//Remove Demo Panel
			if(!demoPanelRemoved){
				unitsPanel.remove("irt.tools.panel.DemoPanel");
				demoPanelRemoved = true;
			}

			Component unitPanel;
			if (protocol == Protocol.CONVERTER)
				unitPanel = getConverterPanel(deviceInfo);
			else
				unitPanel = getNewBiasPanel(deviceInfo, 0, 0, 0, 0, unitsPanel.getHeight());

			unitsPanel.add(unitPanel);

			final Timer t = new Timer((int) TimeUnit.SECONDS.toMillis(5), e->removePanel(deviceInfo));
			t.setRepeats(false);
			t.start();
			return timers.put(deviceInfo, t);
		}

		private void setSysSerialNumber(DeviceInfo deviceInfo) {
			deviceInfo
			.getSerialNumber()
			.ifPresent(sn->{
				final String collect = timers.entrySet().stream().map(t->t.getKey()).map(di->di.getSerialNumber().get()).collect(Collectors.joining("_"));

				if(!collect.isEmpty())
					sn += "_" + collect;

				DumpControllerFull.setSysSerialNumber(sn);
			});
		}

		private void removePanel(DeviceInfo deviceInfo) {
			logger.info("removePanel{})", deviceInfo);

			SwingUtilities.invokeLater(
					()->{
						unitsPanel.remove(deviceInfo.getLinkHeader());
						unitsPanel.revalidate();});

			//			unitsPanel.getParent().getParent().repaint();
			Optional.ofNullable(timers.remove(deviceInfo)).ifPresent(t->t.stop());
			comPortThreadQueue.clear();
			setSysSerialNumber(deviceInfo);
			if(timers.isEmpty()) {
				reset();
				restart(1);
			}
		}
	}

	private void reset() {
		logger.info("reset()");
		panelController.removeAll();
		protocol = getDefaultProtocol();
		moduleList = null;
		setRetransmit = true;
		gui.setModuleSelectFxPanel(null);
	}

	public void stop() {

		comPortThreadQueue.stop();
		Optional.ofNullable(scheduledFuture).filter(shf->!shf.isDone()).ifPresent(shf->shf.cancel(true));
		Optional.of(service).filter(serv->!serv.isShutdown()).ifPresent(serv->serv.shutdownNow());
		gui.setModuleSelectFxPanel(null);
	}
}
