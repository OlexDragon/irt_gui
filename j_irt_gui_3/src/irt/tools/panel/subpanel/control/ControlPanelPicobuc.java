package irt.tools.panel.subpanel.control;

import irt.controller.control.ControlControllerPicobuc;
import irt.controller.control.ControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.Listeners;
import irt.data.packet.LinkHeader;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;

@SuppressWarnings("serial")
public class ControlPanelPicobuc extends ControlPanel {

	private JComboBox<String>  cbLoSelect;
	private JLabel lblSave;

	public ControlPanelPicobuc(LinkHeader linkHeader) {
		super(linkHeader, ControlPanel.FLAG_ATTENUATION|ControlPanel.FLAG_FREQUENCY);
		
		Font font = Translation.getFont().deriveFont(16f);

		cbLoSelect = new JComboBox<String>();
		cbLoSelect.setName("LO Select");
		cbLoSelect.setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0; }};}});
		cbLoSelect.addPopupMenuListener(Listeners.popupMenuListener);
		cbLoSelect.setForeground(Color.YELLOW);
		cbLoSelect.setBackground(color);
		cbLoSelect.setCursor(cursor);
		cbLoSelect.setFont(font);
		cbLoSelect.setBounds(10, 141, 194, 26);
		add(cbLoSelect);
		
		font = font.deriveFont(new Float(properties.getProperty("control.label.mute.font.size_"+selectedLanguage)))
				.deriveFont(Font.BOLD);

		lblSave = new JLabel(Translation.getValue(String.class, "save", "SAVE"));
		lblSave.setHorizontalAlignment(SwingConstants.LEFT);
		lblSave.setForeground(Color.YELLOW);
		lblSave.setFont(font);
		String property;
		int x = (property = properties.getProperty("control.label.save.x_"+selectedLanguage))!=null ? Integer.parseInt(property): 153;
		int y = (property = properties.getProperty("control.label.save.y_"+selectedLanguage))!=null ? Integer.parseInt(property): 107;
		int width = (property = properties.getProperty("control.label.save.width_"+selectedLanguage))!=null ? Integer.parseInt(property): 61;
		lblSave.setBounds(x, y, width, 20);
		add(lblSave);
		((JLabel)cbLoSelect.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected ControllerAbstract getNewController() {
		return new ControlControllerPicobuc(getLinkHeader(),this);
	}

	@Override
	protected Point getConfigButtonPosition() {
		String property;
		int x = (property = properties.getProperty("control.button.save.x_"+selectedLanguage))!=null ? Integer.parseInt(property): 118;
		int y = (property = properties.getProperty("control.button.save.y_"+selectedLanguage))!=null ? Integer.parseInt(property): 101;
		return new Point(x, y);
	}

	@Override
	protected Point getMuteButtonPosition() {
		String property;
		int x = (property = properties.getProperty("control.button.mute.x_"+selectedLanguage))!=null ? Integer.parseInt(property): 14;
		int y = (property = properties.getProperty("control.button.mute.y_"+selectedLanguage))!=null ? Integer.parseInt(property): 101;
		return new Point(x, y);
	}

	@Override
	public void refresh() {
		super.refresh();
		Font font = Translation.getFont().deriveFont(new Float(properties.getProperty("control.label.mute.font.size_"+selectedLanguage)))
				.deriveFont(Font.BOLD);
		lblSave.setText(Translation.getValue(String.class, "save", "SAVE"));
		lblSave.setFont(font);
		String property;
		int x = (property = properties.getProperty("control.label.save.x_"+selectedLanguage))!=null ? Integer.parseInt(property): 153;
		int y = (property = properties.getProperty("control.label.save.y_"+selectedLanguage))!=null ? Integer.parseInt(property): 107;
		int width = (property = properties.getProperty("control.label.save.width_"+selectedLanguage))!=null ? Integer.parseInt(property): 61;
		lblSave.setBounds(x, y, width, 20);
	}

}
