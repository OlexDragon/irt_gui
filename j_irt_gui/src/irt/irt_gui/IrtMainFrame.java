package irt.irt_gui;

import irt.data.Listeners;
import irt.tools.panel.head.ClosePanel;
import irt.tools.panel.head.IrtPanel;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public abstract class IrtMainFrame extends JFrame {


	public IrtMainFrame(int width, int hight, int closePanelXPosition) {
		super(IrtPanel.properties.getProperty("company_name_"+IrtPanel.companyIndex));
		setSize(width, hight);
		ImageIcon imageIcon = new ImageIcon(
				IrtGui.class.getResource(
						IrtPanel.properties.get("company_logo_"+IrtPanel.companyIndex).toString()));
		setIconImage(imageIcon.getImage());
 
        Color transparent = new Color(0,true);

        setUndecorated(true);
		setBackground(transparent);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel contentPane = new JPanel();
		contentPane.setBackground(transparent);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		setLocationRelativeTo(null);
		
		ClosePanel closePanel = new ClosePanel(this);
		closePanel.setLocation(closePanelXPosition, 29);
		contentPane.add(closePanel);

		setIrtPanel(contentPane);

		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setName("Unit's Serial Port");
		comboBox.addPopupMenuListener(Listeners.popupMenuListener);
		comboBox.setBounds(comboBoxBounds());
		getContentPane().add(comboBox);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent arg0) {

				Thread guiController = getNewGuiController();
				guiController.setPriority(guiController.getPriority()-1);
				guiController.start();
			}
		});
	}

	protected void setIrtPanel(JPanel contentPane) {
		IrtPanel irtPanel = new IrtPanel(this);
		irtPanel.setVisible(true);
		contentPane.add(irtPanel);
	}

	protected abstract Rectangle comboBoxBounds();

	protected abstract Thread getNewGuiController();
}
