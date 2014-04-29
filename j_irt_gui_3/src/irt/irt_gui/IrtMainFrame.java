package irt.irt_gui;

import irt.controller.GuiControllerAbstract;
import irt.data.Listeners;
import irt.tools.panel.head.ClosePanel;
import irt.tools.panel.head.IrtPanel;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class IrtMainFrame extends JFrame {

	protected GuiControllerAbstract guiController;

	public IrtMainFrame(int width, int hight) {
		super(IrtPanel.companyIndex!=null ? IrtPanel.PROPERTIES.getProperty("company_name_"+IrtPanel.companyIndex) : "IRT");
		setSize(width, hight);
		ImageIcon imageIcon = new ImageIcon(
				IrtGui.class.getResource(
						IrtPanel.PROPERTIES.get("company_logo_"+IrtPanel.companyIndex).toString()));
		setIconImage(imageIcon.getImage());
 
        Color transparent = new Color(0,true);

        setUndecorated(true);
		setBackground(transparent);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel contentPane = new JPanel();
		contentPane.setBackground(transparent);
		contentPane.setBorder(null);
		contentPane.setLayout(null);
		setContentPane(contentPane);
		setLocationRelativeTo(null);
		
		ClosePanel closePanel = new ClosePanel(this);
		closePanel.setLocation(getClosePanelPosition());
		contentPane.add(closePanel);

		setIrtPanel(contentPane);

		JComboBox<String> serialPortSelection = new JComboBox<>();
		serialPortSelection.setName("Unit's Serial Port");
		serialPortSelection.addPopupMenuListener(Listeners.popupMenuListener);
		serialPortSelection.setBounds(comboBoxBounds());
		getContentPane().add(serialPortSelection);

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent arg0) {

				guiController = getNewGuiController();
				int priority = guiController.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					guiController.setPriority(priority-1);
				guiController.setDaemon(true);
				guiController.start();
			}
		});
	}

	protected void setIrtPanel(JPanel contentPane) {
		IrtPanel irtPanel = new IrtPanel(this);
		irtPanel.setVisible(true);
		contentPane.add(irtPanel);
	}


	protected abstract Point getClosePanelPosition();
	protected abstract Rectangle comboBoxBounds();
	protected abstract GuiControllerAbstract getNewGuiController();
}
