package irt.tools.panel.vizards.address;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ButtonGroup;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class StyleSelectorPanel extends JPanel {

	public enum Selection{
		AUTO,
		MANUALLY;

		private int unitsNumber;
		public int getUnitsNumber() {
			return unitsNumber;
		}
		public Selection setUnitsNumber(int unitsNumber) {
			this.unitsNumber = unitsNumber;
			return this;
		}
	}

	private Selection selection = Selection.AUTO;

	public StyleSelectorPanel() {
		
		JRadioButton rdbtnFindAddressAutomatically = new JRadioButton("Find Address Automatically");
		rdbtnFindAddressAutomatically.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selection = Selection.AUTO;
			}
		});
		rdbtnFindAddressAutomatically.setFont(new Font("Tahoma", Font.BOLD, 12));
		rdbtnFindAddressAutomatically.setSelected(true);
		buttonGroup.add(rdbtnFindAddressAutomatically);

		final JRadioButton rdbtnSetNumberOf = new JRadioButton("Set Adress Manually");
		rdbtnSetNumberOf.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				selection = Selection.MANUALLY.setUnitsNumber(getUnitNumber());
			}

			private int getUnitNumber() {
				String text = textField.getText();
				if(text==null || (text=text.replaceAll("\\D", "")).isEmpty()){
					textField.setText("1");
					return 1;
				}else
					return Integer.parseInt(text);
			}
		});
		rdbtnSetNumberOf.setFont(new Font("Tahoma", Font.BOLD, 12));
		buttonGroup.add(rdbtnSetNumberOf);
		
		textField = new JTextField();
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				textField.selectAll();
				rdbtnSetNumberOf.setSelected(true);
			}
			@Override
			public void focusLost(FocusEvent e) {
				String text = textField.getText();
				if(text!=null && !(text=text.replaceAll("\\D", "")).isEmpty())
					selection.setUnitsNumber(Integer.parseInt(text));
			}
		});
		textField.setForeground(Color.BLACK);
		textField.setColumns(10);
		
		JLabel lblSetNumberOf = new JLabel("Set Number Of Units");
		lblSetNumberOf.setForeground(Color.DARK_GRAY);
		lblSetNumberOf.setFont(new Font("Tahoma", Font.BOLD, 12));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(rdbtnFindAddressAutomatically)
						.addComponent(rdbtnSetNumberOf)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(83)
							.addComponent(lblSetNumberOf)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(67, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(30)
					.addComponent(rdbtnFindAddressAutomatically)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rdbtnSetNumberOf)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblSetNumberOf)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(44, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}
	private static final long serialVersionUID = 7707714410994814953L;
	private JTextField textField;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	public Selection getSelection() {
		return selection;
	}
}
