package irt.tools.panel.subpanel.control;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import irt.controller.DefaultController;
import irt.controller.ValueRangeControllerAbstract;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo.DeviceType;
import irt.data.IdValueForComboBox;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.ValueDouble;
import irt.tools.CheckBox.SwitchBox;

@SuppressWarnings("serial")
public class ControlPanelPicobuc extends ControlPanelSSPA{

	private SwitchBox switchBox;
	private DefaultController alcEnableSetterController;

	private	ItemListener alcItemListener = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				if(alcEnableSetterController!=null && alcEnableSetterController.isRun()){
					logger.warn("ALC Eneble Controller Stoped");
					alcEnableSetterController.stop();
				}

				ConfigurationSetter configurationSetter = new ConfigurationSetter(linkHeader, PacketImp.PARAMETER_CONFIG_FCM_ALC_ENABLED, PacketWork.PACKET_ID_CONFIGURATION_ALC_ENABLE_COMAND){

					private int times;

					@Override
					public boolean set(Packet packet) {

						if(packet.getHeader().getPacketId() == PacketWork.PACKET_ID_CONFIGURATION_ALC_ENABLE_COMAND){

							if(packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE){
								Boolean enabled = packet.getPayload(0).getByte()==1;
								logger.trace(packet);
								fireValueChangeListener(new ValueChangeEvent(enabled, PacketWork.PACKET_ID_CONFIGURATION_ALC_ENABLE_COMAND));
							}else{
								logger.warn(packet);
								if(++times>=3)
									fireValueChangeListener(new ValueChangeEvent("error", PacketWork.PACKET_ID_CONFIGURATION_ALC_ENABLE_COMAND));
							}
						}

						return false;
					}
					
				};
				configurationSetter.preparePacketToSend(switchBox.isSelected() ? (byte)1 : (byte)0);
				alcEnableSetterController = new DefaultController(deviceType, "Command SET ALC", configurationSetter, Style.CHECK_ALWAYS){

					@Override
					protected ValueChangeListener addGetterValueChangeListener() {
						return new ValueChangeListener() {
							@Override
							public void valueChanged(ValueChangeEvent valueChangeEvent) {
								ControlPanelPicobuc.this.logger.trace(valueChangeEvent);									
								stop();
							}

						};
					}
					
				};
				startThread(alcEnableSetterController);
			}
		};
	private DefaultController alcEnableGetterController;
	private JLabel lblSave;

	public ControlPanelPicobuc(Optional<DeviceType> deviceType, LinkHeader linkHeader) {
		super(deviceType, linkHeader, deviceType.filter(dt->dt!=DeviceType.CONVERTER_L_TO_KU_OUTDOOR).map(dt->(short)ActionFlags.FLAG_FREQUENCY.ordinal()).orElse((short)ActionFlags.FLAG_ATTENUATION.ordinal()));
		
		Font font = Translation.getFont()
				.deriveFont(Translation.getValue(Float.class, "control.label.mute.font.size", 12f))
				.deriveFont(Font.BOLD);

		lblSave = new JLabel();
		lblSave.setHorizontalAlignment(SwingConstants.LEFT);
		lblSave.setForeground(Color.YELLOW);
		lblSave.setFont(font);
		int x = Translation.getValue(Integer.class, "control.label.save.x", 153);
		int y = Translation.getValue(Integer.class, "control.label.save.y", 107);
		int width = Translation.getValue(Integer.class, "control.label.save.width", 61);
		lblSave.setBounds(x, y, width, 20);
		add(lblSave);

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				startAlcEnableController();
			}
			public void ancestorMoved(AncestorEvent event) {
			}
			public void ancestorRemoved(AncestorEvent event) {
				if(alcEnableGetterController!=null)
					alcEnableGetterController.stop();
			}
		});

		deviceType
		.filter(dt->dt==DeviceType.CONVERTER_L_TO_KU_OUTDOOR)
		.ifPresent(
				dt->{
			
					Image imageOn = new ImageIcon(ControlPanelDownConverter.class.getResource("/irt/irt_gui/images/switch1.png")).getImage();
					Image imageOff = new ImageIcon(ControlPanelDownConverter.class.getResource("/irt/irt_gui/images/switch2.png")).getImage();
					switchBox = new SwitchBox(imageOff, imageOn);
					switchBox.addItemListener(alcItemListener);
					switchBox.setName("ALC");
					switchBox.setBounds(128, 101, 27, 33);
					switchBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
					add(switchBox);

					lblSave.setText("ALC");
					lblSave.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							switchBox.setSelected(!switchBox.isSelected());
						}
					});

					cbActionSelector.addItem(new IdValueForComboBox((short) ActionFlags.FLAG_ALC.ordinal(), "ALC"));

					startAlcEnableController();
				});
	}

	private void startAlcEnableController() {
		DefaultController alcEnableController = createAlcEnableController();
		alcEnableController.setWaitTime(5000);
		startThread( alcEnableController);
	}

	private DefaultController createAlcEnableController() {
		return alcEnableGetterController = new DefaultController(
				deviceType,
				"ALC",
				new Getter(
						linkHeader,
						PacketImp.GROUP_ID_CONFIGURATION,
						PacketImp.PARAMETER_CONFIG_FCM_ALC_ENABLED,
						PacketWork.PACKET_ID_CONFIGURATION_ALC_ENABLE){

					@Override
					public boolean set(Packet packet) {
						switch(packet.getHeader().getPacketId()){
						case PacketWork.PACKET_ID_CONFIGURATION_ALC_ENABLE:
							boolean isOn = packet.getPayload(0).getByte()==1;
							if(switchBox!=null && isOn != switchBox.isSelected()){
								switchBox.removeItemListener(alcItemListener);
								switchBox.setSelected(isOn);
								switchBox.addItemListener(alcItemListener);
								ControlPanelPicobuc.this.logger.entry(packet);
							}
						}
						return false;
					}
				},
				Style.CHECK_ALWAYS);
	}

	private void startThread(DefaultController controller) {
		Thread t = new Thread(controller);
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(--priority);
		t.setDaemon(true);
		t.start();
	}

	@Override
	protected ControllerAbstract getNewAlcController() {
		logger.entry();

		Getter alcRangeGetter = new Getter(
				linkHeader,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_FCM_ALC_RANGE,
				PacketWork.PACKET_ID_CONFIGURATION_ALC_RANGE, logger){

					@Override
					public boolean set(Packet packet) {
						if(packet.getHeader().getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_ALC_RANGE){
							logger.debug(packet);
							Payload payload = packet.getPayload(0);
							if(payload!=null)
								fireValueChangeListener(new ValueChangeEvent(new Range(payload), PacketWork.PACKET_ID_CONFIGURATION_ALC_RANGE));
						}
						return false;
					}
			
		};
		return new ValueRangeControllerAbstract(
				deviceType,
				"ALC Controller",
				alcRangeGetter,
				txtGain,
				slider,
				txtStep,
				Style.CHECK_ALWAYS){

					@Override
					protected ValueChangeListener addGetterValueChangeListener() {
						return new ValueChangeListener() {
							
							@Override
							public void valueChanged(ValueChangeEvent valueChangeEvent) {
								if(valueChangeEvent.getID()==PacketWork.PACKET_ID_CONFIGURATION_ALC_RANGE && valueChangeEvent.getSource() instanceof Range){
									logger.debug(valueChangeEvent);
									String prefix = Translation.getValue(String.class, "dbm", " dBm");

									Range r = (Range)valueChangeEvent.getSource();

									long minimum = r.getMinimum();
									long maximum = r.getMaximum();
									ValueDouble stepValue = new ValueDouble(1, 1, maximum-minimum, 1);
									stepValue.setPrefix(prefix);
									setStepValue(stepValue);

									ValueDouble value = new ValueDouble(0, minimum, maximum, 1);
									value.setPrefix(prefix);
									startTextSliderController(ControlPanelPicobuc.this.getName(), value, PacketWork.PACKET_ID_CONFIGURATION_ALC_LEVEL, PacketImp.PARAMETER_CONFIG_FCM_ALC_LEVEL, style);
								}
							}
						};
					}
			
		};
	}
}
