package irt.controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.controller.serial_port.value.setter.DeviceDebagSetter;
import irt.data.Listeners;
import irt.data.RegisterValue;
import irt.data.DeviceInfo.DeviceType;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ControllerFocusListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.Value;

public class DeviceDebugController extends ControllerAbstract {

	protected Value value;

	private FocusListener txtFocusListener;
	private FocusListener sliderFocusListener;
	private ActionListener txtActionListener;
	private KeyListener txtKeyListener;
	private PropertyChangeListener txtPropertyChangeListener;
	private ChangeListener sliderChangeListener;
	private ItemListener commandItemListener;
	private ItemListener parameterItemListener;

	private int addrToSave;

	private JTextField txtField;
	private JSlider slider;

	private JTextArea textArea;
	private JComboBox<String> cbCommand;
	private JComboBox<Integer> cbParameter;

/**
 * @param addrToSave if addrToSave < 0 save command deasn't work
 */
	public DeviceDebugController(Optional<DeviceType> deviceType, String controllerName, JTextField txtField, JSlider slider, Value value, PacketWork packetWork, int addrToSave, Style style) {
		super(deviceType, controllerName, packetWork, null, style);
		logger.entry(controllerName);

		this.addrToSave = addrToSave;
//		setWaitTime(1000);
		setListeners();
		this.txtField = txtField;
		if(slider!=null)
			txtField.addFocusListener(txtFocusListener);
		txtField.addActionListener(txtActionListener);
		txtField.addKeyListener(txtKeyListener);
		txtField.addPropertyChangeListener(txtPropertyChangeListener);
		this.slider = slider;
		this.value = value;
		if(slider!=null)
			slider.setMaximum((int) value.getRelativeMaxValue());
	}

	public DeviceDebugController(Optional<DeviceType> deviceType, String controllerName, PacketWork packetWork, JComboBox<String> cbCommand, JComboBox<Integer> cbParameter, JTextArea textArea) {
		super(deviceType, controllerName, packetWork, null, null);

		this.cbCommand = cbCommand;
		this.cbParameter = cbParameter;
		this.textArea = textArea;

		PacketThreadWorker packetThread = getPacketWork().getPacketThread();

		byte[] d = packetThread.getData();
		int l = d.length;

		int selectedItem = (int) cbParameter.getSelectedItem();
		byte[] b = PacketImp.toBytes(selectedItem);

		d[l-1] = (byte) b.length;
		d =  Arrays.copyOf(d, l+b.length);
		System.arraycopy(b, 0, d, l, b.length);
		packetThread.setData(d);

		cbCommand.addPopupMenuListener(Listeners.popupMenuListener);
		cbCommand.addItemListener(commandItemListener);
		cbParameter.addPopupMenuListener(Listeners.popupMenuListener);
		cbParameter.addItemListener(parameterItemListener);

	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				new ControllerWorker(DeviceDebugController.this.getName(), valueChangeEvent);
			}
		};
	}

	@Override
	protected void setListeners() {
		parameterItemListener = new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent itemEvent){
				if(itemEvent.getStateChange()==ItemEvent.SELECTED){
					PacketWork pw = getPacketWork();
					if(pw!=null){
						PacketThreadWorker pt = pw.getPacketThread();
						Payload pl = pt.getPacket().getPayload(0);
						pl.setBuffer(DeviceDebugController.this.cbParameter.getSelectedItem());
						pt.preparePacket();

						synchronized (DeviceDebugController.this) {	DeviceDebugController.this.notify(); }
					}
				}
			}
		};

		commandItemListener = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent itemEvent) {
				if(itemEvent.getStateChange()==ItemEvent.SELECTED){
					GetterAbstract ga = (GetterAbstract)getPacketWork();
					if(ga!=null){
						PacketThreadWorker pt = ga.getPacketThread();
						int code = DeviceDebugController.this.cbCommand.getSelectedIndex()+1;
						ga.setPacketParameterHeaderCode((byte)code);

						Payload pl = pt.getPacket().getPayload(0);
						if(code==PacketImp.PARAMETER_DEVICE_DEBAG_INFO)
							pl.setBuffer(null);
						else
							pl.setBuffer(DeviceDebugController.this.cbParameter.getSelectedItem());

						pt.preparePacket();

						synchronized (DeviceDebugController.this) {	DeviceDebugController.this.notify(); }
					}
				}
			}
		};

		txtPropertyChangeListener = new PropertyChangeListener() {

			private long oldValue;

			@Override
			public void propertyChange(PropertyChangeEvent pchl) {
				switch(pchl.getPropertyName()){
					case "background":

						Color c = (Color) pchl.getNewValue();
						PacketWork unitPacketWork = getPacketWork();
						if(unitPacketWork!=null){
							PacketThreadWorker unitPacketThread = unitPacketWork.getPacketThread();
							Packet up = unitPacketThread.getPacket();
							RegisterValue urv = (RegisterValue)unitPacketThread.getValue();
							Value uv = urv.getValue();

							if(c.equals(Color.WHITE)&& up!=null &&  up.getHeader().getPacketType()==PacketImp.PACKET_TYPE_COMMAND){

								if(addrToSave>=0 && oldValue!=uv.getValue()){
									int index = urv.getIndex();
									new DeviceDebagSaveController(
											deviceType, txtField,
											new DeviceDebagSetter(unitPacketThread.getLinkHeader(),
													index,
													addrToSave,
													(short) (((GetterAbstract)unitPacketWork).getPacketId()+1),
													PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE,
													0),
											Style.CHECK_ONCE);
								}
							} else {
								Value value = uv;
								if(value!=null)
									oldValue = value.getValue();
							}
						}
				}
			}
		};

		txtKeyListener = new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent keyEvent) {

				RegisterValue rv = (RegisterValue)getPacketWork().getPacketThread().getValue();
				if(rv!=null){
					Value v = rv.getValue();

					if(v!=null){

						switch(keyEvent.getExtendedKeyCode()){
						case KeyEvent.VK_UP:
							slider.setValue(slider.getValue()+(slider.getSnapToTicks() ? slider.getMinorTickSpacing() : 1));
							setValue(slider.getValue());
							setAll();
							break;
						case KeyEvent.VK_DOWN:
							slider.setValue(slider.getValue()-(slider.getSnapToTicks() ? slider.getMinorTickSpacing() : 1));
							setValue(slider.getValue());
							setAll();
						}
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		};

		txtFocusListener = new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {
				FocusListener[] focusListeners = slider.getFocusListeners();
				for(FocusListener fl:focusListeners)
					slider.removeFocusListener(fl);

				slider.addFocusListener(sliderFocusListener);
				slider.setValue(value.getRelativeValue());

				fireFocusListener(new ValueChangeEvent(txtField, 0));
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				PacketWork packetWork = getPacketWork();
				if(packetWork!=null){
					RegisterValue registerValue = (RegisterValue)packetWork.getPacketThread().getValue();
					if(registerValue!=null){
						Value value = registerValue.getValue();
						if(value!=null){
							String valueStr = value.toString();
							if(!txtField.getText().equals(valueStr)){
								txtField.setText(valueStr);
							}
						}
					}
				}
			}
		};

		sliderFocusListener = new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				if(slider!=null)
					slider.removeChangeListener(sliderChangeListener);
			}

			@Override
			public void focusGained(FocusEvent e) {
				if(slider!=null)
					slider.addChangeListener(sliderChangeListener);
			}
		};

		sliderChangeListener = new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				if (slider.isFocusOwner())
					if (!slider.getValueIsAdjusting()) {
						if(txtField.isEnabled()){
							txtField.requestFocusInWindow();
							setValue(slider.getValue());
						}else{
							txtField.setText(value.toString());
							slider.setValue(value.getRelativeValue());
						}
					} else {
						if(value!=null){
							Value v = value.getCopy();
							v.setRelativeValue(slider.getValue());
							txtField.setText(v.toString());
						}
					}
			}
		};

		txtActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					String s = DeviceDebugController.this.txtField.getText().replaceAll("\\D", "");
					int value;

					if (s.length() > 0) {
						value = Integer.parseInt(s);
					} else
						value = 0;

					DeviceDebugController.this.slider.setValue(value);
					setValue(DeviceDebugController.this.slider.getValue());
				} catch (Exception ex) {
					logger.catching(ex);
				}

			}
		};
	}

	private void setAll() {
		txtField.requestFocusInWindow();
		txtField.setText(value.toString());
		if(slider!=null)
			slider.setValue(value.getRelativeValue());
	}

	private void setValue(int value) {

		this.value.setRelativeValue(value);
		DeviceDebagSetter dds = (DeviceDebagSetter)getPacketWork();
		if(dds!=null){
			PacketThreadWorker packetThread = dds.getPacketThread();
			if(packetThread!=null){
				RegisterValue registerValue = (RegisterValue) packetThread.getValue();
				registerValue.setValue(this.value);
				dds.preparePacketToSend(registerValue);
				setSend(true);
			}else
				stop();
		}
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private EventListenerList focusListeners = new EventListenerList();

	public void addFocusListener(ControllerFocusListener focusListener) {
		focusListeners.add(ControllerFocusListener.class, focusListener);
	}

	public void removeFocusListener(ControllerFocusListener focusListener) {
		focusListeners.remove(ControllerFocusListener.class, focusListener);
	}

	protected void fireFocusListener(ValueChangeEvent focusEvent) {
		Object[] listeners = focusListeners.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			Object l = listeners[i];
			if (l == ControllerFocusListener.class)
				((ControllerFocusListener) listeners[i + 1]).focusGained(focusEvent);
			;
		}
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	protected void clear() {
		super.clear();
		if(txtField!=null){
			txtField.removeActionListener(txtActionListener);
			txtField.removeKeyListener(txtKeyListener);
			txtField.removeFocusListener(txtFocusListener);
			txtField.removePropertyChangeListener(txtPropertyChangeListener);
			txtField = null;
			txtActionListener = null;
			txtKeyListener = null;
			txtPropertyChangeListener = null;
			txtFocusListener = null;
		}
		if(slider!=null){
			slider.removeFocusListener(sliderFocusListener);
			slider.removeChangeListener(sliderChangeListener);
			slider = null;
			sliderChangeListener = null;
			sliderFocusListener = null;
		}
		value =null;

		textArea = null;

		if(cbCommand!=null){
			cbCommand.removePopupMenuListener(Listeners.popupMenuListener);
			cbCommand.removeItemListener(commandItemListener);
			cbCommand = null;
			commandItemListener = null;
		}
		if(cbParameter!=null){
			cbParameter.removePopupMenuListener(Listeners.popupMenuListener);
			cbParameter.removeItemListener(parameterItemListener);
			cbParameter = null;
			parameterItemListener = null;
		}
	}

	//********************* class ControllerWorker *****************
	private class ControllerWorker extends Thread {

		private ValueChangeEvent valueChangeEvent;

		public ControllerWorker(String threadName, ValueChangeEvent valueChangeEvent){
			super(threadName);
			logger.entry(threadName, valueChangeEvent);
			this.valueChangeEvent = valueChangeEvent;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			try {
				int id = valueChangeEvent.getID();

				GetterAbstract pw = (GetterAbstract) getPacketWork();
				PacketThreadWorker pt = pw.getPacketThread();
				if (id == pw.getPacketId()) {

					RegisterValue rv = (RegisterValue) pt.getValue();
					RegisterValue urv = rv;
					Object source = valueChangeEvent.getSource();

					switch (source.getClass().getSimpleName()) {
					case "Byte":
						logger.debug("DeviceDebagController.valueChanged: ERROR ={}", source);
						txtField.setText("error" + source);
						rv.setValue(null);
						pt.preparePacket(pw.getPacketParameterHeaderCode(), rv);
						setSend(true, false);
						break;
					case "RegisterValue":
						logger.debug("DeviceDebagController.valueChanged: {}", source);
						RegisterValue crv = (RegisterValue) source;
						if (rv.getAddr() == crv.getAddr() && rv.getIndex() == crv.getIndex()) {
							Value unitValue = urv.getValue();

							if (unitValue == null) {
								value.setValue(crv.getValue().getValue());
								urv.setValue(value);
							} else
								value.setValue(crv.getValue().getValue());

							setAll();
							if (style == Style.CHECK_ALWAYS) {
								logger.debug("DeviceDebagController.valueChanged: style==Style.CHECK_ALWAYS");
								RegisterValue tmpRV = new RegisterValue(rv);
								tmpRV.setValue(null);
								pt.preparePacket(pw.getPacketParameterHeaderCode(), tmpRV);
							}
						}
						break;
					default:
						if(textArea!=null)
							textArea.setText(source != null ? source.toString() : null);
					}
				}
			} catch (Exception ex) {
				logger.catching(ex);
			}
		}

	}
}
