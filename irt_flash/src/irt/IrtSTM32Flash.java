package irt;

import irt.controller.STM32GuiController;
import irt.data.Listeners;
import irt.data.value.StaticComponents;
import irt.tools.button.ImageButton;
import irt.tools.label.LED;
import irt.tools.panel.head.ClosePanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.head.MainPanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

@SuppressWarnings("serial")
public class IrtSTM32Flash extends JFrame {

	private JPanel contentPane;
	private JTextArea textArea;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IrtSTM32Flash frame = new IrtSTM32Flash();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public IrtSTM32Flash() {
		super("Irt STM32 Flash");

		ImageIcon imageIcon = new ImageIcon(getClass().getResource("images/logo.gif"));
		setIconImage(imageIcon.getImage());
 
        Color transparent = new Color(0,true);

        setUndecorated(true);
		setSize(705, 598);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBackground(transparent);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setBackground(transparent);
		setContentPane(contentPane);
		contentPane.setLayout(null);

		ClosePanel closePanel = new ClosePanel();
		closePanel.setLocation(618, 22);
		contentPane.add(closePanel);

		IrtPanel irtPanel = new IrtPanel(this);
		irtPanel.setVisible(true);
		contentPane.add(irtPanel);

		Color background = new Color(0x3B, 0x4A, 0x8B);

		MainPanel headPanel = new MainPanel(this, 700);
		headPanel.setLocation(0, 51);
		headPanel.setVisible(true);
		headPanel.setSize(700, 74);
		headPanel.setBackground(background);
		headPanel.setArcStart(-50);
		headPanel.setArcStep(160);
		headPanel.setArcWidth(80);
		contentPane.add(headPanel);
		
		ImageButton btnConnect = new ImageButton(new ImageIcon(getClass().getResource("images/power-red.png")).getImage());
		btnConnect.setName("Connect");
		btnConnect.setToolTipText("Connect");
		btnConnect.setShadowShiftY(5);
		btnConnect.setShadowShiftX(5);
		btnConnect.setShadowPressedShiftY(1);
		btnConnect.setShadowPressedShiftX(1);
		btnConnect.setBackground(new Color(30, 144, 255));
		btnConnect.setBounds(43, 11, 52, 52);
		headPanel.add(btnConnect);
		
		JLabel lblConnect = new JLabel("CONNECT");
		lblConnect.setHorizontalAlignment(SwingConstants.LEFT);
		lblConnect.setForeground(Color.YELLOW);
		lblConnect.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblConnect.setBounds(98, 28, 79, 20);
		headPanel.add(lblConnect);
		
		ImageButton btnRead = new ImageButton(new ImageIcon(getClass().getResource("/irt/images/read.png")).getImage());
		btnRead.setName("Read");
		btnRead.setToolTipText("Read Variables");
		btnRead.setShadowShiftY(5);
		btnRead.setShadowShiftX(5);
		btnRead.setShadowPressedShiftY(1);
		btnRead.setShadowPressedShiftX(1);
		btnRead.setBackground(new Color(30, 144, 255));
		btnRead.setBounds(207, 11, 52, 52);
		headPanel.add(btnRead);
		
		JLabel lblRead = new JLabel("READ");
		lblRead.setHorizontalAlignment(SwingConstants.LEFT);
		lblRead.setForeground(Color.YELLOW);
		lblRead.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblRead.setBounds(262, 28, 79, 20);
		headPanel.add(lblRead);
		
		ImageButton btnWrite = new ImageButton(new ImageIcon(getClass().getResource("/irt/images/write-button.png")).getImage());
		btnWrite.setName("Write");
		btnWrite.setToolTipText("Write Variables");
		btnWrite.setShadowShiftY(5);
		btnWrite.setShadowShiftX(5);
		btnWrite.setShadowPressedShiftY(1);
		btnWrite.setShadowPressedShiftX(1);
		btnWrite.setBackground(new Color(30, 144, 255));
		btnWrite.setBounds(366, 11, 52, 52);
		headPanel.add(btnWrite);
		
		JLabel lblWrite = new JLabel("WRITE");
		lblWrite.setHorizontalAlignment(SwingConstants.LEFT);
		lblWrite.setForeground(Color.YELLOW);
		lblWrite.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblWrite.setBounds(421, 28, 79, 20);
		headPanel.add(lblWrite);
		
		Font font = new Font("Tahoma", Font.BOLD, 18);

		JComboBox<String> comboBox_1 = new JComboBox<String>();
		comboBox_1.setName("Address");
		comboBox_1.setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0; }};}});
		comboBox_1.addPopupMenuListener(Listeners.popupMenuListener);
		comboBox_1.setForeground(Color.YELLOW);
		comboBox_1.setBackground(background.darker().darker());
		comboBox_1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboBox_1.setBounds(533, 23, 114, 28);
		comboBox_1.setFont(font);
		((JLabel)comboBox_1.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		comboBox_1.setModel(new DefaultComboBoxModel<String>(new String[]{"0x080C0000","0x080E0000"}));
		headPanel.add(comboBox_1);
		
		LED ledRx = StaticComponents.getLedRx();
		ledRx.setBounds(8, 31, 14, 14);
		headPanel.add(ledRx);

		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setBounds(321, 11, 180, 28);
		comboBox.setFont(font);
		contentPane.add(comboBox);
		
		JPanel workPanel = new JPanel();
		workPanel.setBackground(new Color(0x0B,0x17,0x3B));
		workPanel.setBounds(0, 179, getWidth(), 419);
		contentPane.add(workPanel);
		workPanel.setLayout(null);
		
		ImageButton imageButton = new ImageButton(new ImageIcon(getClass().getResource("/irt/images/folder.png")).getImage());
		imageButton.setToolTipText("Select the File");
		imageButton.setShadowShiftY(5);
		imageButton.setShadowShiftX(5);
		imageButton.setShadowPressedShiftY(1);
		imageButton.setShadowPressedShiftX(1);
		imageButton.setName("Open");
		imageButton.setBackground(new Color(30, 144, 255));
		imageButton.setBounds(538, 11, 52, 52);
		workPanel.add(imageButton);
		
		ImageButton btnUpload = new ImageButton(new ImageIcon(getClass().getResource("images/whitehouse_button.png")).getImage());
		btnUpload.setToolTipText("Upload Program");
		btnUpload.setShadowShiftY(5);
		btnUpload.setShadowShiftX(5);
		btnUpload.setShadowPressedShiftY(1);
		btnUpload.setShadowPressedShiftX(1);
		btnUpload.setName("Upload");
		btnUpload.setBackground(new Color(30, 144, 255));
		btnUpload.setBounds(538, 74, 52, 52);
		workPanel.add(btnUpload);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 518, 397);
		workPanel.add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setForeground(new Color(0x0B,0x17,0x3B));
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
		textArea.setBackground(new Color(204, 255, 255));
		textArea.setColumns(10);
		
		JLabel lblOpenFile = new JLabel("OPEN FILE");
		lblOpenFile.setHorizontalAlignment(SwingConstants.LEFT);
		lblOpenFile.setForeground(Color.YELLOW);
		lblOpenFile.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblOpenFile.setBounds(600, 31, 95, 20);
		workPanel.add(lblOpenFile);
		
		JLabel lblUpload = new JLabel("UPLOAD");
		lblUpload.setHorizontalAlignment(SwingConstants.LEFT);
		lblUpload.setForeground(Color.YELLOW);
		lblUpload.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblUpload.setBounds(600, 89, 79, 20);
		workPanel.add(lblUpload);
		
		JLabel lblFileName = new JLabel("...");
		lblFileName.setName("File Name");
		lblFileName.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileName.setForeground(Color.YELLOW);
		lblFileName.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblFileName.setBackground(new Color(11, 11, 97));
		lblFileName.setBounds(528, 159, 177, 32);
		workPanel.add(lblFileName);

		ImageButton btnSaveFile = new ImageButton(new ImageIcon(getClass().getResource("/irt/images/green-save-button-24995991.png")).getImage());
		btnSaveFile.setToolTipText("Upload Program");
		btnSaveFile.setShadowShiftY(5);
		btnSaveFile.setShadowShiftX(5);
		btnSaveFile.setShadowPressedShiftY(1);
		btnSaveFile.setShadowPressedShiftX(1);
		btnSaveFile.setName("Save File");
		btnSaveFile.setBackground(new Color(30, 144, 255));
		btnSaveFile.setBounds(538, 280, 52, 52);
		workPanel.add(btnSaveFile);

		JLabel lblSaveFile = new JLabel("SAVE FILE");
		lblSaveFile.setHorizontalAlignment(SwingConstants.LEFT);
		lblSaveFile.setForeground(Color.YELLOW);
		lblSaveFile.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblSaveFile.setBounds(600, 294, 95, 20);
		workPanel.add(lblSaveFile);

		JLabel lblSelectTheSerial = new JLabel("Select The Serial Port");
		lblSelectTheSerial.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectTheSerial.setForeground(Color.YELLOW);
		lblSelectTheSerial.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblSelectTheSerial.setBackground(new Color(0x0B0B61));
		lblSelectTheSerial.setOpaque(true);
		lblSelectTheSerial.setBounds(0, 136, getWidth(), 32);
		contentPane.add(lblSelectTheSerial);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(0, 123, getWidth(), 13);
		contentPane.add(progressBar);
//		progressBar.setOpaque(false);
		progressBar.setForeground(Color.GREEN);

		new STM32GuiController(this);
	}
}
