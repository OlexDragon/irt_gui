package irt.tools.panel.head;

import irt.tools.button.ImageButton;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	private final int PANEL_WIDTH;

    private JFrame target = null;
    private Point draggingAnchor = null;

    private int cornerWidth = 50;
	private int cornerHeight = 50;
	private int arcStart = -150;
	private int arcStep = 220;
	private int arcWidth = 200;
	private Timer timer;

	private boolean increase = true;

	private boolean isGradient = true;

	public MainPanel(final JFrame target, int width) {
		PANEL_WIDTH = width;
		this.target = target;
		this.addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				draggingAnchor = new Point(e.getX() + getX(), e.getY() + getY());
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				MainPanel.this.target.setLocation(e.getLocationOnScreen().x - draggingAnchor.x, e.getLocationOnScreen().y - draggingAnchor.y);
			}
		});
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		setLayout(null);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHints(ImageButton.RENDERING);
		Color background = getBackground();
		if(isGradient)
			g2.setPaint(new GradientPaint(0,0, background, 0,(int)(getHeight()/1.5), background.darker().darker().darker(), true));
		else
			g2.setPaint(background);
		int w = getWidth()-1;
		int h = getHeight()-1;
		g2.fillRoundRect(0, 0, w, h, cornerWidth, cornerHeight);
		g2.setPaint(Color.WHITE);
		g2.drawRoundRect(0, 0, w, h, cornerWidth, cornerHeight);
		g2.setPaint(Color.BLACK);
		g2.drawRoundRect(2, 2, w-4, h-4, cornerWidth, cornerHeight);

		g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		g2.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, getHeight()/2, Color.BLUE, true));

		int arcStart = this.arcStart;
		for(;arcStart<getWidth(); arcStart+=arcStep)
			g2.draw(new Arc2D.Double(arcStart, 0, arcWidth, getHeight(), -42, 80, Arc2D.OPEN));

		paintChildren(g2);
		g.dispose();
	}

	@Override
	public void setVisible(boolean aFlag) {
		if(increase = aFlag)
			super.setVisible(aFlag);
		horizontalIncrease();
	}

	public void horizontalIncrease() {
		if (timer == null){

			timer = new Timer(1, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int width = getWidth();

					setSize(width+(increase ? 8 : -8), getHeight());

					if ((width>=PANEL_WIDTH && increase) || (width<=1 && !increase)){
						timer.stop();
						if(!increase)
							setUnvisible();
						else
							setSize(PANEL_WIDTH, getHeight());
					}
				}

			});

			timer.setRepeats(true);
			timer.start();
		}else
			timer.restart();
	}
	public void setUnvisible() {
		super.setVisible(increase=false);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		if(getParent()!=null && getParent().getParent()!=null)
			getParent().getParent().repaint();
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
}
