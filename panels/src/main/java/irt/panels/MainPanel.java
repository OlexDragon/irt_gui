package irt.panels;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class MainPanel extends IrtStylePanel {

	private final int PANEL_WIDTH;

    private JFrame target = null;
    private Point draggingAnchor = null;
	private Timer timer;

	private boolean increase = true;

	public MainPanel(final JFrame target, int width) {
		PANEL_WIDTH = width!=0 ? width : 650;
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
}
