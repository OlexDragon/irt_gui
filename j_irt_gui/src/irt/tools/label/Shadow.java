package irt.tools.label;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

@SuppressWarnings("serial")
public class Shadow extends JLabel {

	private int arc = 50;

	public Shadow(){
		setBackground(new Color(0,0,0,arc));
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(getBackground());
		g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
		g2.dispose();
	}

	public int getArc() {
		return arc;
	}

	public void setArc(int arc) {
		this.arc = arc;
	}
}
