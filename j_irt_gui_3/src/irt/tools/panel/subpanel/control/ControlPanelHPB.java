package irt.tools.panel.subpanel.control;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiController;
import irt.controller.ValueController;
import irt.controller.interfaces.ControlPanel;
import irt.controller.interfaces.DescriptionPacketValue;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.data.ALCSet;
import irt.data.AttanuationSet;
import irt.data.FrequencySet;
import irt.data.IdValueFreq;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.value.Value;
import irt.tools.ALCComboBox;
import irt.tools.IrtComboBox;
import irt.tools.button.MuteButton;
import irt.tools.combobox.LoSelectComboBox;

public class ControlPanelHPB extends JPanel implements Refresh, ControlPanel, Observer{
	private static final String ACTION = "Action";

	private static final String STEP = "Step";

	private static final String STEP_ENABLE = "StepEnable";

	private static final long serialVersionUID = 3774070668998313663L;

	private final Logger logger = LogManager.getLogger();

	private static final Preferences prefs = GuiController.getPrefs();

	private static final Color BG_COLOR = new Color(11, 23, 59);


	private final TitledBorder 	titledBorder;
	private final MuteButton 	btnMute;
	private final JSlider 		slider 			= new JSlider();
	private final JTextField 	valueTextField;
	private final JTextField 	stepTextField;
	private final JCheckBox 	chckbxStep;
	private final JCheckBox		chckbxEnableAlc;
	private final IrtComboBox<DescriptionPacketValue> actionComboBox;

	private final ValueController 		valueController;
	private boolean presetDone;

	private JLabel lblMute;

	private JComboBox<IdValueFreq> cbLoSelect;

	public ControlPanelHPB(byte linkAddr) {
		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				addListeners();
			}
			public void ancestorMoved(AncestorEvent event) {
			}
			public void ancestorRemoved(AncestorEvent event) {
				removeListeners();
			}
		});
		titledBorder = new TitledBorder(null, "Control", TitledBorder.LEADING, TitledBorder.TOP, getFont().deriveFont(Font.BOLD, 18), Color.WHITE);
		setBorder(titledBorder);
		setOpaque(false);
		setLayout(null);
		setSize(new Dimension(214, 180));

		btnMute = new MuteButton();
		btnMute.setLinkAddr(linkAddr);
		btnMute.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnMute.setBounds(16, 99, 35, 35);
		add(btnMute);
		
		lblMute = new JLabel("Mute");
		btnMute.setMuteLabel(lblMute);
		lblMute.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		lblMute.setHorizontalAlignment(SwingConstants.LEFT);
		lblMute.setForeground(Color.YELLOW);
		lblMute.setBounds(53, 99, 127, 35);
		add(lblMute);

		valueTextField = new JTextField();
		valueTextField.setName("Text Gain");
		valueTextField.setHorizontalAlignment(SwingConstants.CENTER);
		valueTextField.setForeground(Color.YELLOW);
		valueTextField.setFont(null);
		valueTextField.setColumns(10);
		valueTextField.setCaretColor(Color.WHITE);
		valueTextField.setBackground(BG_COLOR);
		valueTextField.setBounds(8, 46, 188, 20);
		add(valueTextField);		

		actionComboBox = new IrtComboBox<>();
		actionComboBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		actionComboBox.setForeground(Color.YELLOW);
		actionComboBox.setFont(new Font("Tahoma", Font.BOLD, 14));
		actionComboBox.setBackground(BG_COLOR);
		actionComboBox.setBounds(8, 23, 98, 20);
		actionComboBox.addItem(	new AttanuationSet	(linkAddr));
		actionComboBox.addItem(	new FrequencySet	(linkAddr));
		actionComboBox.addItem(	new ALCSet			(linkAddr));
		add(actionComboBox);

		chckbxStep = new JCheckBox(STEP);
		chckbxStep.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxStep.setOpaque(false);
		chckbxStep.setForeground(Color.WHITE);
		chckbxStep.setFont(null);
		chckbxStep.setBounds(8, 69, 62, 23);
		add(chckbxStep);

		stepTextField = new JTextField();
		stepTextField.setText("1");
		stepTextField.setHorizontalAlignment(SwingConstants.CENTER);
		stepTextField.setForeground(Color.WHITE);
		stepTextField.setFont(null);
		stepTextField.setColumns(10);
		stepTextField.setCaretColor(Color.YELLOW);
		stepTextField.setBackground(BG_COLOR);
		stepTextField.setBounds(69, 69, 127, 20);
		add(stepTextField);

		cbLoSelect = new LoSelectComboBox(linkAddr);
		cbLoSelect.setForeground(Color.YELLOW);
		cbLoSelect.setBackground( new Color(0x0B,0x17,0x3B));
		cbLoSelect.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cbLoSelect.setFont(new Font("Tahoma", Font.BOLD, 14));
		cbLoSelect.setBounds(10, 141, 194, 26);
		add(cbLoSelect);
		((JLabel)cbLoSelect.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		
		chckbxEnableAlc = new ALCComboBox("Enable ALC", linkAddr);
		chckbxEnableAlc.setName("enable.alc");
		chckbxEnableAlc.setBounds(112, 20, 102, 23);
		chckbxEnableAlc.setOpaque(false);
		chckbxEnableAlc.setForeground(Color.YELLOW);
		add(chckbxEnableAlc);

		refresh();
		selectAction();

		valueController = new ValueController(linkAddr, actionComboBox);
	}

	private void selectAction() {

		final String action = prefs.get(ACTION, null);
		if(action!=null)
			for(int i=0; i<actionComboBox.getItemCount(); i++)
				if(actionComboBox.getItemAt(i).getClass().getSimpleName().equals(action)){
					actionComboBox.setSelectedIndex(i);
					break;
				}
	}

	private void preset() {

		final DescriptionPacketValue selectedItem = (DescriptionPacketValue) actionComboBox.getSelectedItem();
		final String simpleName = selectedItem.getClass().getSimpleName();

		//step value
		final String text = prefs.get(simpleName+STEP, selectedItem.getDeaultStep());
		stepTextField.setText(text);

		//set step enable
		setStepEnabled(prefs.getBoolean(simpleName+STEP_ENABLE, true));

	}

	private void addListeners() {

		valueController.addVlueChangeListener(valueChangeListener);

		slider.addChangeListener(sliderChangeListener);

		valueTextField.addActionListener(valueTextActionListener);
		valueTextField.addKeyListener(valueTextKeyAdapter);
		valueTextField.getDocument().addDocumentListener(documentListener);

		actionComboBox.addActionListener(actionComboBoxListener);

		stepTextField.addActionListener(stepTextFieldActionListener);
		stepTextField.addFocusListener(stepFocuseListener);
		chckbxStep.addActionListener(stepCheckBoxActionListener);
	}

	private void removeListeners() {

		valueController.removeVlueChangeListener(valueChangeListener);

		slider.removeChangeListener(sliderChangeListener);

		valueTextField.removeActionListener(valueTextActionListener);
		valueTextField.removeKeyListener(valueTextKeyAdapter);
		valueTextField.getDocument().removeDocumentListener(documentListener);

		actionComboBox.removeActionListener(actionComboBoxListener);

		stepTextField.removeActionListener(stepTextFieldActionListener);
		stepTextField.removeFocusListener(stepFocuseListener);
		chckbxStep.removeActionListener(stepCheckBoxActionListener);
	}

	@Override
	public void refresh() {

		setText();
		setFont();
	}

	private void setText() {
		titledBorder.setTitle(Translation.getValue(String.class, "control", "Control"));

		String muteText = Translation.getValue(String.class, btnMute.getName(), "Mute");
		btnMute.setToolTipText(muteText);
		lblMute.setText(muteText);

		chckbxEnableAlc.setText(Translation.getValue(String.class, "enable.alc", "Enable ALC"));

		chckbxStep.setText(Translation.getValue(String.class, "step", "Step"));
	}

	private void setFont() {
		Font font = Translation.getFont().deriveFont(Translation.getValue(Float.class, "control.label.mute.font.size.hp", 12f)).deriveFont(Font.BOLD);
		lblMute.setFont(font);
		btnMute.setFont(font);

		actionComboBox.setFont(font.deriveFont(14F));

		font = font.deriveFont(12F);
		cbLoSelect.setFont(font);
		chckbxStep.setFont(font);
	}

	@Override
	public JSlider getSlider() {
		return slider;
	}

	private void setValueTextField(final Value value) {
		logger.trace(value);

		valueTextField.removeActionListener(valueTextActionListener);
		valueTextField.getDocument().removeDocumentListener(documentListener);

		valueTextField.setText(value.isError() ? "" : value.toString());

		valueTextField.getDocument().addDocumentListener(documentListener);
		valueTextField.addActionListener(valueTextActionListener);
	}

	private void setSliderValue(final Value value) {

		slider.removeChangeListener(sliderChangeListener);

		slider.setValue(value.getRelativeValue());

		slider.addChangeListener(sliderChangeListener);
	}

	private void setALCEnabled( boolean enabled) {
		if(enabled){
			chckbxEnableAlc.setEnabled(true);
			chckbxEnableAlc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}else{
			chckbxEnableAlc.setEnabled(false);
			chckbxEnableAlc.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private void setStepEnabled() {
		final Value value = valueController.getValue();
		if(value!=null){
			final String text = stepTextField.getText();
			if(text!=null && !text.isEmpty()){
				final Value vCopy = value.getCopy();
				vCopy.setMinMax(1, value.getMaxValue()-value.getMinValue());
				vCopy.setValue(text);
				chckbxStep.setSelected(true);
				slider.removeChangeListener(sliderChangeListener);
				slider.setSnapToTicks(true);
				slider.setMinorTickSpacing((int) vCopy.getValue());
				slider.addChangeListener(sliderChangeListener);
				stepTextField.setText(vCopy.toString());
			}
		}
	}

	private void setStepEnabled(final boolean selected) {
		if(selected)
			setStepEnabled();
		else{
			slider.setSnapToTicks(false);
			chckbxStep.setSelected(false);
			final Value value = valueController.getValue();
			value.setValue(valueTextField.getText());
			slider.removeChangeListener(sliderChangeListener);
			slider.setValue(value.getRelativeValue());
			slider.addChangeListener(sliderChangeListener);
		}

		final DescriptionPacketValue selectedItem = (DescriptionPacketValue) actionComboBox.getSelectedItem();
		final String simpleName = selectedItem.getClass().getSimpleName();
		prefs.putBoolean(simpleName+STEP_ENABLE, chckbxStep.isSelected());
	}

	private void setStepValue() {
		setStepEnabled(true);

		final DescriptionPacketValue selectedItem = (DescriptionPacketValue) actionComboBox.getSelectedItem();
		final String simpleName = selectedItem.getClass().getSimpleName();
		prefs.put(simpleName+STEP, stepTextField.getText());
	}

	@Override
	public void update(Observable o, Object arg) {
		valueController.reset(actionComboBox);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Listeners ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private final KeyAdapter 		valueTextKeyAdapter = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
				valueTextField.setBackground(BG_COLOR);
				valueController.resetValue();
			}
		}
	};

	private final ChangeListener 	sliderChangeListener = new ChangeListener() {
		
		@Override
		public void stateChanged(ChangeEvent e) {

			if(slider.getValueIsAdjusting()){
				final Value v = valueController.getValue();

				if (v != null) {


					final Value value = v.getCopy();
					value.setRelativeValue(slider.getValue());
					setValueTextField(value);

				}
			}else try {

				valueController.setValue(valueTextField, slider);

			} catch (Exception e1) {
				logger.catching(e1);
			}
			
		}
	};
	private final ActionListener 	actionComboBoxListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				if (valueController != null) {
					valueController.reset(actionComboBox);
					presetDone = false;

					final DescriptionPacketValue selectedItem = (DescriptionPacketValue) actionComboBox
							.getSelectedItem();
					final String simpleName = selectedItem.getClass().getSimpleName();
					prefs.put(ACTION, simpleName);
				}
			} catch (Exception ex) {
				logger.catching(ex);
			}
		}
	};
	private final ActionListener 	valueTextActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				valueController.setValue(valueTextField, slider);
			} catch (Exception e1) {
				logger.catching(e1);
			}
		}
	};


	private final ActionListener 	stepCheckBoxActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try{
				setStepEnabled(chckbxStep.isSelected());
			}catch(Exception ex){
				logger.catching(ex);
			}
		}
	};
	private final FocusAdapter 		stepFocuseListener = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent e) {
			setStepValue();
		}
	};
	private final ActionListener 	stepTextFieldActionListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			try{
				setStepValue();
			}catch(Exception ex){
				logger.catching(ex);
			}
		}
	};


	private final DocumentListener 	documentListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			valueTextField.setBackground(new Color(0, 0, 139));
			valueController.duUpdate(false);
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			valueTextField.setBackground(new Color(0, 0, 139));
			valueController.duUpdate(false);
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			valueTextField.setBackground(new Color(0, 0, 139));
			valueController.duUpdate(false);
		}
	};
	private final ValueChangeListener valueChangeListener = new ValueChangeListener() {

		private static final String MIN_MAX = "min=%s; max=%s";

		@Override
		public void valueChanged(ValueChangeEvent e) {

			final int id = e.getID();

			if(id==ValueController.RANGE)
				setRange((Range)e.getSource());

			else if(id==ValueController.VALUE && !slider.getValueIsAdjusting())
				setValue((Value) e.getSource());
		}

		private void setValue(final Value value) {
			logger.entry(value);

			setSliderValue(value);
			setValueTextField(value);
			valueTextField.setBackground(BG_COLOR);

			if(actionComboBox.getSelectedItem() instanceof ALCSet){

				setALCEnabled(!value.isError());

			}else
				setALCEnabled(false);

			if(!presetDone){
				preset();
				presetDone = true;
			}
		}

		private void setRange(Range range) {
			logger.entry(range);

			final long minimum = range.getMinimum();
			final long maximum = range.getMaximum();

			final DescriptionPacketValue selectedItem = (DescriptionPacketValue) actionComboBox.getSelectedItem();
			final Value value = selectedItem.getValue(minimum, maximum);
			value.setValue(minimum);
			String minStr = value.toString();

			value.setValue(maximum);
			String maxStr = value.toString();

			actionComboBox.getSelectedItem();

			slider.removeChangeListener(sliderChangeListener);
			slider.setMinimum(value.getRelativeMinValue());

			final int relativeMaxValue = (int) value.getRelativeMaxValue();
			slider.setMaximum(relativeMaxValue);

			valueTextField.setToolTipText(String.format(MIN_MAX, minStr, maxStr));
		}
	};
}
