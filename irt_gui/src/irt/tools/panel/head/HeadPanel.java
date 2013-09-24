package irt.tools.panel.head;

import irt.controller.GuiController;
import irt.controller.monitor.MonitorController;
import irt.data.Listeners;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.value.StaticComponents;
import irt.tools.label.LED;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.plaf.basic.BasicComboBoxUI;

import jssc.SerialPortList;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import resources.Tanslation;
import resources.tools.ValueLabel;

import javax.swing.UIManager;

@SuppressWarnings("serial")
public class HeadPanel extends MainPanel {

	private LED ledPowerOn;
	private LED ledMute;
	private LED ledAlarm;
	public static LED ledRx = StaticComponents.getLedRx();

	public HeadPanel(JFrame target) {
		super(target, 650);
		setSize(607, 74);
		Color color = new Color(0x3B, 0x4A, 0x8B);
		setBackground(color);
		setArcStart(-40);
		setArcStep(155);
		setArcWidth(80);

		ResourceBundle messages = Tanslation.messages;
		ledPowerOn = new LED(Color.GREEN, messages.getString("power_on"));
		ledPowerOn.setName("Power On");
		ledPowerOn.setForeground(new Color(176, 224, 230));
		ledPowerOn.setFont(UIManager.getFont("CheckBoxMenuItem.font"));
		ledPowerOn.setBounds(48, 22, 137, 30);
		add(ledPowerOn);

		ledAlarm = new LED(Color.RED, messages.getString("alarm"));
		ledAlarm.setName("Main Alarm");
		ledAlarm.setForeground(new Color(176, 224, 230));
		ledAlarm.setFont(UIManager.getFont("ToolTip.font"));
		ledAlarm.setBounds(207, 22, 108, 30);
		add(ledAlarm);

		ledMute = new LED(Color.YELLOW, messages.getString("mute"));
		ledMute.setName("Main Mute");
		ledMute.setForeground(new Color(176, 224, 230));
		ledMute.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledMute.setBounds(362, 22, 88, 30);
		add(ledMute);
		
		ledRx.setForeground(new Color(176, 224, 230));
		ledRx.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledRx.setBounds(10, 29, 17, 17);
		add(ledRx);
		
		ApplicationContext context =  new ClassPathXmlApplicationContext("translation.xml");
		@SuppressWarnings("unchecked")
		List<ValueLabel> languages = (ArrayList<ValueLabel>) context.getBean("languages");
		ValueLabel[] valueLabels = new ValueLabel[languages.size()];
		DefaultComboBoxModel<ValueLabel> defaultComboBoxModel = new DefaultComboBoxModel<ValueLabel>(languages.toArray(valueLabels));
		JComboBox<ValueLabel> comboBoxLanguage = new JComboBox<ValueLabel>();
		comboBoxLanguage.setName("Language");
		comboBoxLanguage.setFont(UIManager.getFont("CheckBoxMenuItem.acceleratorFont"));
		comboBoxLanguage.setModel(defaultComboBoxModel);
		comboBoxLanguage.addPopupMenuListener(Listeners.popupMenuListener);
		comboBoxLanguage.setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0;}};}});
		comboBoxLanguage.setForeground(Color.WHITE);
		comboBoxLanguage.setCursor(new Cursor(Cursor.HAND_CURSOR));
		comboBoxLanguage.setBackground(color.darker());
		comboBoxLanguage.setBounds(516, 54, 91, 17);
		comboBoxLanguage.setMinimumSize(new Dimension(77, 17));
		ValueLabel valueLabel = new ValueLabel();
		valueLabel.setValue(GuiController.getPrefs().get("locate", "en,US"));
		comboBoxLanguage.setSelectedItem(valueLabel);
		add(comboBoxLanguage);
	}

	public void setPowerOn(boolean isOn) {
		ledPowerOn.setOn(isOn);
		if(!isOn){
			ledMute.setOn(isOn);
			ledAlarm.setOn(isOn);
		}
	}

	public ValueChangeListener getStatusChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {

				Object source = valueChangeEvent.getSource();
				int status;

				if(source instanceof Long)
					status=((Long)source).intValue();
				else
					status = (int) source;

				ledMute.setOn((status&MonitorController.MUTE)>0);
				ledAlarm.setOn((status&MonitorController.LOCK)==0);
			}
		};
	}

	public void refresh() {
		ResourceBundle messages = Tanslation.messages;
		ledPowerOn.setText(messages.getString("power_on"));
		ledAlarm.setText(messages.getString("alarm"));
		ledMute.setText(messages.getString("mute"));
	}
}