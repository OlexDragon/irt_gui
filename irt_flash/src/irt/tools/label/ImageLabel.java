package irt.tools.label;


import irt.IrtSTM32Flash;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class ImageLabel extends JLabel{

	public ImageLabel(ImageIcon icon, String text){
		super(text);
		setIcon((icon!=null ? icon : new ImageIcon(IrtSTM32Flash.class.getResource("/irt/images/logo.gif"))));
		setOpaque(false);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.drawImage(((ImageIcon)getIcon()).getImage(), 1, 1, getWidth()-2, getHeight()-2, this);
	}
}
