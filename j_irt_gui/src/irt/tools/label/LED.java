package irt.tools.label;

import irt.controller.BlinkControl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

@SuppressWarnings("serial")
public class LED extends JLabel{

	private Color color;
	private int off = 30;
	private volatile boolean isOn;
	private BlinkControl blinkControl;

	public LED(Color color, String title) {
		super(title);
		this.color = color;
		setHorizontalAlignment(RIGHT);
		setOn(false);
	}

	public void setOn(boolean isOn){
		if(isOn)
			setLedColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
		else{
			setLedColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), off ));
		}
		if(isShowing() && this.isOn != isOn){
			this.isOn = isOn;
		}
	}

	@Override
	public void paintComponent(Graphics g) {

		Graphics2D g2 = (Graphics2D)g;
		g2.setPaint(getBackground());
		g2.setStroke(new BasicStroke(2));
		int start = 1;
		int height = getHeight()-start*2;
		g2.setPaint(new GradientPaint(1, 1, Color.WHITE.brighter(), height, height, Color.WHITE.darker().darker()));
		g2.drawOval(start, start, height, height);
		start += 2;
		height = getHeight()-start*2;
		g2.setPaint(new GradientPaint(1, 1, Color.BLACK.brighter().brighter(), height, height, Color.BLACK));
		g2.drawOval(start, start, height, height);
		start += 2;
		height = getHeight()-start*2;
		g2.setPaint(new GradientPaint(1, 1, color.brighter(), height, height, color.darker()));
		g2.fillOval(start, start, height, height);
		
		super.paintComponent(g);
		g2.dispose();
	}

	public void blink() {
		if(blinkControl==null)
			blinkControl = new BlinkControl(this);
		blinkControl.blink();
	}

//	public void blink(int time, boolean repeat){
//		blink();
//		blinkControl.setBlinkTime(time);
//		blinkControl.setBlink(repeat);
//	}

	public boolean isOn() {
		return isOn;
	}

	public boolean isBlink() {
		return blinkControl.isBlink();
	}

	public Color getLedColor() {
		return color;
	}

	public void setLedColor(Color color) {
		if(!color.equals(this.color)){
			firePropertyChange("LedColor", this.color, color);
			this.color = color;
			repaint();
		}
	}
}
