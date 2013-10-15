package irt.tools.panel.head;

import irt.IrtSTM32Flash;
import irt.tools.label.ImageLabel;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class IrtPanel extends MainPanel {

	public enum Style {
		WITH_IMAGE,
		WITHOUT_IMAGE
	}

	public IrtPanel(JFrame target) {
		this(target, (ImageIcon)null, "IRT Technologies", new Font("Tahoma", Font.BOLD, 18), Style.WITH_IMAGE);
	}

	public IrtPanel(JFrame target, String text, Font font) {
		this(target, (ImageIcon)null, text, font,  Style.WITHOUT_IMAGE);
	}

	private IrtPanel(JFrame target, ImageIcon imageIcon, String text, Font font, Style style) {
		super(target, 235);
		setCorner(30);
		setGradient(false);
		setSize(1, 46);
		setArcStart(14);
		setArcWidth(45);
		setBackground(new Color(0x3B, 0x4A, 0x8B, 100));

		JLabel lblIrtTechnologies;
		if(style==Style.WITH_IMAGE){
			if(imageIcon==null)
				imageIcon = new ImageIcon(IrtSTM32Flash.class.getResource("images/logo.gif"));
			lblIrtTechnologies = new ImageLabel(imageIcon, text);
			lblIrtTechnologies.setBounds(10, 3, 40, 40);
			lblIrtTechnologies.setHorizontalAlignment(SwingConstants.RIGHT);
			lblIrtTechnologies.setForeground(new Color(176, 224, 230));
			lblIrtTechnologies.setFont(font);
			add(lblIrtTechnologies);
		}
		
		JLabel lblText = new JLabel(text);
		lblText.setBounds(68, 13, 166, 20);
		lblText.setHorizontalAlignment(SwingConstants.LEFT);
		lblText.setForeground(new Color(0x3B, 0x4A, 0x8B));
		lblText.setFont(font);
		add(lblText);
		
		JLabel lblShadow = new JLabel(text);
		lblShadow.setHorizontalAlignment(SwingConstants.LEFT);
		lblShadow.setForeground(Color.WHITE);
		lblShadow.setFont(font);
		lblShadow.setBounds(lblText.getX()-1, lblText.getY()-1, 166, 20);
		add(lblShadow);
	}
}
