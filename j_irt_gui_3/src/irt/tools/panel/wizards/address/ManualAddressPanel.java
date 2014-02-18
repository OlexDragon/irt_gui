package irt.tools.panel.wizards.address;

import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.tools.panel.wizards.address.AddressWizard.Selection;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class ManualAddressPanel extends JScrollPane implements Refresh{
	private static final long serialVersionUID = -8327621688270673147L;

	private JPanel panel;
	private List<JTextField> textFields = new ArrayList<>();
	private Font font;

	public ManualAddressPanel() {
		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				panel.removeAll();

				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{20, 0, 20, 0};

				int unitsCount = Selection.getUnitsCount();
				int[] rowHeights = new int[unitsCount*2];
				for(int i=0; i<unitsCount; i++){
					int j = i*2;
					rowHeights[j] = 10;
					rowHeights[j+1] = 0;
				}
				gbl_panel.rowHeights = rowHeights;
//				gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
//				gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);		

				String text = Translation.getValue(String.class, "address", "Address");
				font = Translation.getFont().deriveFont(14f);

				for(int i=0; i<unitsCount; ){
					int rowNumber = i*2+1;
					addLable(rowNumber, text+" "+(++i));
					addTextField(rowNumber);
				}
				panel.revalidate();
				panel.repaint();
			}
			private void addTextField(int rowNumber) {
				JTextField textField = new JTextField();
				textField.setFont(font);
				GridBagConstraints gbc_textField = new GridBagConstraints();
				gbc_textField.anchor = GridBagConstraints.WEST;
				gbc_textField.gridx = 3;
				gbc_textField.gridy = rowNumber;
				panel.add(textField, gbc_textField);
				textField.setColumns(10);
				textFields.add(textField);
			}
			private void addLable(int rowNumber, String text) {
				JLabel lblNewLabel = new JLabel(text);
				lblNewLabel.setFont(font);
				GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
				gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
				gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
				gbc_lblNewLabel.gridx = 1;
				gbc_lblNewLabel.gridy = rowNumber;
				panel.add(lblNewLabel, gbc_lblNewLabel);
			}
			public void ancestorMoved(AncestorEvent event) { }
			public void ancestorRemoved(AncestorEvent event) { }
		});

		panel = new JPanel();
		setViewportView(panel);
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	public List<JTextField> getTextFields() {
		return textFields;
	}
}
