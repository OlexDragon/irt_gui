package irt.tools;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class MakerResizable {

    private boolean drag = false;
    private Point dragLocation  = new Point();

    private MouseAdapter mouseAdapter;
	private MouseMotionAdapter mouseMotionAdapter;
	private AncestorListener ancestorListener;

	private JComponent component;
	private int sensitiveArea;

    public MakerResizable(JComponent component, int sensitiveArea){
 
    	this.sensitiveArea = sensitiveArea;
 
    	setListeners();
 
    	this.component = component;
		component.addMouseListener(mouseAdapter);
		component.addMouseMotionListener(mouseMotionAdapter);
		component.addAncestorListener(ancestorListener);
    }

    private void setListeners() {
    	mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            	drag = true;
            	dragLocation = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            	drag = false;
            }
    	};

		mouseMotionAdapter = new MouseMotionAdapter() {
            private boolean cursorRight;
			private boolean cursorBottom;

			@Override
            public void mouseDragged(MouseEvent e) {

				if(drag && (cursorRight || cursorBottom)){
					if (cursorRight && cursorBottom) 
						MakerResizable.this.component.setSize(getWidth(e), getHeight(e));
					else if(cursorRight)
						MakerResizable.this.component.setSize(getWidth(e), MakerResizable.this.component.getHeight());
					else
						MakerResizable.this.component.setSize(MakerResizable.this.component.getWidth(), getHeight(e));

					dragLocation = e.getPoint();
				}
			}

			protected int getHeight(MouseEvent e) {
				int height = (int) (MakerResizable.this.component.getHeight() + (e.getPoint().getY() - dragLocation.getY()));
				Dimension minimumSize = MakerResizable.this.component.getMinimumSize();
				int parentHeight = MakerResizable.this.component.getParent().getSize().height-MakerResizable.this.component.getY()-5;

				if(height<minimumSize.height)
					height = minimumSize.height;
				else if(parentHeight<height)
					height = parentHeight;

				return height;
			}

			protected int getWidth(MouseEvent e) {
				int width = (int) (MakerResizable.this.component.getWidth() + (e.getPoint().getX() - dragLocation.getX()));
				Dimension minimumSize = MakerResizable.this.component.getMinimumSize();
				int parentWidth = MakerResizable.this.component.getParent().getSize().width-MakerResizable.this.component.getX()-5;

				if(width<minimumSize.width)
					width = minimumSize.width;
				else if(parentWidth<width)
					width = parentWidth;

				return width;
			}

			@Override
			public void mouseMoved(MouseEvent mouseEvent) {

				cursorRight = mouseEvent.getX() > MakerResizable.this.component.getWidth() - sensitiveArea;
				cursorBottom = mouseEvent.getY() > MakerResizable.this.component.getHeight() - sensitiveArea;

				if (cursorRight && cursorBottom)
					MakerResizable.this.component.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
				else if(cursorRight)
					MakerResizable.this.component.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				else if(cursorBottom)
					MakerResizable.this.component.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				else
					MakerResizable.this.component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				MakerResizable.this.component.revalidate();
			}
		};
 
		ancestorListener = new AncestorListener() {
			@Override public void ancestorMoved(AncestorEvent arg0) { }
			@Override public void ancestorAdded(AncestorEvent arg0) { }
			@Override
			public void ancestorRemoved(AncestorEvent arg0) {
				clear();
			}	
		};
   }

	public void clear(){
    	component.removeMouseListener(mouseAdapter);
    	mouseAdapter = null;
    	component.removeMouseMotionListener(mouseMotionAdapter);
    	mouseMotionAdapter = null;
    	component.removeAncestorListener(ancestorListener);
    	ancestorListener = null;
    	component = null;
    	dragLocation = null;
    }

	public Component getComponent() {
		return component;
	}
}
