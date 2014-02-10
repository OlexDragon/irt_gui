package irt.tools.panel.subpanel;

import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;
import irt.tools.label.VarticalLabel;

import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

public class RedundancyPanelDemo extends JPanel implements Refresh{
	private static final long serialVersionUID = -7179255648628796498L;

//	private static final String UNKNOWN = "'Unknown',";
	protected static final String STANDBY = "'Standby',";
	protected static final String ONLINE = "'Online',";
	protected static final String SET_ONLINE = "Set Online";

	public enum REDUNDANCY{
		DISABLE("Desable"),
		ENABLE("Enable");
		
		private String redundancy;

		private REDUNDANCY(String redundancy){
			this.redundancy = redundancy;
		}

		@Override
		public String toString() {
			return redundancy;
		}

		public void setRedundancy(String redundancy) {
			this.redundancy = redundancy;
		}
	}

	public enum REDUNDANCY_MODE {
		COLD_STANDBY("Cold Standby"),
		HOT_STANDBY("Hot Standby");

		private String mode;

		private REDUNDANCY_MODE(String mode){
			this.mode = mode;
		}
		@Override
		public String toString() {
			return mode;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
	}

	public enum REDUNDANCY_NAME {
		NO_NAME(null),
		BUC_A("BUC A"),
		BUC_B("BUC B");

		private String name;

		private REDUNDANCY_NAME(String name){
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}

	private JComboBox<REDUNDANCY_MODE> cmbBxMode;
	private JComboBox<REDUNDANCY> cmbBxRedundancy;
	private JComboBox<REDUNDANCY_NAME> cmbBxName;
	private ImageLabel lblImage;
	private VarticalLabel lblSetOnline;
	private JLabel lblRedundancy;
	private JLabel lblMode;
	private JLabel lblUnitName;

	public RedundancyPanelDemo() {
		setBackground(SystemColor.inactiveCaption);

		
		Font font = Translation.getFont().deriveFont(Translation.getValue(Float.class, "redundancy.lable.font.size", 14f));

		lblRedundancy = new JLabel(Translation.getValue(String.class, "redundancy", "Redundancy"));
		lblRedundancy.setFont(font);
		
		lblMode = new JLabel(Translation.getValue(String.class, "redundancy.mode", "Mode"));
		lblMode.setFont(font);
		
		lblUnitName = new JLabel(Translation.getValue(String.class, "redundancy.unit_name", "Unit Name"));
		lblUnitName.setFont(font);
		
		lblImage = new ImageLabel(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/BUC_X.jpg")), null);

		font = font.deriveFont(Translation.getValue(Float.class, "redundancy.combobox.font.size", 12f));

		DefaultComboBoxModel<REDUNDANCY_MODE> modeModel = new DefaultComboBoxModel<>();
		REDUNDANCY_MODE[] values = REDUNDANCY_MODE.values();
		for(REDUNDANCY_MODE rm:values){
			String name = rm.name();
			rm.setMode(Translation.getValue(String.class, name, name));
			modeModel.addElement(rm);
		}
		cmbBxMode = new JComboBox<>(modeModel);
		cmbBxMode.setFont(font);
		
		DefaultComboBoxModel<REDUNDANCY> redundancyModel = new DefaultComboBoxModel<>();
		REDUNDANCY[] rs = REDUNDANCY.values();
		for(REDUNDANCY r:rs){
			String name = r.name();
			r.setRedundancy(Translation.getValue(String.class, name, name));
			redundancyModel.addElement(r);
		}
		cmbBxRedundancy = new JComboBox<>(redundancyModel);
		cmbBxRedundancy.setFont(font);
		
		DefaultComboBoxModel<REDUNDANCY_NAME> nameModel = new DefaultComboBoxModel<>();
		REDUNDANCY_NAME[] ns = REDUNDANCY_NAME.values();
		for(REDUNDANCY_NAME n:ns)
			nameModel.addElement(n);
		cmbBxName = new JComboBox<>(nameModel);
		cmbBxName.setFont(font);
		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.BLACK, Color.LIGHT_GRAY, SystemColor.window, SystemColor.activeCaption));

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(lblMode, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblUnitName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblRedundancy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(cmbBxName, 0, 220, Short.MAX_VALUE)
								.addComponent(cmbBxRedundancy, 0, 220, Short.MAX_VALUE)
								.addComponent(cmbBxMode, Alignment.TRAILING, 0, 220, Short.MAX_VALUE))
							.addGap(13))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(panel, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
							.addGap(8)
							.addComponent(lblImage, 0, 276, Short.MAX_VALUE)
							.addContainerGap())))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRedundancy)
						.addComponent(cmbBxRedundancy, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMode, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
						.addComponent(cmbBxMode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblUnitName, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
						.addComponent(cmbBxName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
						.addComponent(lblImage, 0, 194, Short.MAX_VALUE))
					.addContainerGap())
		);

		String text = Translation.getValue(String.class, "SET_ONLINE", "Set Online");
		lblSetOnline = new VarticalLabel(text, false);
		lblSetOnline.setToolTipText(text);
		lblSetOnline.setHorizontalAlignment(SwingConstants.CENTER);
		lblSetOnline.setFont(font);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(lblSetOnline, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addComponent(lblSetOnline, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
		);
		panel.setLayout(gl_panel);
		setLayout(groupLayout);
	}


	@Override
	public void refresh() {
		Font font = Translation.getFont().deriveFont(Translation.getValue(Float.class, "redundancy.lable.font.size", 14f));

		lblRedundancy.setText(Translation.getValue(String.class, "redundancy", "Redundancy"));
		lblRedundancy.setFont(font);

		lblMode.setText(Translation.getValue(String.class, "redundancy.mode", "Mode"));
		lblMode.setFont(font);

		lblUnitName.setText(Translation.getValue(String.class, "redundancy.unit_name", "Unit Name"));
		lblUnitName.setFont(font);

		String text = Translation.getValue(String.class, lblSetOnline.getName(), SET_ONLINE);
		lblSetOnline.setText(text);
		lblSetOnline.setToolTipText(text);
		lblSetOnline.setFont(font);

		font = font.deriveFont(Translation.getValue(Float.class, "redundancy.combobox.font.size", 12f));

		for(int i=0; i<cmbBxMode.getItemCount(); i++){
			REDUNDANCY_MODE itemAt = cmbBxMode.getItemAt(i);
			String name = itemAt.name();
			itemAt.setMode(Translation.getValue(String.class, name, name));
		}
		cmbBxMode.setFont(font);

		for(int i=0; i<cmbBxRedundancy.getItemCount(); i++){
			REDUNDANCY itemAt = cmbBxRedundancy.getItemAt(i);
			String name = itemAt.name();
			itemAt.setRedundancy(Translation.getValue(String.class, name, name));
		}
		cmbBxRedundancy.setFont(font);
	}
}
