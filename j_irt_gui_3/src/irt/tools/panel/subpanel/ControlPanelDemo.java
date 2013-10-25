package irt.tools.panel.subpanel;

import irt.controller.control.ControllerAbstract;
import irt.data.IdValue;
import irt.data.Listeners;
import irt.irt_gui.IrtGui;
import irt.tools.button.ImageButton;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

@SuppressWarnings("serial")
public class ControlPanelDemo extends JPanel {

	public static final short ATTENUATION= 0;
	public static final short GAIN 		= 1;
	public static final short FREQUENCY 	= 2;

	protected JTextField txtGain;
	protected JSlider slider;
	private JTextField txtStep;
	private JCheckBox chckbxStep;

	private ControllerAbstract ñontroller;
	protected Cursor cursor;
	protected Color color;

	public ControlPanelDemo() {
		setLayout(null);

		color = new Color(0x0B,0x17,0x3B);
		cursor = new Cursor(Cursor.HAND_CURSOR);

		ImageButton btnMute = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/power-red.png")).getImage());
		btnMute.setToolTipText("MUTE");
		btnMute.setName("Button Mute");
		btnMute.setBounds(50, 13, 33, 9);
		btnMute.setCursor(cursor);
		add(btnMute);

		ImageButton imageButton = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png")).getImage());
		imageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		imageButton.setToolTipText("Store Config");
		imageButton.setName("Store");
		imageButton.setBounds(88, 13, 33, 9);
		add(imageButton);

		JLabel lblMute = new JLabel("MUTE");
		lblMute.setName("Label Mute");
		lblMute.setHorizontalAlignment(SwingConstants.LEFT);
		lblMute.setForeground(Color.YELLOW);
		lblMute.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblMute.setBounds(126, 8, 47, 20);
		add(lblMute);
		txtGain = new JTextField();
		txtGain.setForeground(Color.YELLOW);
		txtGain.setBackground(color);
		txtGain.setHorizontalAlignment(SwingConstants.CENTER);
		txtGain.setName("Text Gain");
		txtGain.setFont(new Font("Tahoma", Font.BOLD, 16));
		txtGain.setBounds(178, 5 , 156, 26);
		txtGain.setCaretColor(Color.WHITE);
		add(txtGain);
		txtGain.setColumns(10);
		
		JComboBox<IdValue> comboBox = new JComboBox<>();
		comboBox.setUI(new BasicComboBoxUI());//remove border
		comboBox.addPopupMenuListener(Listeners.popupMenuListener);

		comboBox.setBounds(339, 17, 1, 1);
		comboBox.setUI(new BasicComboBoxUI(){

			@Override
			protected JButton createArrowButton() {
				return new JButton(){

					@Override
					public int getWidth() {
						return 0;
					}};
			}
		});
		comboBox.setBackground(color);
		comboBox.setForeground(Color.YELLOW);
		comboBox.setCursor(cursor);
		add(comboBox);
		
		chckbxStep = new JCheckBox("Step:");
		chckbxStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				slider.setSnapToTicks(chckbxStep.isSelected());
				txtGain.requestFocusInWindow();
			}
		});
		chckbxStep.setForeground(Color.WHITE);
		chckbxStep.setOpaque(false);
		chckbxStep.setFont(new Font("Tahoma", Font.PLAIN, 12));
		chckbxStep.setBounds(345, 6, 55, 23);
		add(chckbxStep);
		
		txtStep = new JTextField();
 		txtStep.setBackground(color);
		txtStep.setForeground(Color.WHITE);
		txtStep.setText("1");
		txtStep.setHorizontalAlignment(SwingConstants.CENTER);
		txtStep.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtStep.setColumns(10);
		txtStep.setBounds(152, 36, 146, 26);
		txtStep.setCaretColor(Color.YELLOW);
		add(txtStep);
		
		slider = new JSlider();

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
			}

			public void ancestorMoved(AncestorEvent event) {}

			public void ancestorRemoved(AncestorEvent event) {
				if(ñontroller!=null)
					ñontroller.setRun(false);
				ñontroller = null;
			}
		});
	}
}
