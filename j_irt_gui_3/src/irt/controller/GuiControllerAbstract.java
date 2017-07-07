package irt.controller;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.serial_port.ComPort;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.value.getter.DeviceInfoGetter;
import irt.controller.serial_port.value.getter.ValueChangeListenerClass;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.MyThreadFactory;
import irt.data.DeviceInfo.DeviceType;
import irt.data.DeviceInfo.Protocol;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketAbstract.Priority;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.LinkedPacket;
import irt.irt_gui.IrtGui;
import irt.tools.KeyValue;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.head.Console;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.UnitsContainer;
import irt.tools.textField.UnitAddressField;
import jssc.SerialPortList;

public abstract class GuiControllerAbstract implements Runnable, PacketListener{

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
	protected volatile UnitsContainer unitsPanel;
	protected JComboBox<String> serialPortSelection;
	protected JComboBox<KeyValue<String, String>> languageComboBox;
	protected HeadPanel headPanel;

	protected VCLC vclc =  new VCLC();
	protected Protocol protocol = getDefaultProtocol();

	private final PanelController panelController = new PanelController();

	private static Set<DeviceInfo>  deviceInfos = new HashSet<>();

	private byte address;

	@SuppressWarnings("unchecked")
	public GuiControllerAbstract(String threadName, IrtGui gui) {

		guiController = this;

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

		if(scheduledFuture!=null && !scheduledFuture.isCancelled())
			scheduledFuture.cancel(true);

		scheduledFuture = service.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
	}

	private void reset() {
		panelController.removeAll();
		comPortThreadQueue.clear();
		protocol = getDefaultProtocol();
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

		return Optional
				.ofNullable(serialPortSelection)
				.map(sps->sps.getSelectedItem())
				.filter(si->si!=null)
				.map(Object::toString)
				.filter(ComPortThreadQueue.getSerialPort().getPortName()::equals)
				.map(spName->true)
				.orElse(false);
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
		return Optional.of(packet).filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).orElse(new LinkHeader((byte)0, (byte)0, (short) 0));
	}

	public Protocol getDefaultProtocol() {
		return Protocol.ALL;
	}

	@Override
	public void onPacketRecived(Packet packet) {
		logger.trace(packet);

		final Optional<Packet> isDeviceInfo = Optional
													.ofNullable(packet)
													.map(p->p.getHeader())
													.filter(h->h.getGroupId() == PacketImp.GROUP_ID_DEVICE_INFO)
													.map(h->packet);

		//Return if not device info packet
		if(!isDeviceInfo.isPresent())
			return;

		final Optional<Packet> hasAnswer = isDeviceInfo.filter(p->p.getHeader().getPacketType() == PacketImp.PACKET_TYPE_RESPONSE);

		//return if no answer
		if(!hasAnswer.isPresent())
			return;

		hasAnswer
		.map(DeviceInfo::new)
		.ifPresent(di->{


			Optional<DeviceType> oDeviceType = di.getDeviceType();
			oDeviceType.ifPresent(dt->{

				if(dt==DeviceType.IMPOSSIBLE)
					logger.warn("Can not connect. {}", packet);

				else
					protocol = dt.PROTOCOL;

				panelController.control(di);
			});
			if(!oDeviceType.isPresent()){

					final int typeId = di.getTypeId();
					JOptionPane.showMessageDialog(headPanel, "The Device is not Supported.(device Id=" + typeId + ")");
					logger.warn("The Device is not Supported.(device Id={})", typeId);
			}

		});
	}

	public void run() {
		try {

			if(isSerialPortSet())
				getInfo();

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
			t.start();
			return timers.put(deviceInfo, t);
		}

		private void setSysSerialNumber(DeviceInfo deviceInfo) {
			deviceInfo.getSerialNumber()
			.ifPresent(sn->{
				final String collect = timers.entrySet().stream().map(t->t.getKey()).map(di->di.getSerialNumber().get()).collect(Collectors.joining("_"));

				if(!collect.isEmpty())
					sn += "_" + collect;

				DumpController.setSysSerialNumber(sn);
			});
		}

		private void removePanel(DeviceInfo di) {

			unitsPanel.remove(di.getLinkHeader());
			unitsPanel.revalidate();
			unitsPanel.getParent().getParent().repaint();
			Optional.ofNullable(timers.remove(di)).ifPresent(t->t.stop());
			comPortThreadQueue.clear();
		}
	}
}
