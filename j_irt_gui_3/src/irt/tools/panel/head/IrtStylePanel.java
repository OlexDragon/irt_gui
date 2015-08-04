package irt.tools.panel.head;

import irt.tools.button.ImageButton;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class IrtStylePanel extends JPanel {
	public IrtStylePanel() {
	}
	private static final long serialVersionUID = 198707451482153465L;

	private final static Logger logger = (Logger) LogManager.getLogger();

    private int cornerWidth = 50;
	private int cornerHeight = 50;

	private boolean isArc = true;
	private int arcStart = -150;
	private int arcStep = 220;
	private int arcWidth = 200;

	private boolean isGradient = true;

	@Override
	protected void paintComponent(Graphics g) {
		synchronized (g) {
			logger.trace("paintComponent(Graphics g) {}", getClass().getSimpleName());
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHints(ImageButton.RENDERING);
			Color background = getBackground();
			if (isGradient)
				g2.setPaint(new GradientPaint(0, 0, background, 0, (int) (getHeight() / 1.5), background.darker().darker().darker(), true));
			else
				g2.setPaint(background);
			int w = getWidth() - 1;
			int h = getHeight() - 1;
			g2.fillRoundRect(0, 0, w, h, cornerWidth, cornerHeight);
			g2.setPaint(Color.WHITE);
			g2.drawRoundRect(0, 0, w, h, cornerWidth, cornerHeight);
			g2.setPaint(Color.BLACK);
			g2.drawRoundRect(2, 2, w - 4, h - 4, cornerWidth, cornerHeight);
			g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
			g2.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, getHeight() / 2, Color.BLUE, true));
			if (isArc) {
				int arcStart = this.arcStart;
				for (; arcStart < getWidth(); arcStart += arcStep)
					g2.draw(new Arc2D.Double(arcStart, 0, arcWidth, getHeight(), -42, 80, Arc2D.OPEN));
			}
			paintChildren(g2);
			g.dispose();
		}
	}

	public int getArcStep() {
		return arcStep;
	}

	public void setArcStep(int arcStep) {
		this.arcStep = arcStep;
		if(arcStep<=0)
			this.arcStep = 10;
	}

	public int getArcWidth() {
		return arcWidth;
	}

	public void setArcWidth(int arcWidth) {
		this.arcWidth = arcWidth;
	}

	public int getArcStart() {
		return arcStart;
	}

	public void setArcStart(int arcStart) {
		this.arcStart = arcStart;
	}

	public void setCorner(int corner){
		cornerHeight = cornerWidth = corner;
	}

	public boolean isGradient() {
		return isGradient;
	}

	public void setGradient(boolean isGradient) {
		this.isGradient = isGradient;
	}


	public boolean isArc() {
		return isArc;
	}


	public void setArc(boolean isArc) {
		this.isArc = isArc;
	}
}
