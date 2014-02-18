package irt.tools.panel.wizards.address;

import irt.controller.GuiController;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class AddressWizard extends JDialog implements Refresh{

	public static final String REDUNDANCY_ADDRESSES = "addresses";
	public static final String REDUNDANCY_DEFAULT_ADDRESSES = "253,254";

	private static final long serialVersionUID = -8162692468959448079L;

	private static final Logger logger = (Logger) LogManager.getLogger();
	private static final Preferences prefs = GuiController.getPrefs();

	private static final AddressWizard ADDRESS_VIZARD = new AddressWizard();

	public static final int MAX_ADDRESS = 254;

	public enum Selection{
		DEFAULT,
		NON,
		SELECT,
		AUTO,
		MANUALLY;

		private static int unitsCount = 1;
		public static int getUnitsCount() {
			return unitsCount;
		}
		public static void setUnitsCount(int unitsCount) {
			Selection.unitsCount = unitsCount;
		}
		public static int size() {
			return 3;
		}
	}

	private Window owner;
	private CardLayout cardLayout;

	private Selection visiblePanel,
						nextPanel;

	private JPanel					cardLayoutPanel;
	private AutoAddressPanel		autoAddressPanel;
	private AWStyleSelectorPanel	styleSelectorPanel;

	private JButton btnBack;
	private JButton btnNext;

	private JButton btnCansel;

	private ManualAddressPanel manually;

	public static AddressWizard getInstance() {
		return ADDRESS_VIZARD;
	}

	private AddressWizard() {
		setMinimumSize(new Dimension(350, 220));
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout(0, 0));
			
		cardLayoutPanel = new JPanel();
		getContentPane().add(cardLayoutPanel, BorderLayout.CENTER);
		cardLayout = new CardLayout(0, 0);
		cardLayoutPanel.setLayout(cardLayout);

		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.SOUTH);
		
		btnBack = new JButton("<"+Translation.getValue(String.class, "back", "Back"));
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextPanel = Selection.SELECT;
				showPanel();
			}
		});
		toolBar.add(btnBack);

		btnNext = new JButton(Translation.getValue(String.class, "next", "Next")+">");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.trace(visiblePanel);
				new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception {
						switch(visiblePanel){
						case DEFAULT:
							break;
						case AUTO:
							break;
						case MANUALLY:
							manualActionPerformed();
							break;
						case SELECT:
							selectActionPerformed();
						case NON:
						}
						showPanel();
						return null;
					}

					private void selectActionPerformed() {
						nextPanel = styleSelectorPanel.getSelection();
						if(nextPanel==Selection.NON){
							boolean addresses = prefs.get(REDUNDANCY_ADDRESSES, null)!=null;
							if(addresses
									&& JOptionPane.showConfirmDialog(
										owner,
										"Do you really want to delete addresses "+addresses,"Delete Adddresses",
										JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)
								prefs.remove(REDUNDANCY_ADDRESSES);

							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									setVisible(false);
								}
							});
						}
					}
				}.execute();
			}

			private void manualActionPerformed() {
				boolean set = true;
				TreeSet<Object> addrs = new TreeSet<>();
				for(JTextField tf:manually.getTextFields()){
					String text = tf.getText();
					if(text==null || (text = text.replaceAll("\\D", "")).isEmpty()){
						set = false;
						JOptionPane.showMessageDialog(owner, Translation.getValue(String.class, "all_field", "All fields must be filled."));
						break;
					}else{
						
						if(!addrs.add(Integer.parseInt(text))){
							set = false;
							JOptionPane.showMessageDialog(owner, Translation.getValue(String.class, "not_repeated", "Addresses should not be repeated."));
							break;
						}
					}
				}

				if(!addrs.isEmpty()
						&& set
						&& JOptionPane.showConfirmDialog(
								owner,
								"Are addresses correct?\n"+addrs.toString(),
								"Address setting",
								JOptionPane.OK_CANCEL_OPTION
						)==JOptionPane.OK_OPTION){
					prefs.put(REDUNDANCY_ADDRESSES, addrs.toString());

					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							setVisible(false);
						}
					});
				}
			}
		});
		toolBar.add(btnNext);

		setVisiblePanel(Selection.SELECT);

		btnCansel = new JButton("Cansel");
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

		styleSelectorPanel = new AWStyleSelectorPanel();
		cardLayoutPanel.add(styleSelectorPanel, Selection.SELECT.name());

		autoAddressPanel = new AutoAddressPanel();
		cardLayoutPanel.add(autoAddressPanel, Selection.AUTO.name());

		manually = new ManualAddressPanel();
		cardLayoutPanel.add(manually, Selection.MANUALLY.name());

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				showPanel();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				switch(visiblePanel){
				case AUTO:
					autoAddressPanel.stop();
					setVisiblePanel(Selection.SELECT);
				case MANUALLY:
					break;
				case SELECT:
					break;
				default:
					break;
				}
			}
		});

		refresh();
	}

	public void setOwner(Window owner) {
		this.owner = owner;
	}

	private void showPanel() {
		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				try{
					logger.trace("visiblePanel={}, nextPanel={}", visiblePanel, nextPanel);

					if(validateSelection(nextPanel!=null ? nextPanel : visiblePanel))
						cardLayout.show(cardLayoutPanel, visiblePanel.name());

				}catch(Exception ex){
					logger.catching(ex);
				}
				return null;
			}
		}.execute();
	}

	private boolean validateSelection(Selection selection) {
		boolean valid = true;
		logger.entry(selection);

		if(selection!=null){

			int unitsNumber = Selection.getUnitsCount();
			if(unitsNumber>0 && unitsNumber<=MAX_ADDRESS)
				setVisiblePanel(selection);
			else{
				String message = "The number of units should be between 0 and "+(MAX_ADDRESS+1);
				logger.trace(message);
				JOptionPane.showMessageDialog(owner, message);
				valid = false;
			}
		}else
			valid = false;

		return logger.exit(valid);
	}

	@Override
	public void refresh() {
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				setTitle(Translation.getValue(String.class, "address_wizard", "Address Wizard..."));
				for(Component r:cardLayoutPanel.getComponents())
					if(r instanceof Refresh)
						((Refresh)r).refresh();

				btnBack.setText("<"+Translation.getValue(String.class, "back", "Back"));
				btnNext.setText(Translation.getValue(String.class, "next", "Next")+">");
				btnCansel.setText(Translation.getValue(String.class, "cancel", "Cancel"));
				return null;
			}
		}.execute();
	}

	public Selection getVisiblePanel() {
		return visiblePanel;
	}

	public void setVisiblePanel(Selection visiblePanel) {
		this.visiblePanel = visiblePanel;
		switch(visiblePanel){
		case SELECT:
			btnBack.setEnabled(false);
			break;
		default:
			btnBack.setEnabled(true);
		}
	}
}
