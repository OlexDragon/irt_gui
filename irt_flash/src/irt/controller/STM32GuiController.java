package irt.controller;

import irt.controller.serial_port.ComPort;
import irt.data.Listeners;
import irt.data.MicrocontrollerSTM32;
import irt.data.Unit;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.value.StaticComponents;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.filechooser.FileNameExtensionFilter;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class STM32GuiController extends GuiControllerAbstract{

	private MicrocontrollerSTM32 microcontrollerSTM32;
	private Unit unit = new Unit();

	private JPanel workPanel;
	private JComboBox<String> serialPortSelection;
	private JButton btnConnect;
	private JLabel label;
	private JTextArea textArea;

	private JButton btnRead;
	private JButton btnWrite;
	private JButton btnOpen;
	private JButton btnUpload;
	private JLabel lblFileName;
	private JButton btnSaveFile;

	@SuppressWarnings("unchecked")
	public STM32GuiController(JFrame gui) {
		super("STM32GuiController", gui);

		ComPort serialPort = new ComPort(prefs.get(SERIAL_PORT, "COM1"));
		comPortThreadQueue.setSerialPort(serialPort);
		microcontrollerSTM32 = new MicrocontrollerSTM32(serialPort);

		JPanel contentPane = (JPanel) gui.getContentPane();
		Component[] components = contentPane.getComponents();
		for(Component c:components)
			switch(c.getClass().getSimpleName()){
			case "MainPanel":
				JPanel headPanel = (JPanel) c;
				Component[] cs = headPanel.getComponents();
				for(Component hc:cs)
					if(hc instanceof JComboBox)
						setComboBox((JComboBox<String>)hc);
					else
						setButtons(hc);
						
				break;
			case "JPanel":
				workPanel = (JPanel) c;
				cs = workPanel.getComponents();
				for(Component wc:cs)
					switch(wc.getClass().getSimpleName()){
					case "JScrollPane":
						JScrollPane sp = (JScrollPane)wc;
						Component[] spcs = sp.getComponents();
						for(Component cpc:spcs)
							switch(cpc.getClass().getSimpleName()){
							case "JViewport":
								textArea = (JTextArea) ((JViewport)cpc).getView();
								break;
//							default:
//								System.out.println(cpc.getClass().getSimpleName());
							}
						break;
					case "ImageButton":
						setButtons(wc);
						break;
					case "JLabel":
						String name = wc.getName();
						if(name!=null && name.equals("File Name"))
							lblFileName = (JLabel)wc;
					}
				break;
			case "JLabel":
					label = (JLabel) c;
				break;
			case "JComboBox":
				setComboBox((JComboBox<String>)c);
				try {
					if(!serialPort.getPortName().startsWith("Select")){
						serialPort.openPort();
						serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);
					}
				} catch (SerialPortException e) {
					JOptionPane.showMessageDialog(null, e.getLocalizedMessage()	+ ";(E001)");
//					e.printStackTrace();
				}
				break;
			case "JProgressBar":
				microcontrollerSTM32.setProgressBar((JProgressBar)c);
			}

		microcontrollerSTM32.addVlueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				switch(valueChangeEvent.getID()){

				case MicrocontrollerSTM32.MESSAGE:
					label.setForeground(Color.YELLOW);
					label.setText(valueChangeEvent.getSource().toString());
					StaticComponents.getLedRx().setLedColor(Color.YELLOW);
//					System.out.println("* "+valueChangeEvent.getSource());
					break;

				case MicrocontrollerSTM32.ERROR:
					label.setForeground(new Color(0xF6CEF5));
					label.setText("~ "+valueChangeEvent.getSource().toString());
					StaticComponents.getLedRx().setLedColor(Color.RED);
					break;

				case MicrocontrollerSTM32.READ:
					StaticComponents.getLedRx().setLedColor(Color.GREEN);
					try {

						unit.setFlashStr(valueChangeEvent.getSource().toString());
						Map<String, String> variables = unit.getVariables();
						textArea.setText("");
						for(String s:variables.keySet()){
							String string = variables.get(s);
							textArea.append(s+" "+string+"\n");
						}

						List<String[]> tables = unit.getTables();
						for(String[] s:tables)
							textArea.append(s[0]+" "+s[1]+"\n");

					} catch (UnsupportedEncodingException e) {
						JOptionPane.showMessageDialog(null, e.getLocalizedMessage()	+ ";(E001)");
						e.printStackTrace();
					}
					break;
				case MicrocontrollerSTM32.VERSION:
					unit.setBootloaderVersion((Byte)valueChangeEvent.getSource());
					StaticComponents.getLedRx().setLedColor(Color.GREEN);
				}

				StaticComponents.getLedRx().blink();
			}
		});

		Thread t = new Thread(microcontrollerSTM32);
		t.setPriority(t.getPriority()-1);
		t.start();
	}

	protected void setButtons(Component hc) {
		String name = hc.getName();
		if(name!=null)
		switch(name){
		case "Connect":
			setConnectButton(hc);
			break;
		case "Read":
			setReadButton(hc);
			break;
		case "Write":
			setWriteButton(hc);
			break;
		case "Open":
			setOpenButton(hc);
			break;
		case "Save File":
			setSaveFileButton(hc);
			break;
		case "Upload"://Upload program 
			setUploadButton(hc);
		}
	}

	private void setUploadButton(Component hc) {
		btnUpload = (JButton)hc;
		btnUpload.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(unit.getProgram()==null)
					label.setText("Select the File");
				else{
					if(comPortThreadQueue.getSerialPort()!=microcontrollerSTM32.getComPort())
						microcontrollerSTM32.setComPort(comPortThreadQueue.getSerialPort());

					label.setText("Writing The Program");

					microcontrollerSTM32.writeToFlash(unit.getProgram());
				}
			}
		});
	}

	private void setSaveFileButton(Component hc) {
		btnSaveFile = (JButton)hc;
		btnSaveFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!textArea.getText().isEmpty()){

					JFileChooser fc = new JFileChooser();
					fc.setMultiSelectionEnabled(false);
					Path p = Paths.get(prefs.get("path", ""));
					p.getParent();

					FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter("Bin files(bin)", "bin");
					fc.addChoosableFileFilter(fileNameExtensionFilter);
					fc.setFileFilter(fileNameExtensionFilter);

					fc.setSelectedFile(p.getParent().resolve(unit.getSerialNumber()+".bin").toFile());
					if(fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION ){

						try {
							BufferedWriter out = new BufferedWriter(new FileWriter(fc.getSelectedFile()));
							textArea.write(out);
							out.flush();
							out.close();
						} catch(IOException e) {
							label.setText("There was an error saving your file.");
}
					}
				}else
					label.setText("The Profile was not read from the Unit.");
			}
		});
	}

	private void setOpenButton(Component hc) {
		btnOpen = (JButton)hc;
		btnOpen.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				JFileChooser fc = new JFileChooser();
				fc.setMultiSelectionEnabled(false);
				String path = prefs.get("path", "");

				FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter("Bin files(bin)", "bin");
				fc.addChoosableFileFilter(fileNameExtensionFilter);
				fc.setFileFilter(fileNameExtensionFilter);

				fc.setSelectedFile(new File(path));

				int returnVal = fc.showOpenDialog(null);
				File file = fc.getSelectedFile();

				String newPath = file.getPath();
				if(!newPath.equals(path))
					prefs.put("path", newPath);

				int modifiers = e.getModifiers();
				if (returnVal == JFileChooser.APPROVE_OPTION){
					lblFileName.setText(file.getName());
					if((modifiers&InputEvent.CTRL_MASK)==0){
						try {

							FileInputStream fileInputStream = new FileInputStream(file);
							byte fileContent[] = new byte[(int)file.length()];
							fileInputStream.read(fileContent);
							fileInputStream.close();
							unit.setProgram(fileContent);

						} catch (IOException e1) {
							e1.printStackTrace();
						};
					}else
						try {
							Scanner scanner = new Scanner(file);
							while(scanner.hasNextLine())
								textArea.append(scanner.nextLine()+"\n");
							scanner.close();

							unit.setFlashStr(textArea.getText());
							textArea.setText(unit.toString());

						} catch (FileNotFoundException | UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}			
				}
			}
		});
	}

	protected void setWriteButton(Component hc) {
		btnWrite = (JButton)hc;
		btnWrite.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(comPortThreadQueue.getSerialPort()!=microcontrollerSTM32.getComPort())
					microcontrollerSTM32.setComPort(comPortThreadQueue.getSerialPort());

				label.setText("Writing to the Flash Memory");

				try {
					unit.setFlashStr(textArea.getText());
					microcontrollerSTM32.writeToFlash(unit);
				} catch (UnsupportedEncodingException e1) {
					JOptionPane.showMessageDialog(null, e1.getLocalizedMessage()	+ ";(E001)");
					e1.printStackTrace();
				}
			}
		});
	}

	protected void setReadButton(Component hc) {
		btnRead = (JButton)hc;
		btnRead.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(comPortThreadQueue.getSerialPort()!=microcontrollerSTM32.getComPort())
					microcontrollerSTM32.setComPort(comPortThreadQueue.getSerialPort());

				label.setText("Reading the Flash Memory from the Unit");

				unit.clear();
				microcontrollerSTM32.setCommand(MicrocontrollerSTM32.READ);
			}
		});
	}

	protected void setConnectButton(Component hc) {
		btnConnect = (JButton)hc;
		btnConnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ComPort serialPort = comPortThreadQueue.getSerialPort();
				if(serialPort!=microcontrollerSTM32.getComPort())
					microcontrollerSTM32.setComPort(serialPort);

				if(serialPort!=null && serialPort.getPortName().startsWith("Select"))
					JOptionPane.showMessageDialog(null, "Select The Serial Port");
				else
					microcontrollerSTM32.setCommand(MicrocontrollerSTM32.CONNECT);
			}
		});
	}

	private void setComboBox(JComboBox<String> c) {
		String name = c.getName();
		if(name==null){
			serialPortSelection = c;
			DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<String>(SerialPortList.getPortNames());
			defaultComboBoxModel.insertElementAt("Select Serial Port", 0);
			serialPortSelection.setModel(defaultComboBoxModel);

			String portName = comPortThreadQueue.getSerialPort().getPortName();
			if(defaultComboBoxModel.getIndexOf(portName)==-1)
				if(defaultComboBoxModel.getSize()>0){
					comPortThreadQueue.setSerialPort(new ComPort(serialPortSelection.getSelectedItem().toString()));
					prefs.put(SERIAL_PORT,	comPortThreadQueue.getSerialPort().getPortName());
				}else{
					if(comPortThreadQueue.getSerialPort()!=null)
						try {
							comPortThreadQueue.getSerialPort().closePort();
						} catch (SerialPortException e1) {
							e1.printStackTrace();
						}
					comPortThreadQueue.setSerialPort(null);
				}
			else
				serialPortSelection.setSelectedItem(portName);

			serialPortSelection.addPopupMenuListener(Listeners.popupMenuListener);
			serialPortSelection.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange()==ItemEvent.SELECTED){
						String string = serialPortSelection.getSelectedItem().toString();
						STM32GuiController.this.setSerialPort(string);
						if(string.startsWith("Select"))
						label.setText("Select The Serial Port");
						else
							label.setText("For the connection, click CONNECT button");
					}
				}
			});
		}else{
			microcontrollerSTM32.setAddressIndex(c.getSelectedIndex());
			c.addItemListener(new ItemListener() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange()==ItemEvent.SELECTED)
						microcontrollerSTM32.setAddressIndex(((JComboBox<String>)itemEvent.getSource()).getSelectedIndex());
				}
			});
		}
	}

	protected void setSerialPort(String serialPortName) {
		try {
			comPortThreadQueue.setSerialPort(new ComPort(serialPortName));
			if(!comPortThreadQueue.getSerialPort().getPortName().startsWith("Select")){
				comPortThreadQueue.getSerialPort().openPort();
				label.setText("Press the Connect button");
			}else
				label.setText("Select The Serial Port");
			prefs.put(SERIAL_PORT,	serialPortName);
		} catch (SerialPortException e) {
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage()	+ ";(E001)");
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if(comPortThreadQueue.getSerialPort()!=null)
			comPortThreadQueue.getSerialPort().closePort();
		microcontrollerSTM32.setRunning(false);
	}
}
