package irt.controller;

import irt.controller.serial_port.ComPortThreadQueue;
import irt.tools.panel.head.Console;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GuiControllerAbstract extends Thread {

	protected static final String SERIAL_PORT = "serialPort";

	public static final int ALL 		= 0;
	public static final int CONVERTER 	= 1;
	public static final int LINKED 	= 2;

	protected static Preferences prefs = Preferences.userRoot().node("SHOW");

	protected static ComPortThreadQueue comPortThreadQueue = new ComPortThreadQueue();

	private Console console;

	public GuiControllerAbstract(String threadName, JFrame gui) {
		super(threadName);
		console = new Console(gui, "Console");
		JPanel contentPane = (JPanel) gui.getContentPane();
		Component[] components = contentPane.getComponents();
		for(Component c:components)
			if(c.getClass().getSimpleName().equals("IrtPanel"))
				c.addMouseListener(new MouseListener() {
					
					@Override
					public void mouseReleased(MouseEvent e) {
						int modifiers = e.getModifiers();
						if((modifiers&InputEvent.CTRL_MASK)>0)
							console.setVisible(!console.isVisible());
					}
					@Override public void mousePressed(MouseEvent e) {}
					@Override public void mouseExited(MouseEvent e) {}
					@Override public void mouseEntered(MouseEvent e) {}
					@Override public void mouseClicked(MouseEvent e) {}
				});
	}

	public static ComPortThreadQueue getComPortThreadQueue() {
		return comPortThreadQueue;
	}

	public static Preferences getPrefs() {
		return prefs;
	}
}
