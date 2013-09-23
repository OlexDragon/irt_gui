package irt.tools.label;

import irt.irt_gui.IrtGui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class ImageLabel extends JLabel{

	public ImageLabel(ImageIcon icon, String text){
		super(text);
		setIcon((Icon) (icon!=null ? icon : IrtGui.class.getResource("/irt/irt_gui/images/logo.gif")));
		setOpaque(false);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.drawImage(((ImageIcon)getIcon()).getImage(), 1, 1, getWidth()-2, getHeight()-2, this);
		
	}
}
