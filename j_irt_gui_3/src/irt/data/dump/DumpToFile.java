package irt.data.dump;

import irt.controller.DumpControllers;
import irt.tools.panel.head.UnitsContainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

public class DumpToFile extends Thread {

	private static final Object lock = new Object();

	private File file;
	private String id;
	private String variable;

	private UnitsContainer parent;

	public DumpToFile(UnitsContainer parent, File file, String id, String variable) {
		this.file = file;
		this.id = id;
		this.variable = variable;
		this.parent = parent;

		int priority = getPriority();
		if(priority>Thread.MIN_PRIORITY)
			setPriority(priority-1);
		start();
	}

	@Override
	public void run() {
		synchronized (lock) {
			try {
				appendToFile();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(parent, "DumpToFile:run:"+e.getLocalizedMessage());
			}
		}
	}

	private void appendToFile() throws IOException {
	
		FileWriter fileWritter = new FileWriter(file,true);
	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	
	    bufferWritter.write(id+": "+DumpControllers.getDate("yyyy.MM.dd HH:mm:ss"));
	    bufferWritter.newLine();
	
	    bufferWritter.write(variable);

	    bufferWritter.newLine();
	    bufferWritter.newLine();
	    bufferWritter.close();
	}
}
