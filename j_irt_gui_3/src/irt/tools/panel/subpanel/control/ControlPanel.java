package irt.tools.panel.subpanel.control;

import irt.controller.AttenuationController;
import irt.controller.FrequencyContriller;
import irt.controller.GainController;
import irt.controller.GuiController;
import irt.controller.control.ControlController;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.translation.Translation;
import irt.data.IdValue;
import irt.data.IdValueForComboBox;
import irt.data.Listeners;
import irt.data.packet.LinkHeader;
import irt.irt_gui.IrtGui;
import irt.tools.button.ImageButton;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicComboBoxUI;


@SuppressWarnings("serial")
public class ControlPanel extends MonitorPanelAbstract {

	public static final short FLAG_ATTENUATION= 0;
	public static final short FLAG_GAIN			= 1;
	public static final short FLAG_FREQUENCY 	= 1<<1;
	public static final short FLAG_FREQUENCY_SET= 1<<2;

	protected JTextField txtGain;
	protected JSlider slider;
	private JTextField txtStep;
	private JCheckBox chckbxStep;
	private JComboBox<IdValue> comboBox;
	private JLabel lblChoice;

	private ControllerAbstract ñontroller;
	protected Cursor cursor;
	protected Color color;
	private IdValue selection;
	private JComboBox<String> cbLoSelect;
	private boolean hasFreqSet;
	private JLabel lblMute;
	private ImageButton btnMute;
	private ImageButton btnStoreConfig;
	private int flags;
	protected String selectedLanguage;

	public ControlPanel(LinkHeader linkHeader, int flags) {
		super(linkHeader, Translation.getValue(String.class, "control", "Control") , 214, 180);

		selectedLanguage = Translation.getSelectedLanguage();

		Font font = Translation.getFont();
		titledBorder.setTitleFont(font);

		this.flags = flags;
		hasFreqSet = (flags & ControlPanel.FLAG_FREQUENCY_SET)>0;

		color = new Color(0x0B,0x17,0x3B);
		cursor = new Cursor(Cursor.HAND_CURSOR);

		String muteText = Translation.getValue(String.class, "mute", "MUTE");

		btnMute = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/power-red.png")).getImage());
		btnMute.setToolTipText(muteText);
		btnMute.setName("Button Mute");
		Point p = setMuteButtonPosition();
		btnMute.setBounds(p.x, p.y, 33, 33);
		btnMute.setShadowShiftX(4);
		btnMute.setShadowShiftY(4);
		btnMute.setShadowPressedShiftX(1);
		btnMute.setShadowPressedShiftY(1);
		btnMute.setCursor(cursor);
		add(btnMute);

		font = font.deriveFont(new Float(properties.getProperty("control.label.mute.font.size_"+selectedLanguage)));
		lblMute = new JLabel(muteText);
		lblMute.setName("Label Mute");
		lblMute.setHorizontalAlignment(SwingConstants.LEFT);
		lblMute.setForeground(Color.YELLOW);
		lblMute.setFont(font);
		lblMute.setBounds(48, 107, 93, 20);
		add(lblMute);

		txtGain = new JTextField();
		txtGain.setForeground(Color.YELLOW);
		txtGain.setBackground(color);
		txtGain.setHorizontalAlignment(SwingConstants.CENTER);
		txtGain.setName("Text Gain");
		txtGain.setFont(font);
		txtGain.setBounds(14, 42 , 188, 20);
		txtGain.setCaretColor(Color.WHITE);
		add(txtGain);
		txtGain.setColumns(10);

		btnStoreConfig = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png")).getImage());
		btnStoreConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnStoreConfig.setToolTipText(Translation.getValue(String.class, "store_config", "Store Config"));
		btnStoreConfig.setShadowShiftY(4);
		btnStoreConfig.setShadowShiftX(4);
		btnStoreConfig.setShadowPressedShiftY(1);
		btnStoreConfig.setShadowPressedShiftX(1);
		btnStoreConfig.setName("Store");
		p = setConfigButtonPosition();
		btnStoreConfig.setBounds(p.x, p.y, 33, 33);
		add(btnStoreConfig);
		
		comboBox = new JComboBox<>();
		comboBox.addPopupMenuListener(Listeners.popupMenuListener);

		comboBox.addItem(new IdValueForComboBox(FLAG_ATTENUATION, Translation.getValue(String.class, "attenuation", "ATTENUATION")));
		if((flags&FLAG_GAIN)>0)
			comboBox.addItem(new IdValueForComboBox(FLAG_GAIN, Translation.getValue(String.class, "gain", "GAIN")));
		if((flags&FLAG_FREQUENCY)>0)
			comboBox.addItem(new IdValueForComboBox(FLAG_FREQUENCY, Translation.getValue(String.class, "frequency", "FREQUENCY")));
		comboBox.setBounds(14, 19, 85, 20);
		comboBox.setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0;}};}});
		comboBox.setBackground(color);
		comboBox.setForeground(Color.YELLOW);
		comboBox.setCursor(cursor);
		add(comboBox);
		
		lblChoice = new JLabel("");
		lblChoice.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(selection!=null)
					comboBox.setSelectedItem(selection);
			}
		});
		lblChoice.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblChoice.setName("Label Choice");
		lblChoice.setHorizontalAlignment(SwingConstants.LEFT);
		lblChoice.setForeground(Color.LIGHT_GRAY);
		lblChoice.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblChoice.setBounds(118, 19, 85, 20);
		add(lblChoice);

		slider = new JSlider();

		addAncestorListener(new AncestorListener() {
			private IdValue selectedItem;

			public void ancestorAdded(AncestorEvent event) {
				short control = (short) GuiController.getPrefs().getInt("Control", FLAG_ATTENUATION);

				comboBox.addItemListener(new ItemListener() {

					@SuppressWarnings("unchecked")
					@Override
					public void itemStateChanged(ItemEvent e) {

						if(e.getStateChange()==ItemEvent.SELECTED){

							JComboBox<IdValue> source = (JComboBox<IdValue>)e.getSource();

							if(selectedItem!=null){
								selection = selectedItem;
								lblChoice.setText(selection.toString());
							}

							selectedItem = (IdValue) source.getSelectedItem();
							setController(selectedItem.getID());
							GuiController.getPrefs().putInt("Control", selectedItem.getID());
						}
					}
				});

				comboBox.setSelectedItem(new IdValue(control, null));
				selectedItem = (IdValue) comboBox.getSelectedItem();

				setController(((IdValue)comboBox.getSelectedItem()).getID());
			}

			public void ancestorMoved(AncestorEvent event) {}

			public void ancestorRemoved(AncestorEvent event) {
				if(ñontroller!=null)
					ñontroller.setRun(false);
				ñontroller = null;
			}
		});


		font = font.deriveFont(16f);

		if(hasFreqSet){
			cbLoSelect = new JComboBox<String>();
			cbLoSelect.setName("LO Select");
			cbLoSelect.setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0; }};}});
			cbLoSelect.addPopupMenuListener(Listeners.popupMenuListener);
			cbLoSelect.setForeground(Color.YELLOW);
			cbLoSelect.setBackground(color);
			cbLoSelect.setCursor(cursor);
			cbLoSelect.setFont(font);
			cbLoSelect.setBounds(10, 141, 194, 26);
			add(cbLoSelect);
			((JLabel)cbLoSelect.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		}

		font = font.deriveFont(
				new Float(
						properties.getProperty(
								"control.checkBox.font.size_"+selectedLanguage)))
								.deriveFont(
										IrtPanel.fontStyle.get(
												properties.getProperty(
														"control.checkBox.font.style_"+selectedLanguage)));
		chckbxStep = new JCheckBox(Translation.getValue(String.class, "step", "Step")+":");
		chckbxStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				slider.setSnapToTicks(chckbxStep.isSelected());
				txtGain.requestFocusInWindow();
			}
		});
		chckbxStep.setForeground(Color.WHITE);
		chckbxStep.setOpaque(false);
		chckbxStep.setFont(font);
		chckbxStep.setBounds(14, 65, 62, 23);
		add(chckbxStep);
		
		txtStep = new JTextField();
 		txtStep.setBackground(color);
		txtStep.setForeground(Color.WHITE);
		txtStep.setText("1");
		txtStep.setHorizontalAlignment(SwingConstants.CENTER);
		txtStep.setFont(font);
		txtStep.setColumns(10);
		txtStep.setBounds(75, 65, 127, 20);
		txtStep.setCaretColor(Color.YELLOW);
		add(txtStep);
	}

	protected Point setConfigButtonPosition() {
		return new Point(151, 101);
	}

	protected Point setMuteButtonPosition() {
		return new Point(14, 101);
	}

	private void setController(int control) {
		if(ñontroller!=null)
			ñontroller.setRun(false);

		Thread thread;
		switch(control){
		case FLAG_GAIN:
			thread = new Thread(ñontroller =  getNewGainController(), "Gain Controller");
			break;
		case FLAG_FREQUENCY:
			thread = new Thread(ñontroller = getNewFreqController(), "Frequency Controller");
			break;
		default:
			thread = new Thread(ñontroller = getNewAttenController(), "Attenuation Controller");
		}
		thread.setPriority(thread.getPriority()-1);
		thread.start();
	}

	@Override
	protected ControllerAbstract getNewController() {
		return new ControlController(getLinkHeader(),this);
	}

	protected AttenuationController getNewAttenController() {
		return new AttenuationController(getLinkHeader(), txtGain, slider, txtStep, Style.CHECK_ALWAYS);
	}

	protected GainController getNewGainController() {
		return new GainController(getLinkHeader(), txtGain, slider, txtStep, Style.CHECK_ALWAYS);
	}

	protected FrequencyContriller getNewFreqController() {
		return new FrequencyContriller(getLinkHeader(), txtGain, slider, txtStep, Style.CHECK_ALWAYS);
	}

	public JSlider getSlider() {
		return slider;
	}

	public void refresh() {
		super.refresh();

		titledBorder.setTitle(Translation.getValue(String.class, "control", "Control"));

		selectedLanguage = Translation.getSelectedLanguage();
		Font font = Translation.getFont().deriveFont(
				Float.parseFloat(
						properties.getProperty(
								"control.label.mute.font.size_"+selectedLanguage)));
		String muteText = Translation.getValue(String.class, "mute", "MUTE");

		btnMute.setToolTipText(muteText);
		btnStoreConfig.setToolTipText(Translation.getValue(String.class, "store_config", "Store Config"));

		font = font.deriveFont(new Float(properties.getProperty("control.label.mute.font.size_"+selectedLanguage)));
		lblMute.setText(muteText);
		lblMute.setFont(font);

		font = font.deriveFont(
				new Float(
						properties.getProperty(
								"control.checkBox.font.size_"+selectedLanguage)))
								.deriveFont(
										IrtPanel.fontStyle.get(
												properties.getProperty(
														"control.checkBox.font.style_"+selectedLanguage)));
		chckbxStep.setText(Translation.getValue(String.class, "step", "Step")+":");
		chckbxStep.setFont(font);

		DefaultComboBoxModel<IdValue> model = new DefaultComboBoxModel<>();
		model.addElement(new IdValueForComboBox(FLAG_ATTENUATION, Translation.getValue(String.class, "attenuation", "ATTENUATION")));
		if((flags&FLAG_GAIN)>0)
			model.addElement(new IdValueForComboBox(FLAG_GAIN, Translation.getValue(String.class, "gain", "GAIN")));
		if((flags&FLAG_FREQUENCY)>0)
			model.addElement(new IdValueForComboBox(FLAG_FREQUENCY, Translation.getValue(String.class, "frequency", "FREQUENCY")));
		comboBox.setModel(model);

		repaint();
	}
}
