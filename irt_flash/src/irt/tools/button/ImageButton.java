package irt.tools.button;

import irt.tools.label.Shadow;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageButton extends JButton {

	private Image image;
	private boolean isIn;
	private int radius = 50;
	private boolean isPressed;
	private Shadow shadow;
	private int shadowShiftX = 10;
	private int shadowShiftY = 10;
	private int shadowPressedShiftX = 5;
	private int shadowPressedShiftY = 5;
	private Color shadowColor = new Color(255,255,255,50);
	private int textX;
	private int textY;

	public final static Map<RenderingHints.Key,Object> RENDERING = new HashMap<>();

	public ImageButton(){
		RENDERING.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		RENDERING.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		RENDERING.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		RENDERING.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		RENDERING.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		RENDERING.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints. VALUE_STROKE_NORMALIZE );
		RENDERING.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}

	public ImageButton(Image image) {
		this();
		this.image = image;
	}

	@Override
	public void paintComponent(Graphics g) {
		if(isShowing()){
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHints(RENDERING);
			Color background = isIn ? isPressed ? getBackground().darker() : getBackground().brighter() : getBackground();
			g2.setPaint(new GradientPaint(0,0, background, 0,(int)(getHeight()/1.5), background.darker().darker().darker(), true));
			g2.fillRoundRect(1, 1, getWidth()-4, getHeight()-4, radius, radius);
			if(isIn)
				if(isPressed){
					g2.drawImage(image, 4, 4, getWidth()-8, getHeight()-8, this);
					if(shadow!=null)
						shadow.setLocation(getX()+shadowPressedShiftX, getY()+shadowPressedShiftY);
				}else{
					g2.drawImage(image, 1, 1, getWidth()-4, getHeight()-4, this);
					if(shadow!=null)
						shadow.setLocation(getX()+shadowShiftX, getY()+shadowShiftY);
				}
			else{
				g2.drawImage(image, 2, 2, getWidth()-6, getHeight()-6, this);
			}
			g2.dispose();
		}
	}

	@Override
	protected void paintBorder(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHints(RENDERING);
		Color background = isIn ? getBackground().brighter() : getBackground().darker();
		g2.setStroke(isIn ? new BasicStroke(2) : new BasicStroke(1));
		g2.setPaint(new GradientPaint(0,0, background.brighter().brighter().brighter(), 0,getHeight()/2, background, true));
		if(isIn)
			if(isPressed){
				g2.drawRoundRect(4, 4, getWidth()-8, getHeight()-8, radius, radius);
			}else{
				g2.drawRoundRect(1, 1, getWidth()-4, getHeight()-4, radius, radius);
			}
		else{
			g2.drawRoundRect(2, 2, getWidth()-6, getHeight()-6, radius, radius);
		}
		if(shadow==null){
			shadow = new Shadow();
			shadow.setBackground(shadowColor);
			shadow.setBounds(getX()+shadowShiftX, getY()+shadowShiftY, getWidth(), getHeight());
			shadow.setVisible(true);
			((JPanel)getParent()).add(shadow);
		}
		g2.setPaint(getForeground());
		g2.drawString(getText(), textX, textY);
		g2.dispose();
	}

	@Override
	public boolean contains(int x, int y) {
		boolean contains = super.contains(x, y);
		if(isIn!=contains){
			isIn = contains;
			getParent().repaint(getX()-1,getY()-1,getWidth(),getHeight());
		}if(!isIn && isPressed)
			setPressed(false);
		return contains;
	}

	@Override
	protected void fireActionPerformed(ActionEvent actionEvent) {
		setPressed(false);
		super.fireActionPerformed(actionEvent);
	}

	@Override
	public long getMultiClickThreshhold() {
		if(isIn)
			setPressed(true);
		return super.getMultiClickThreshhold();
	}

	public int getShadowShiftX() {
		return shadowShiftX;
	}

	public void setShadowShiftX(int sadowShiftX) {
		this.shadowShiftX = sadowShiftX;
	}

	public int getShadowShiftY() {
		return shadowShiftY;
	}

	public void setShadowShiftY(int sadowShiftY) {
		this.shadowShiftY = sadowShiftY;
	}

	public int getShadowPressedShiftX() {
		return shadowPressedShiftX;
	}

	public void setShadowPressedShiftX(int shadowPressedShiftX) {
		this.shadowPressedShiftX = shadowPressedShiftX;
	}

	public int getShadowPressedShiftY() {
		return shadowPressedShiftY;
	}

	public void setShadowPressedShiftY(int shadowPressedShiftY) {
		this.shadowPressedShiftY = shadowPressedShiftY;
	}

	private void setPressed(boolean isPressed) {
		if(this.isPressed != isPressed){
			this.isPressed = isPressed;
			getParent().repaint(getX()-1,getY()-1,getWidth(),getHeight());
		}
	}

	public int getTextX() {
		return textX;
	}

	public int getTextY() {
		return textY;
	}

	public void setTextX(int textX) {
		this.textX = textX;
	}

	public void setTextY(int textY) {
		this.textY = textY;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

}