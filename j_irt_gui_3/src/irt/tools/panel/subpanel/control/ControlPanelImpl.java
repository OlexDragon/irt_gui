package irt.tools.panel.subpanel.control;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

import irt.controller.AttenuationController;
import irt.controller.FrequencyContriller;
import irt.controller.GainController;
import irt.controller.GuiController;
import irt.controller.control.ControlController;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.interfaces.ControlPanel;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.IdValue;
import irt.data.IdValueForComboBox;
import irt.data.IdValueFreq;
import irt.data.Listeners;
import irt.data.RundomNumber;
import irt.data.packet.LinkHeader;
import irt.data.packet.Payload;
import irt.irt_gui.IrtGui;
import irt.tools.button.ImageButton;
import irt.tools.combobox.LoSelectComboBox;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;


@SuppressWarnings("serial")
public class ControlPanelImpl extends MonitorPanelAbstract implements ControlPanel {

	public enum ActionFlags{
		FLAG_ATTENUATION,
		FLAG_GAIN,
		FLAG_FREQUENCY,
		FLAG_FREQUENCY_SET,
		FLAG_ALC
	}

	private static final Preferences prefs = GuiController.getPrefs();

	protected JTextField txtGain;
	protected JSlider slider;
	protected JTextField txtStep;
	private JCheckBox chckbxStep;
	protected JComboBox<IdValue> cbActionSelector;
	private JLabel lblChoice;

	private ControllerAbstract controller;
	protected Cursor cursor;
	protected Color color;
	private IdValue selection;
	private JComboBox<IdValueFreq> cbLoSelect;
//	private boolean hasFreqSet;
	private JLabel lblMute; 							protected JLabel getLblMute() { return lblMute; }
	private ImageButton btnMute; 						protected ImageButton getBtnMute() { return btnMute; }
	protected ImageButton btnStoreConfig;
	private int flags;

	@SuppressWarnings("unused")
	public ControlPanelImpl(final int deviceType, LinkHeader linkHeader, int flags) {
		super(deviceType, linkHeader, Translation.getValue(String.class, "control", "Control") , 214, 180);
		setName("ControlPanelImpl");

		Font font = Translation.getFont();

		this.flags = flags;
//		hasFreqSet = (flags & (short)ActionFlags.FLAG_FREQUENCY_SET.ordinal())>0;

		color = new Color(0x0B,0x17,0x3B);
		cursor = new Cursor(Cursor.HAND_CURSOR);

		String muteText = Translation.getValue(String.class, "mute", "MUTE");

		btnMute = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/power-red.png")).getImage());
		btnMute.setToolTipText(muteText);
		btnMute.setName("Button Mute");
		Point p = getMuteButtonPosition();
		if(p==null)
			p = new Point(14, 101);
		int size = Translation.getValue(Integer.class, "control.buttons.size", 33);
		btnMute
		.setBounds(
				p.x,
				p.y, size, size);
		btnMute.setCursor(cursor);
		add(btnMute);

		font = font.deriveFont(Translation.getValue(Float.class, "control.label.mute.font.size", 12f))
				.deriveFont(Font.BOLD);

		lblMute = new JLabel(muteText);
		lblMute.setName("Label Mute");
		lblMute.setHorizontalAlignment(SwingConstants.LEFT);
		lblMute.setForeground(Color.YELLOW);
		lblMute.setFont(font);
		add(lblMute);
		new SwingWorker<Rectangle, Void>() {

			@Override
			protected Rectangle doInBackground() throws Exception {
				try {
					int width = Translation.getValue(Integer.class, "control.label.mute.width", 93);
					return new Rectangle(Translation.getValue(Integer.class, "control.label.mute.x", 48),
							Translation.getValue(Integer.class, "control.label.mute.y", 107), width, 20);
				} catch (Exception e) {
					logger.catching(e);
					return null;
				}
			}
			@Override
			protected void done() {
				try {
					lblMute.setBounds(get());
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();

		font = font.deriveFont(16f);
		if(font == null)//This is for WindowBuilder Editor
			font = new Font("Tahoma", Font.PLAIN, 12);
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

		if(deviceType>DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR){
			btnStoreConfig = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png")).getImage());
			btnStoreConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			btnStoreConfig.setToolTipText(Translation.getValue(String.class, "store_config", "Store Config"));
			btnStoreConfig.setName("Store");
			p = getConfigButtonPosition();
			btnStoreConfig.setBounds(p.x, p.y, size, size);
			add(btnStoreConfig);
		}

		cbActionSelector = new JComboBox<>();
		cbActionSelector.addPopupMenuListener(Listeners.popupMenuListener);

		IdValueForComboBox item = new IdValueForComboBox((short) ActionFlags.FLAG_ATTENUATION.ordinal(), Translation.getValue(String.class, "attenuation", "ATTENUATION"));
		if(item!=null)//for WindowBuilder Editor
			cbActionSelector.addItem(item);
		if((flags&(short) ActionFlags.FLAG_GAIN.ordinal())>0)
			cbActionSelector.addItem(new IdValueForComboBox((short) ActionFlags.FLAG_GAIN.ordinal(), Translation.getValue(String.class, "gain", "GAIN")));
		if((flags&(short) ActionFlags.FLAG_FREQUENCY.ordinal())>0)
			cbActionSelector.addItem(new IdValueForComboBox((short) ActionFlags.FLAG_FREQUENCY.ordinal(), Translation.getValue(String.class, "frequency", "FREQUENCY")));
		cbActionSelector.setBounds(14, 19, 98, 20);
		cbActionSelector.setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0;}};}});
		cbActionSelector.setBackground(color);
		cbActionSelector.setForeground(Color.YELLOW);
		cbActionSelector.setFont(font.deriveFont(Translation.getValue(Float.class, "controll.comboBox.font.size", 14f))
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.type", Font.BOLD)));
		cbActionSelector.setCursor(cursor);
		add(cbActionSelector);

		lblChoice = new JLabel(prefs.get("choice"+deviceType, ""));
		lblChoice.setFont(font);
		lblChoice.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(selection!=null)
					cbActionSelector.setSelectedItem(selection);
				else if(!lblChoice.getText().isEmpty()){
					int itemCount = cbActionSelector.getItemCount();
					for(int i=0; i<itemCount; i++){
						IdValue itemAt = cbActionSelector.getItemAt(i);
						if(itemAt.getValue().equals(lblChoice.getText())){
							selection = itemAt;
							cbActionSelector.setSelectedItem(itemAt);
							break;
						}
					}
				}
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
				short control = (short) prefs.getInt("Control", (short) ActionFlags.FLAG_ATTENUATION.ordinal());

				cbActionSelector.addItemListener(new ItemListener() {

					@SuppressWarnings("unchecked")
					@Override
					public void itemStateChanged(ItemEvent e) {

						if(e.getStateChange()==ItemEvent.SELECTED){

							JComboBox<IdValue> source = (JComboBox<IdValue>)e.getSource();

							if(selectedItem!=null){
								selection = selectedItem;
								String string = selection.toString();
								lblChoice.setText(string);
								prefs.put("choice"+deviceType, string);
							}

							selectedItem = (IdValue) source.getSelectedItem();
							setController(selectedItem.getID());
							prefs.putInt("Control", selectedItem.getID());
						}
					}
				});

				cbActionSelector.setSelectedItem(new IdValue(control, null));
				selectedItem = (IdValue) cbActionSelector.getSelectedItem();

				setController(((IdValue)cbActionSelector.getSelectedItem()).getID());
			}

			public void ancestorMoved(AncestorEvent event) {}

			public void ancestorRemoved(AncestorEvent event) {
				if(controller!=null)
					controller.stop();
				controller = null;
			}
		});


		font = font.deriveFont(16f);

		cbLoSelect = new LoSelectComboBox(linkHeader!=null ? linkHeader.getAddr() : 0);
//		cbLoSelect.setName("LO Select");
		cbLoSelect.setForeground(Color.YELLOW);
		cbLoSelect.setBackground(color);
		cbLoSelect.setCursor(cursor);
		cbLoSelect.setFont(font);
		cbLoSelect.setBounds(10, 141, 194, 26);
		add(cbLoSelect);
		

		font = font.deriveFont(Translation.getValue(Float.class, "control.checkBox.font.size", 12f))
				.deriveFont(Translation.getValue(Integer.class, "control.checkBox.font.style", Font.PLAIN));

		String text = Translation.getValue(String.class, "step", "Step")+":";
		if(text==null)// this if for WindowBuilder Editor
			text = "Error";
		chckbxStep = new JCheckBox(text);
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

	protected Point getConfigButtonPosition() {
		return new Point(151, 101);
	}

	protected Point getMuteButtonPosition() {
		return new Point(14, 101);
	}

	private void setController(int control) {
		if(controller!=null)
			controller.stop();

		Thread t;
		ActionFlags a = ActionFlags.values()[control];
		switch(a){
		case FLAG_GAIN:
			t = new Thread(controller =  getNewGainController(), "ControlPanelImpl.GainController-"+new RundomNumber());
			break;
		case FLAG_FREQUENCY:
			t = new Thread(controller = getNewFreqController(), "ControlPanelImpl.FreqController-"+new RundomNumber());
			break;
		case FLAG_ALC:
			t = new Thread(controller = getNewAlcController(), "ControlPanelImpl.AlcController-"+new RundomNumber());
			break;
		default:
			t = new Thread(controller = getNewAttenController(), "ControlPanelImpl.AttenController-"+new RundomNumber());
		}
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();
	}

	protected AttenuationController getNewAttenController() {
		return new AttenuationController(deviceType, getLinkHeader(), txtGain, slider, txtStep, Style.CHECK_ALWAYS);
	}

	protected GainController getNewGainController() {
		return new GainController(deviceType, getLinkHeader(), txtGain, slider, txtStep, Style.CHECK_ALWAYS);
	}

	protected FrequencyContriller getNewFreqController() {
		return new FrequencyContriller(deviceType, getLinkHeader(), txtGain, slider, txtStep, Style.CHECK_ALWAYS);
	}

	protected ControllerAbstract getNewAlcController() {
		return null;
	}

	public JSlider getSlider() {
		return slider;
	}

	public void refresh() {
		super.refresh();

		titledBorder.setTitle(Translation.getValue(String.class, "control", "Control"));

		Font font = Translation.getFont().deriveFont(Translation.getValue(Float.class, "control.label.mute.font.size", 12f));
		String muteText = Translation.getValue(String.class, "mute", "MUTE");

		btnMute.setToolTipText(muteText);
		int size = Translation.getValue(Integer.class, "control.buttons.size", 33);
		btnMute.setSize(size, size);
		btnMute.setLocation(getMuteButtonPosition());

		btnStoreConfig.setToolTipText(Translation.getValue(String.class, "store_config", "Store Config"));
		btnStoreConfig.setSize(size, size);
		btnStoreConfig.setLocation(getConfigButtonPosition());

		font = font.deriveFont(Translation.getValue(Float.class, "control.label.mute.font.size", 12f))
				.deriveFont(Font.BOLD);
		lblMute.setText(muteText);
		lblMute.setFont(font);
		int x = Translation.getValue(Integer.class, "control.label.mute.x", 48);
		int y = Translation.getValue(Integer.class, "control.label.mute.y", 107);
		int width = Translation.getValue(Integer.class, "control.label.mute.width", 93);
		lblMute.setBounds(x, y, width, 20);

		font = font.deriveFont(Translation.getValue(Float.class, "control.checkBox.font.size", 12f))
				.deriveFont(Translation.getValue(Integer.class, "control.checkBox.font.style", Font.PLAIN));
		chckbxStep.setText(Translation.getValue(String.class, "step", "Step")+":");
		chckbxStep.setFont(font);

		DefaultComboBoxModel<IdValue> model = new DefaultComboBoxModel<>();
		model.addElement(new IdValueForComboBox((short)ActionFlags.FLAG_ATTENUATION.ordinal(), Translation.getValue(String.class, "attenuation", "ATTENUATION")));
		if((flags&(short)ActionFlags.FLAG_GAIN.ordinal())>0)
			model.addElement(new IdValueForComboBox((short)ActionFlags.FLAG_GAIN.ordinal(), Translation.getValue(String.class, "gain", "GAIN")));
		if((flags&(short)ActionFlags.FLAG_FREQUENCY.ordinal())>0)
			model.addElement(new IdValueForComboBox((short)ActionFlags.FLAG_FREQUENCY.ordinal(), Translation.getValue(String.class, "frequency", "FREQUENCY")));
		cbActionSelector.setFont(font
				.deriveFont(Translation.getValue(Float.class, "controll.comboBox.font.size", 18f))
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.type", Font.BOLD)));
		cbActionSelector.setModel(model);

		lblChoice.setFont(font);
	}

	@Override
	protected List<ControllerAbstract> getControllers() {
		List<ControllerAbstract> controllers = new ArrayList<>();
		controllers.add(new ControlController(deviceType, getClass().getSimpleName(), getLinkHeader(),this));
		return controllers;
	}

	@Override protected void packetRecived(List<Payload> payloads) { }
}