package irt.tools.panel.wizards.address;

import irt.controller.GuiController;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.tools.panel.wizards.address.AddressWizard.Selection;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingWorker;

public class AWStyleSelectorPanel extends JPanel implements Refresh{

	private final String MIN_MAX_UNIT_COUNT;

	private static final String UNIT_COUNT = "unit_count";

//	private static final Logger logger = (Logger) LogManager.getLogger();

	private final Preferences prefs;

	private Selection selection  = Selection.MANUALLY;

	private JLabel lblSetNumberOf;
	private JRadioButton rdbtnFindAddressAutomatically;
	private JRadioButton rdbtnSetManually;

	public AWStyleSelectorPanel() {

		MIN_MAX_UNIT_COUNT = "(1-"+AddressWizard.MAX_ADDRESS+ ")";

		prefs = GuiController.getPrefs();
		Font font = Translation.getFont().deriveFont(12f);
		
		rdbtnFindAddressAutomatically = new JRadioButton(Translation.getValue(String.class, "set_auto_addr_find", "Find Address Automatically"));
		rdbtnFindAddressAutomatically.setEnabled(false);
		rdbtnFindAddressAutomatically.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selection = Selection.AUTO;
				setUnitsCount();
			}
		});
		rdbtnFindAddressAutomatically.setFont(font);
		buttonGroup.add(rdbtnFindAddressAutomatically);

		rdbtnSetManually = new JRadioButton(Translation.getValue(String.class, "set_addr_manualy", "Set Address Manually"));
		rdbtnSetManually.setSelected(true);
		rdbtnSetManually.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selection = Selection.MANUALLY;
				setUnitsCount();
			}
		});
		rdbtnSetManually.setFont(font);
		buttonGroup.add(rdbtnSetManually);
		
		textField = new JTextField(prefs.get(UNIT_COUNT, "1"));
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				textField.selectAll();
			}
			@Override
			public void focusLost(FocusEvent e) {
				setUnitsCount();
			}
		});
		textField.setForeground(Color.BLACK);
		textField.setColumns(10);
		
		lblSetNumberOf = new JLabel(Translation.getValue(String.class, "set_unit_count", "Set Number Of Units")+MIN_MAX_UNIT_COUNT);
		lblSetNumberOf.setForeground(Color.BLACK);
		lblSetNumberOf.setFont(font);
		
		JRadioButton rdbtnNoRedandancy = new JRadioButton("Default");
		rdbtnNoRedandancy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selection = Selection.NON;
			}
		});
		buttonGroup.add(rdbtnNoRedandancy);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(79)
							.addComponent(lblSetNumberOf)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(47)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(rdbtnFindAddressAutomatically)
								.addComponent(rdbtnSetManually)
								.addComponent(rdbtnNoRedandancy))))
					.addContainerGap(84, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblSetNumberOf)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rdbtnFindAddressAutomatically)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rdbtnSetManually)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rdbtnNoRedandancy)
					.addContainerGap(32, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}
	private static final long serialVersionUID = 7707714410994814953L;
	private JTextField textField;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	public Selection getSelection() {
		return selection;
	}

	private void setUnitsCount() {
		String text = textField.getText();
		int unitsCount = text!=null && !(text = text.replaceAll("\\D", "")).isEmpty() ? Integer.parseInt(text) : 1;
		Selection.setUnitsCount( unitsCount);

		if(prefs.getInt(UNIT_COUNT, 1)!=unitsCount)
			prefs.putInt(UNIT_COUNT, unitsCount);
	}

	@Override
	public void refresh() {
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				Font font = Translation.getFont().deriveFont(12f);

				lblSetNumberOf.setText(Translation.getValue(String.class, "set_unit_count", "Set Number Of Units")+MIN_MAX_UNIT_COUNT);
				rdbtnFindAddressAutomatically.setText(Translation.getValue(String.class, "set_auto_addr_find", "Find Address Automatically"));
				rdbtnSetManually.setText(Translation.getValue(String.class, "set_addr_manualy", "Set Address Manually"));

				lblSetNumberOf.setFont(font);
				rdbtnFindAddressAutomatically.setFont(font);
				rdbtnSetManually.setFont(font);
				return null;
			}
		}.execute();
	}

	public void setSelection(Selection selection) {
		this.selection = selection;
	}
}
