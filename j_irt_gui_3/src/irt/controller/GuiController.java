package irt.controller;

import irt.controller.serial_port.ComPort;
import irt.controller.serial_port.value.Getter.DeviceInfoGetter;
import irt.controller.serial_port.value.Getter.ValueChangeListenerClass;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.irt_gui.IrtGui;
import irt.tools.KeyValue;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.head.Console;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.UnitsContainer;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import jssc.SerialPortList;

public class GuiController extends GuiControllerAbstract{

	public static final int CONNECTION = 1;

	protected UnitsContainer unitsPanel;
	private JComboBox<String> serialPortSelection;

	private VCLC vclc =  new VCLC();
	protected int protocol;

	private HeadPanel headPanel;

	private SoftReleaseChecker softReleaseChecker;

	private JComboBox<KeyValue<String, String>> languageComboBox;
	private static DumpControllers dumpControllers;

//************************************************************************************************
	@SuppressWarnings("unchecked")
	public GuiController(String name, JFrame gui) {
		super(name, gui);

		comPortThreadQueue.setSerialPort(new ComPort(prefs.get(SERIAL_PORT, "COM1")));

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
				headPanel = (HeadPanel)c;
				Component[] cms = headPanel.getComponents();
				for(Component cm:cms){
					String n = cm.getName();
					if(n!=null && n.equals("Language"))
						setComboBox(cm);
				}
					
			}

		comPortThreadQueue.addPacketListener(new PacketListener() {

			private RemoveComponent remover = new RemoveComponent(8000);

			@Override
			public void packetRecived(Packet packet) {

				if (packet != null && packet.getHeader()!=null) {
					ComPort serialPort = comPortThreadQueue.getSerialPort();
					DeviceInfo di = null;
					switch (packet.getHeader().getGroupId()) {
					case Packet.IRT_SLCP_PACKET_ID_DEVICE_INFO:
						DevicePanel unitPanel = null;
						di = new DeviceInfo(packet);

						switch(di.getType()){
						case DeviceInfo.DEVICE_TYPE_L_TO_70:
						case DeviceInfo.DEVICE_TYPE_L_TO_140:
						case DeviceInfo.DEVICE_TYPE_70_TO_L:
						case DeviceInfo.DEVICE_TYPE_140_TO_L:
						case DeviceInfo.DEVICE_TYPE_L_TO_KU:
						case DeviceInfo.DEVICE_TYPE_L_TO_C:
							unitPanel = new ConverterPanel(di, 0, 0, 0, 0, unitsPanel.getHeight());
							protocol = CONVERTER;
							if(softReleaseChecker==null)
								softReleaseChecker = new SoftReleaseChecker();
							break;
						case DeviceInfo.DEVICE_TYPE_BAIS_BOARD:
						case DeviceInfo.DEVICE_TYPE_PICOBUC_KU:
						case DeviceInfo.DEVICE_PICOBUC_C:
							protocol = LINKED;
							unitPanel = getNewBaisPanel(((LinkedPacket)packet).getLinkHeader(), "("+di.getSerialNumber()+") "+di.getUnitName(), 0, 0, 0, 0, unitsPanel.getHeight());
							break;
						default:
							System.out.println("Device Type:"+packet.getHeader().getGroupId());
						}

						if(packet.getHeader().getType()==Packet.IRT_SLCP_PACKET_TYPE_RESPONSE){

							remover.setLinkHeader(packet instanceof LinkedPacket ? ((LinkedPacket)packet).getLinkHeader() : null);

							unitsPanel.remove("DemoPanel");

							if(unitPanel!=null && packet.getPayloads()!=null && unitsPanel.add(unitPanel)==unitPanel){

								if(softReleaseChecker!=null)
									softReleaseChecker.check(di);

								di.setInfoPanel(unitPanel.getInfoPanel());
								unitsPanel.revalidate();
								unitsPanel.repaint();
								if(headPanel!=null)
									unitPanel.addStatusChangeListener(headPanel.getStatusChangeListener());

								if(dumpControllers!=null)
									dumpControllers.stop();
								dumpControllers = new DumpControllers(unitsPanel, packet instanceof LinkedPacket ? ((LinkedPacket)packet).getLinkHeader() : null, di);
							}

						}else{
							remover.setPacketNotReceived();
							if(unitsPanel.getComponentCount()>0) {
								synchronized (this) {
									notify();
								}
							}
						}

						break;
					default:
						if(packet.getHeader().getType()==Packet.IRT_SLCP_PACKET_TYPE_REQUEST && serialPort.isRun()){
							synchronized (GuiController.this) {
								GuiController.this.notify();
							}

							saveToFile(di);
						}
						unitPanel = null;
//						System.out.println(packet);
					}

					if(unitsPanel.getComponentCount()>0 && unitsPanel.getComponent(DevicePanel.class)!=null)
						vclc.fireValueChangeListener(new ValueChangeEvent(new Boolean(true), CONNECTION));
					else
						vclc.fireValueChangeListener(new ValueChangeEvent(new Boolean(false), CONNECTION));
				}else
					vclc.fireValueChangeListener(new ValueChangeEvent(new Boolean(false), CONNECTION));
			}

			private void saveToFile(DeviceInfo di) {
				File f = new File("c:"+File.separator+"irt"+File.separator+"irt.log");
				
				File parentFile = f.getParentFile();

				try {
					if(!parentFile.exists())
				    	parentFile.mkdirs();
					else if(f.length()>1000000000)
						f.delete();

					String text = Console.getText();
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss;");
					Calendar cal = Calendar.getInstance();

					PrintWriter pw = new PrintWriter(new FileWriter(f, true));

					int length = text.length();
					if(length>0)
						pw.println(">>>SN:"+(di!=null ? di.getSerialNumber() : "???")+"; "+
								dateFormat.format(cal.getTime())+
								":Vertion:"+IrtGui.VERTION+">>>\n\r"+
								(length>1000 ? text.substring(length-1000, length) : text));
					pw.close();
				} catch (IOException e) {
					Console.appendLn(e.getLocalizedMessage(), "file error");
//								e.printStackTrace();
				}
			}
		});
	}

	protected boolean removePanel(LinkHeader linkHeader) {
		comPortThreadQueue.getSerialPort().setRun(false);
		boolean removed;
		if(removed = unitsPanel.remove(linkHeader)){
			unitsPanel.revalidate();
			unitsPanel.getParent().getParent().repaint();
		}

		return removed;
	}

	protected DevicePanel getNewBaisPanel(LinkHeader linkHeader, String text, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		if(softReleaseChecker==null)
			softReleaseChecker = new SoftReleaseChecker();
		return new PicobucPanel(linkHeader, text, minWidth, midWidth, maxWidth, minHeight, maxHeight);
	}

	@SuppressWarnings("unchecked")
	private void setComboBox(Component c) {
		String name = c.getName();
		if(name!=null)
			if(name.equals("Unit's Serial Port")){
				serialPortSelection = (JComboBox<String>) c;
				DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<String>(SerialPortList.getPortNames());
				defaultComboBoxModel.insertElementAt(Translation.getValue(String.class, "select_rerial_port", "Select Serial Port"), 0);
				serialPortSelection.setModel(defaultComboBoxModel);
				Font font = Translation.getFont();
				serialPortSelection.setFont(font);

				String portName = comPortThreadQueue.getSerialPort().getPortName();
				if(defaultComboBoxModel.getIndexOf(portName)==-1){
					if(defaultComboBoxModel.getSize()>1)
						setSerialPort(serialPortSelection.getSelectedItem().toString());
				}else
					serialPortSelection.setSelectedItem(portName);

				serialPortSelection.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent itemEvent) {
						if(itemEvent.getStateChange()==ItemEvent.SELECTED)
							setSerialPort(serialPortSelection.getSelectedItem().toString());
					}
				});
			}else if(name.equals("Language")){
				languageComboBox = (JComboBox<KeyValue<String, String>>) c;
				languageComboBox.addItemListener(new ItemListener() {

					@Override
					public void itemStateChanged(ItemEvent itemEvent) {
						if(itemEvent.getStateChange()==ItemEvent.SELECTED){
							Translation.setLocale(((KeyValue<String, String>)languageComboBox.getSelectedItem()).getKey());
							Font font = Translation.getFont();

							headPanel.refresh();
							if(unitsPanel!=null)
								unitsPanel.refresh();

							if(font!=null)
								serialPortSelection.setFont(font);

							DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<String>(SerialPortList.getPortNames());
							defaultComboBoxModel.insertElementAt(Translation.getValue(String.class, "select_rerial_port", "Select Serial Port"), 0);
							serialPortSelection.setModel(defaultComboBoxModel);
						}
					}
				});
			}
	}

	@Override
	public void run() {
		while(true){
			try {
				if(serialPortSelection!=null){
					Object selectedItem = serialPortSelection.getSelectedItem();
					LinkHeader linkHeader = new LinkHeader((byte)254, (byte)0, (short)0);
					if(selectedItem!=null && comPortThreadQueue.getSerialPort().getPortName().equals(selectedItem.toString())){
						if(protocol==ALL || protocol==CONVERTER)
							comPortThreadQueue.add(new DeviceInfoGetter(){ @Override public Integer getPriority() { return 10001; }});
						if(protocol==ALL || protocol==LINKED)
							comPortThreadQueue.add(new DeviceInfoGetter(linkHeader){ @Override public Integer getPriority() { return 10000; }});
					}
				}
				synchronized (this) {
					wait(5000);
				}
			} catch (InterruptedException e) {
			}
		}
	}

	protected void setSerialPort(String serialPortName) {

		if(dumpControllers!=null){
			dumpControllers.stop();
			dumpControllers = null;
		}

		if(serialPortName!=null && !serialPortName.isEmpty()){
			comPortThreadQueue.setSerialPort(new ComPort(serialPortName));
			prefs.put(SERIAL_PORT,	serialPortName);

			if(unitsPanel!=null){
				unitsPanel.removeAll();
				unitsPanel.revalidate();
				unitsPanel.getParent().getParent().repaint();
			}

			protocol = ALL;
		}

		synchronized (this) {
			notify();
		}
	}

	protected JComboBox<String> getSerialPortSelection() {
		return serialPortSelection;
	}

	public void addChangeListener(ValueChangeListener valueChangeListener){
		vclc.addVlueChangeListener(valueChangeListener);
	}

	//***********************************************************************
	private class VCLC extends ValueChangeListenerClass{

		@Override
		public void fireValueChangeListener(ValueChangeEvent valueChangeEvent) {
			super.fireValueChangeListener(valueChangeEvent);
		}
		
	}

	public static DumpControllers getDumpControllers() {
		return dumpControllers;
	}



//************************************************************************************************************
	public class RemoveComponent extends Thread {

		private int waitTime;
		private LinkHeader linkHeader;
		private boolean packetReceived;

		public RemoveComponent(int waitTime) {

			this.waitTime = waitTime;

			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			start();
		}

		public void setPacketNotReceived() {
			synchronized (this) {
				if(packetReceived){
					packetReceived = false;
					notify();
				}
			}
		}

		public void setLinkHeader(LinkHeader linkHeader) {
			this.linkHeader = linkHeader;
			synchronized (this) {
				if(!packetReceived){
					packetReceived = true;
					notify();
				}
			}
		}

		@Override
		public void run() {
			while(true){

				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) { }
				}
				if(packetReceived)
					continue;

				synchronized (this) {
					try {
						wait(waitTime);
					} catch (InterruptedException e) { }
				}
				if(packetReceived)
					continue;

				synchronized (this) {
					if(!packetReceived && removePanel(linkHeader)){
						protocol = ALL;
						if(dumpControllers!=null){
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
