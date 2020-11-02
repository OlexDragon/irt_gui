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

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.serial_port.ComPortJSerialComm;
import irt.controller.serial_port.ComPortJssc;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.SerialPortInterface;
import irt.controller.serial_port.value.getter.ValueChangeListenerClass;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.DeviceInfo.DeviceType;
import irt.data.DeviceInfo.Protocol;
import irt.data.ThreadWorker;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.DeviceInfoPacket;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
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
import javafx.application.Platform;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

public abstract class GuiControllerAbstract implements Runnable, PacketListener{
	private final static Logger logger = LogManager.getLogger();

	public static final String IRT_TECHNOLOGIES_INC = "IRT Technologies inc.";

	public static final String SERIAL_PORT_CLASS = "Serial Port CLASS";

	public static final int CONNECTION	= 1;
	public static final int ALARM		= 2;
	public static final int MUTE		= 3;

	protected static final String SERIAL_PORT = "serialPort";

	protected static Preferences prefs = Preferences.userRoot().node(IRT_TECHNOLOGIES_INC);

	protected static ComPortThreadQueue comPortThreadQueue = new ComPortThreadQueue();

	private ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService service;

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
	private final Map<JRadioButtonMenuItem, Class<? extends SerialPortInterface>> serialPortClasses = new HashMap<>();

	private IrtGui gui;
	protected volatile UnitsContainer unitsPanel;

	@SuppressWarnings("unchecked")
	public GuiControllerAbstract(String threadName, IrtGui gui) {
		logger.traceEntry(threadName);

		final String className = prefs.get(SERIAL_PORT_CLASS, ComPortJSerialComm.class.getSimpleName());
		serialPortClass = Stream.of(ComPortJssc.class, ComPortJSerialComm.class).filter(c->c.getSimpleName().equals(className)).findAny().orElse(ComPortJssc.class);

		this.gui = gui;
		guiController = this;

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
							if ((e.getModifiers() & InputEvent.CTRL_MASK) > 0)
								console.setVisible(!console.isVisible());
						}
						@Override public void mousePressed	(MouseEvent e) { }
						@Override public void mouseExited	(MouseEvent e) { }
						@Override public void mouseEntered	(MouseEvent e) { }
						@Override public void mouseClicked	(MouseEvent e) { }
					});
				}
			}
			comPortThreadQueue.addPacketListener(guiController);

			closedCorrectly(ComPortThreadQueue.GUI_CLOSED_CORRECTLY);
	}

	@SuppressWarnings("unchecked")
	private void closedCorrectly(String properyKey) {
		logger.traceEntry(properyKey);

		Platform.runLater(
				()->{
					// Check if this application close properly
					boolean guiBeenClosedProperly = prefs.getBoolean(properyKey, true);
					prefs.putBoolean(properyKey, false);

					String className = Optional.ofNullable(serialPortClass).map(Class::getSimpleName).orElse(ComPortJssc.class.getSimpleName());

					logger.debug("guiBeenClosedProperly: {}; className: {}", guiBeenClosedProperly, className);
					if(!guiBeenClosedProperly) {
						stop();

						ChoiceDialog<Class<? extends SerialPortInterface>> alert = new ChoiceDialog<>(serialPortClass, ComPortJssc.class, ComPortJSerialComm.class);
						alert.setTitle("The GUI was not closed properly.");
						alert.setHeaderText("Try to select a different serial port driver.");
						ComboBox<Class<?>> comboBox = (ComboBox<Class<?>>) alert.getDialogPane().lookup(".combo-box");

						comboBox.setConverter(
								new StringConverter<Class<?>>() {
						
									@Override public String toString(Class<?> clazz) { return clazz.getSimpleName().substring(7); }
									@Override public Class<?> fromString(String string) { return null; }
								});

						try {
							Optional<Class<? extends SerialPortInterface>> oClass = alert.showAndWait();

							start(1);

							if(oClass.isPresent()) {

								className = oClass.get().getSimpleName();
								prefs.put(SERIAL_PORT_CLASS, className);
							}
						}catch (IllegalStateException e) {
							logger.catching(Level.DEBUG, e);
						}
					}

					addMenuSelectPortDriver(className);
				});
	}

	private void restart(long period) {
		logger.info("restart({})", period);
		stop();
		start(period);
	}

	public static Optional<DeviceInfo> getDeviceInfo(LinkHeader linkHeader) {
		logger.traceEntry("{}", linkHeader);
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

	private boolean setRetransmit = true;
	private Object moduleList;

	@SuppressWarnings("unchecked")
	private void setComboBox(Component c) {

		String name = c.getName();
		if(name==null)
			return;

		if(name.equals("Unit's Serial Port")){

			serialPortSelection = (JComboBox<String>) c;

			DefaultComboBoxModel<String> defaultComboBoxModel = initSerialPortSelection();

				String portName = prefs.get(SERIAL_PORT, serialPortSelection.getItemAt(0));
				Optional.of(defaultComboBoxModel.getIndexOf(portName)).filter(i->i>0).ifPresent(i->serialPortSelection.setSelectedIndex(i));

				serialPortSelection.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent itemEvent) {

						if(itemEvent.getStateChange()==ItemEvent.DESELECTED)
							setSerialPort(null);
						else if(itemEvent.getStateChange()==ItemEvent.SELECTED) {
							final JComboBox<?> source = (JComboBox<?>) itemEvent.getSource();
							setSerialPort((String) source.getSelectedItem());
						}
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

		final String[] array = Optional.ofNullable(serialPortClass)
		.map(
				clazz->{
					try {

						return clazz.getDeclaredMethod("getPortNames");

					} catch (NoSuchMethodException | SecurityException e) {
						logger.catching(e);
					}
					return null;
				})
		.map(
				method->{
					try {

						method.setAccessible(true);
						return (List<?>)method.invoke(null);

					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						logger.catching(e);
					}
					return null;
				})
		.map(list->list.toArray(new String[0]))
		.orElseGet(()->ComPortJSerialComm.getPortNames().toArray(new String[0]));

		DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<String>(array);
		defaultComboBoxModel.insertElementAt(Translation.getValue(String.class, "select_serial_port", "Select Serial Port"), 0);
		serialPortSelection.setModel(defaultComboBoxModel);
		Dimension size = serialPortSelection.getSize();
		size.width = Translation.getValue(Integer.class, "serialPortSelection.width", 200);
		serialPortSelection.setSize(size);
		return defaultComboBoxModel;
	}

	private void addMenuSelectPortDriver(String className) {

		final JPopupMenu popup = Optional
								.ofNullable(serialPortSelection.getComponentPopupMenu())
								.orElseGet(
										()->{
											final JPopupMenu jPopupMenu = new JPopupMenu();
											serialPortSelection.setComponentPopupMenu(jPopupMenu);
											return jPopupMenu;
										});

		final JMenu menu = new JMenu("Serial Port Drivers");
		popup.add(menu);

		ActionListener miListener =
				e->{
					final Object source = e.getSource();
					serialPortClass = serialPortClasses.get(source);
					setSerialPort();
					prefs.put(SERIAL_PORT_CLASS, serialPortClass.getSimpleName());
				};

		final ButtonGroup buttonGroup = new ButtonGroup();

		JRadioButtonMenuItem mi = new JRadioButtonMenuItem();
		mi.setText("JSSC");
		mi.addActionListener(miListener);
		menu.add(mi);
		buttonGroup.add(mi);
		serialPortClasses.put(mi, ComPortJssc.class);

		mi = new JRadioButtonMenuItem();
		mi.setText("JSerialComm");
		mi.addActionListener(miListener);
		menu.add(mi);
		buttonGroup.add(mi);
		serialPortClasses.put(mi, ComPortJSerialComm.class);

		final Set<Entry<JRadioButtonMenuItem, Class<? extends SerialPortInterface>>> entrySet = serialPortClasses.entrySet();
		//set selected JRadioButtonMenuItem
		final Optional<Entry<JRadioButtonMenuItem, Class<? extends SerialPortInterface>>> oEntry = entrySet.parallelStream().filter(entry->entry.getValue().getSimpleName().equals(className)).findAny();

		oEntry.ifPresent(
				entry->{
					entry.getKey().setSelected(true);
					serialPortClass = entry.getValue();
				});

		if(!oEntry.isPresent()) {
			final Entry<JRadioButtonMenuItem, Class<? extends SerialPortInterface>> entry = entrySet.stream().findAny().get();
			entry.getKey().setSelected(true);
			serialPortClass = entry.getValue();
		}

		setSerialPort();
	}

	protected void setSerialPort() {
		logger.traceEntry();

		final String portName = serialPortSelection.getSelectedItem().toString();
		setSerialPort(portName);
	}

	protected synchronized void setSerialPort(String serialPortName) {
		logger.traceEntry(serialPortName);
		//Show Stack Trace
//		logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

		if (serialPortName == null || serialPortName.replaceAll("\\D", "").isEmpty()){
			comPortThreadQueue.setSerialPort(null);
			stop();
			reset();

		}else {

			final Optional<SerialPortInterface> oSerialPort = Optional.ofNullable(ComPortThreadQueue.getSerialPort()).filter(sp->sp.getPortName().equals(serialPortName)).filter(sp->serialPortClass.isInstance(sp));

			if(!oSerialPort.isPresent())

				new ThreadWorker(
						()->{
							try {

								final Constructor<? extends SerialPortInterface> constructor = serialPortClass.getConstructor(String.class);
								SerialPortInterface serialPort = constructor.newInstance(serialPortName);

								comPortThreadQueue.setSerialPort(serialPort);

								prefs.put(SERIAL_PORT, serialPortName);

							} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
								logger.catching(Level.DEBUG, e1);
								comPortThreadQueue.setSerialPort(null);
								Optional.ofNullable(serialPortSelection).ifPresent(sps->SwingUtilities.invokeLater(()->sps.setSelectedItem(0)));
							}

							reset();
							start(1);

						}, getClass().getSimpleName() + ".setSerialPort(String)" );

			else
				start(1);
		} 

		notify();
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
		logger.traceEntry();

		if(protocol.equals(Protocol.LINKED))
			return;

		new ThreadWorker(()->comPortThreadQueue.add(new DeviceInfoPacket(getAddress())), getClass().getSimpleName() + ".getConverterInfo()");
	}

	protected void getUnitsInfo() {
		logger.traceEntry();

		if (protocol.equals(Protocol.CONVERTER))
			return;

		new ThreadWorker(()->{

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

		final Optional<Packet> oPacket = Optional.of(packet);
		final Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);

		// Return if not response
		final boolean noAnswer = oHeader.map(PacketHeader::getPacketType).filter(pt->pt!=PacketImp.PACKET_TYPE_RESPONSE).isPresent();
		if(noAnswer)
			return;

		// Reset timer
		LinkHeader linkHeader = oPacket.filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).orElse(new LinkHeader((byte)0, (byte)0, (byte)0));
		panelController.resetTimer(linkHeader);

		// check retransmit packet
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
			.filter(PacketIDs.CONTROL_MODULE_LIST::match)
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

		//Return if not device info packet
		if(!isDeviceInfo)
			return;

		new ThreadWorker(()->{	

			logger.traceEntry("{}", packet);

			oPacket
			.map(DeviceInfo::new)
			.ifPresent(di->{
				logger.info(di);

				// Send Packet to get modules list
				if(moduleList==null) {
					ModuleListPacket modulListPacket = new ModuleListPacket(di.getAddr());
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

					new ThreadWorker(
							()->Optional
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
//		logger.error("");
		try {

			final SerialPortInterface serialPort = ComPortThreadQueue.getSerialPort();
			Optional.ofNullable(serialPort).filter(SerialPortInterface::isOpened).ifPresent(sp->getInfo());

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	protected abstract void getInfo();

	private void reset() {
		logger.info("reset()");
		panelController.removeAll();
		protocol = getDefaultProtocol();
		moduleList = null;
		setRetransmit = true;
		gui.setModuleSelectFxPanel(null);
	}

	public void start(long period) {
//		logger.error("period: {}", period);
		logger.traceEntry("period: {}", period);

		// do nothing if the job is not finished.

		if(!Optional.ofNullable(service).filter(s->!s.isShutdown() && !s.isTerminated()).isPresent())
			service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker(getClass().getSimpleName()));

		final Optional<?> oFuture = Optional.ofNullable(scheduledFuture).filter(s->!s.isCancelled() && !s.isDone());

		if(oFuture.isPresent()) {

			final long delay = scheduledFuture.getDelay(TimeUnit.SECONDS);
			if(delay!=period) {
				scheduledFuture.cancel(true);
				scheduledFuture = service.scheduleAtFixedRate(guiController, period<0 ? 0 : period, period<1 ? 1 : period, TimeUnit.SECONDS);
			}

		}else
			scheduledFuture = service.scheduleAtFixedRate(guiController, period<0 ? 0 : period, period<1 ? 1 : period, TimeUnit.SECONDS);

		comPortThreadQueue.start();
	}

	public void stop() {
//		logger.error("");
		logger.traceEntry();

		comPortThreadQueue.stop();
		Optional.ofNullable(scheduledFuture).filter(shf->!shf.isDone()).ifPresent(shf->shf.cancel(true));
		Optional.ofNullable(service).filter(serv->!serv.isShutdown()).ifPresent(serv->serv.shutdownNow());
		gui.setModuleSelectFxPanel(null);
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
				createPanel(di);
			}
		}

		public synchronized void resetTimer(LinkHeader linkHeader) {
			timers.entrySet().parallelStream().filter(e->e.getKey().getLinkHeader().equals(linkHeader)).findAny().map(Entry::getValue).ifPresent(Timer::restart);
		}

		public void removeAll() {

			if (!(unitsPanel == null || timers.isEmpty())) {

				timers.entrySet().parallelStream().map(ks->ks.getValue()).forEach(t->t.stop());
				timers.clear();

				logger.info("timers.isEmpty()-{}; Removed all panels from 'unitsPanel'. protocol: {}", timers.isEmpty(), protocol);


//				Optional.ofNullable(scheduledFuture).filter(f->!f.isDone()).ifPresent(f->f.cancel(true));
				SwingUtilities.invokeLater(
						()->{

							if(Optional.ofNullable(ComPortThreadQueue.getSerialPort()).filter(SerialPortInterface::isOpened).isPresent())
								restart(1);
							else
								stop();

							unitsPanel.removeAll();
							unitsPanel.revalidate();
							protocol =  getDefaultProtocol(); });
			}
		}

		private boolean demoPanelRemoved;
		private synchronized Timer createPanel(DeviceInfo deviceInfo) {
			logger.info(deviceInfo);

			//Show Stack Trace
//			logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

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
			start(8);

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

			if(timers.isEmpty())
				start(1);
		}
	}

	// ***********************************************************************
	protected class VCLC extends ValueChangeListenerClass {

		@Override
		public void fireValueChangeListener(ValueChangeEvent valueChangeEvent) {
			super.fireValueChangeListener(valueChangeEvent);
		}
	}
}
