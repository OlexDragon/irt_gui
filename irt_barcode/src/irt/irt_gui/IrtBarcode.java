package irt.irt_gui;

import irt.controller.BarcodeController;
import irt.controller.ImageController;
import irt.panels.ClosePanel;
import irt.panels.IrtPanel;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class IrtBarcode extends JFrame {

	private static final String VER = "Ver: 2.001";

	public IrtBarcode() {
		super("Irt Barcode");
		setSize(845, 441);
		ImageIcon imageIcon = new ImageIcon(getClass().getResource("/irt/images/logo.gif"));
		setIconImage(imageIcon.getImage());
		 
        Color transparent = new Color(0,true);

		Container contentPane = getContentPane();
		contentPane.setLayout(null);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBackground(transparent);
		setLocationRelativeTo(null);

		ClosePanel closePanel = new ClosePanel(this);
		closePanel.setBounds(757, 37, 88, 47);
		contentPane.add(closePanel);
		
		IrtPanel irtPanel = new IrtPanel(this);
		irtPanel.setVisible(true);
		contentPane.add(irtPanel);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 45, 818, 396);
		contentPane.add(tabbedPane);
		
		JPanel barCodePanel = new JPanel();
		barCodePanel.setBackground(new Color(0x0B,0x17,0x3B));
		tabbedPane.addTab("Barcode Creator", null, barCodePanel, null);
		barCodePanel.setLayout(null);
		
		JLabel lblBarcode = new JLabel("");
		lblBarcode.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lblBarcode.setBounds(453, 11, 350, 350);
		barCodePanel.add(lblBarcode);
		
		JTextArea textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
		textArea.setBounds(10, 11, 433, 312);
		barCodePanel.add(textArea);
		
		JButton btnSave = new JButton("Save");
		btnSave.setEnabled(false);
		btnSave.setBounds(10, 334, 70, 23);
		barCodePanel.add(btnSave);
		
		JButton btnCreatBarcode = new JButton("Creat Barcode >");
		btnCreatBarcode.setBounds(302, 334, 141, 23);
		barCodePanel.add(btnCreatBarcode);
				
		JPanel LabelCreatorPanel = new JPanel();
		LabelCreatorPanel.setBackground(new Color(0x0B,0x17,0x3B));
		tabbedPane.addTab("Label Creator", null, LabelCreatorPanel, null);
		LabelCreatorPanel.setLayout(null);
		
		JPanel imagePanel = new JPanel();
		imagePanel.setMinimumSize(new Dimension(50, 20));
		imagePanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		imagePanel.setBackground(Color.WHITE);
		imagePanel.setBounds(139, 11, 664, 346);
		LabelCreatorPanel.add(imagePanel);
		
		JLabel lblSmallImage = new JLabel(""){};
		lblSmallImage.setBackground(new Color(25, 25, 112));
		lblSmallImage.setOpaque(true);
		lblSmallImage.setBorder(new LineBorder(new Color(255, 255, 255), 2));
		lblSmallImage.setBounds(27, 11, 90, 90);
		LabelCreatorPanel.add(lblSmallImage);
		try {
			ImageController.putImage(lblSmallImage, ImageIO.read(IrtBarcode.class.getResource("/irt/images/logo.gif")));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
		}
		
		JLabel lblV = new JLabel(VER);
		lblV.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblV.setBounds(240, 0, 73, 17);
		getContentPane().add(lblV);
		
		JLabel lblV2 = new JLabel(VER);
		lblV2.setForeground(Color.WHITE);
		lblV2.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblV2.setBounds(241, 1, 73, 17);
		getContentPane().add(lblV2);
		imagePanel.setLayout(null);
		
		JLabel lblBackground = new JLabel(""); 
		lblBackground.setBounds(0, 0, imagePanel.getWidth(), imagePanel.getHeight());
		imagePanel.add(lblBackground);

		new BarcodeController(barCodePanel, textArea, lblBarcode, btnCreatBarcode, btnSave);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IrtBarcode frame = new IrtBarcode();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
