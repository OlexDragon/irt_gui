package irt.tools.CheckBox;

import irt.tools.button.ImageButton;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class SwitchBox extends JCheckBox {

	private Image offImage;
	private Image onImage;
	private boolean selected = true;

	public SwitchBox(Image offImage, Image onImage) {
		setOpaque(true);
		this.offImage = offImage;
		this.onImage = onImage;
	}

	@Override
	public void paintComponent(Graphics g) {
		Image image = selected ? onImage : offImage;
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHints(ImageButton.RENDERING);
		g2.setPaint(new Color(0, 0, 0, 0));
		g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
		g2.dispose();
		if (selected != isSelected()) {
			selected = isSelected();
			repaint();
		}
	}
}
