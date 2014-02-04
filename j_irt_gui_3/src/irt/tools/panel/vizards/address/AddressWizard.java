package irt.tools.panel.vizards.address;

import irt.tools.panel.vizards.address.StyleSelectorPanel.Selection;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class AddressWizard extends JDialog {
	public static final int MAX_ADDRESS = 255;

	private static final long serialVersionUID = -8162692468959448079L;

	private static final Logger logger = (Logger) LogManager.getLogger();

	private static final AddressWizard ADDRESS_VIZARD = new AddressWizard();

	private static final int SELECTION 	= 0;
	private static final int AUTO 		= 1;
	private static final int MANUALLY 	= 2;

	private JPanel[] panels = new JPanel[3];
	private String[] panelNames = new String[]{"StyleSelectorPanel","Auto", "Manually"};
	private int visiblePanel;

	private Window owner;

	private CardLayout cardLayout;

	public static AddressWizard getInstance() {
		return ADDRESS_VIZARD;
	}

	private AddressWizard() {
		setMinimumSize(new Dimension(350, 220));
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout(0, 0));
			
		final JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		cardLayout = new CardLayout(0, 0);
		panel.setLayout(cardLayout);

		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.SOUTH);
		
		JButton btnBack = new JButton("<Back");
		btnBack.setEnabled(false);
		toolBar.add(btnBack);
		
		JButton btnNext = new JButton("Next>");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{

					switch(visiblePanel){
					case SELECTION:
						if(validateSelection())
							cardLayout.show(panel, panelNames[AUTO]);
					}

				}catch(Exception ex){
					logger.catching(ex);
				}
			}

			private boolean validateSelection() {
				boolean valid = true;
				Selection selection = ((StyleSelectorPanel)panels[0]).getSelection();
				logger.entry(selection);
				switch(selection){
				case AUTO:
					visiblePanel = AUTO;
					break;
				case MANUALLY:
					int unitsNumber = selection.getUnitsNumber();
					if(unitsNumber>0 && unitsNumber<MAX_ADDRESS)
						visiblePanel = MANUALLY;
					else{
						JOptionPane.showMessageDialog(owner, "The number of units should be between 0 and "+(MAX_ADDRESS+1));
						valid = false;
					}
				}
				return logger.exit(valid);
			}
		});
		toolBar.add(btnNext);
		
		JButton btnCansel = new JButton("Cansel");
		btnCansel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						setVisible(false);
					}
				});
			}
		});
		toolBar.add(btnCansel);

		JPanel selector = new StyleSelectorPanel();
		panel.add(selector, panelNames[SELECTION]);
		panels[SELECTION] = selector;
		
		JPanel auto = new AutoAddressPanel();
		panel.add(auto, panelNames[AUTO]);
		panels[AUTO] = auto;

		JPanel manually = new ManualAddressPanel();
		panel.add(manually, panelNames[MANUALLY]);
		panels[MANUALLY] = manually;

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				switch(visiblePanel){
				case AUTO:
					cardLayout.show(panel, panelNames[visiblePanel = SELECTION]);
				}
			}
			@Override
			public void componentHidden(ComponentEvent e) {
				switch(visiblePanel){
				case AUTO:
					((AutoAddressPanel)panels[AUTO]).stop();
				}
			}
		});
	}

	public void setOwner(Window owner) {
		this.owner = owner;
	}
}
