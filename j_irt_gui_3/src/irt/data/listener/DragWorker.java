package irt.data.listener;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DragWorker extends MouseAdapter {

	private Component target;
	private Component component;
    private Point draggingAnchor = new Point();

	public DragWorker(Component component, Component target) {

		this.target = target;
		this.component = component;
		component.addMouseListener(this);
		component.addMouseMotionListener(this);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point locationOnScreen = e.getLocationOnScreen();

		target.setLocation(locationOnScreen.x - draggingAnchor.x, locationOnScreen.y - draggingAnchor.y);
	}

	@Override
	public void mouseMoved(MouseEvent e) {

		if(target==component){
			draggingAnchor.x = e.getX();
			draggingAnchor.y = e.getY();
		}else{
			draggingAnchor.x = e.getX() + component.getX();
			draggingAnchor.y = e.getY() + component.getY();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		component.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}
