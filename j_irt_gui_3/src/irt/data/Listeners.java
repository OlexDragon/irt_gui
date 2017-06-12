package irt.data;

import java.awt.EventQueue;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Listeners {
	private static final Logger logger = LogManager.getLogger();

	public static final PopupMenuListener popupMenuListener = new PopupMenuListener() {


		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
        	@SuppressWarnings("unchecked")

        	final JComboBox<String> comboBox = (JComboBox<String>)popupMenuEvent.getSource();

			EventQueue.invokeLater(new Runnable() {

				@Override 
	            public void run() {
					try{
					JComboBox<String> c = comboBox;
	            	Object o = c.getAccessibleContext().getAccessibleChild(0);
	            	if(o instanceof JComponent) { //BasicComboPopup
	            		((JComponent)o).repaint();
	            	}
					}catch (Exception e) {
						logger.catching(e);
					}
	            }
	        });
		}
		@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
		@Override public void popupMenuCanceled(PopupMenuEvent arg0) {}
	};

}
