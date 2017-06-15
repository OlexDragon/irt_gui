package irt.irt_gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import irt.controller.GuiControllerAbstract;
import irt.data.Listeners;
import irt.data.MyThreadFactory;
import irt.tools.panel.head.ClosePanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.wizards.serial_port.SerialPortWizard;
import javafx.application.Platform;

@SuppressWarnings("serial")
public abstract class IrtMainFrame extends JFrame {

	private final Logger logger = (Logger) LogManager.getLogger();

	protected GuiControllerAbstract guiController;
//	private ScheduledFuture<?> scheduledFuture;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	public IrtMainFrame(int width, int hight) {
		super(IrtPanel.PROPERTIES.getProperty("company_name"));

		Platform.setImplicitExit(false); 

		//
//		Runtime.getRuntime().addShutdownHook(new Thread()
//		{
//		    @Override
//		    public void run()
//		    {
//		        logger.info("ShutdownHook");
//		    }
//		});

		setSize(width, hight);

		if(IrtPanel.logoIcon!=null)
			setIconImage(IrtPanel.logoIcon.getImage());
 
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
		
		JPopupMenu popupMenu = new JPopupMenu();
		IrtGui.addPopup(serialPortSelection, popupMenu);
		
		JMenuItem mntmBaudrate = new JMenuItem("Baudrate");
		mntmBaudrate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					new SerialPortWizard().setVisible(true);
				}catch(Exception ex){
					logger.catching(ex);
				}
			}
		});
		popupMenu.add(mntmBaudrate);

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent arg0) {

				guiController = getNewGuiController();
//				scheduledFuture = 
						service.scheduleAtFixedRate(guiController, 2, 5, TimeUnit.SECONDS);
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
