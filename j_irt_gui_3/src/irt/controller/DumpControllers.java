package irt.controller;

import irt.controller.serial_port.value.Getter.Getter;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.dump.DumpToFile;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.tools.panel.head.UnitsContainer;
import irt.tools.panel.subpanel.InfoPanel;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

public class DumpControllers {

	private static final int MAX_FILE_QUANTITY = 10;


	private static final String DUMP = "dump";


	private List<DumpController> dumpsList = new ArrayList<>();


	private volatile static Map<Integer, String> variables = new HashMap<>();

	private ValueChangeListener valueChangeListener = new ValueChangeListener() {

		@Override
		public void valueChanged(ValueChangeEvent valueChangeEvent) {

			int id = valueChangeEvent.getID();
//			System.out.println(id+") valueChangeEvent: "+valueChangeEvent);
			String source = valueChangeEvent.getSource().toString();

			String value = variables.get(id);
			if(value==null || !value.equals(source)){
				variables.put(id, source);
				deleteExtraFiles(MAX_FILE_QUANTITY, file.getParentFile(), DUMP);
				renameFile();
				new DumpToFile(parent, file, parseId(id), source);
			}
		}

		private String parseId(int id) {
			String str = ""+id;

			if(id==99)
				str = "1.10";
			else if(id==100)
				str = "2.100";
			else if(str.charAt(0)=='9')
				str = str.replace("9", "1.");
			else if(str.charAt(str.length()-1)>'0')
				str = str.replace("10", "2.");

			return str;
		}
	};

	private File file;

	private long startTime;

	private String fileName;
	private String fileExt = ".log";


	private UnitsContainer parent;

	public DumpControllers(UnitsContainer unitsPanel, LinkHeader linkHeader, DeviceInfo deviceInfo, int waitTime) {

		this.parent = unitsPanel;

		createNewFile(deviceInfo.getSerialNumber().toString());
		new DumpToFile(unitsPanel, file, "Start", deviceInfo.toString());

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_0, 0) { @Override public Integer getPriority() { return 14; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_1, 1) { @Override public Integer getPriority() { return 13; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_2, 2) { @Override public Integer getPriority() { return 12; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_3,3) { @Override public Integer getPriority() { return 11; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_4, 4) { @Override public Integer getPriority() { return 10; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_10, 10) { @Override public Integer getPriority() { return 9; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_1, 1) { @Override public Integer getPriority() { return 7; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_2, 2) { @Override public Integer getPriority() { return 6; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_3, 3) { @Override public Integer getPriority() { return 5; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_4, 4) { @Override public Integer getPriority() { return 4; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_5, 5) { @Override public Integer getPriority() { return 3; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_6, 6) { @Override public Integer getPriority() { return 2; }
		}, waitTime);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_100, 100) { @Override public Integer getPriority() { return 1; }
		}, waitTime);
	}

	private void addDumpController(Getter getter, int waitTime){

		DumpController dumpController = new DumpController(getter)
		{ @Override protected ValueChangeListener addGetterValueChangeListener() { return valueChangeListener; }};

		dumpController.setWaitTime(waitTime);

		Thread t = new Thread(dumpController);
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.start();

		dumpsList.add(dumpController);
	}

	private void createNewFile(String serialNumber) {

		serialNumber = serialNumber.replaceAll("[:\\\\/*?|<>]", "_");

//		System.out.println("file : "+serialNumber);
		startTime = System.currentTimeMillis();

		File file = new File("c:"+File.separator+"irt"+File.separator+serialNumber);

		if(!file.isDirectory())
			file.mkdirs();

		deleteExtraFiles(MAX_FILE_QUANTITY, file, DUMP);

		fileName = DUMP+serialNumber+'-'+getDate("yyyyMMddHHmmss");

		this.file = new File(file,fileName + fileExt);

//		System.out.println("file : "+file);
		try {
			this.file.createNewFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(parent, "DumpControllers:createNewFile:"+e.getLocalizedMessage());
		}
	}

	protected void checkFileSize() {
		if(file.length()>1000000000){

			File[] files = getFilesStartWith(file.getParentFile(), fileName);
			fileExt = files[files.length-1].getName();
			fileExt = fileExt.substring(fileExt.lastIndexOf("."));

			char charAt = fileExt.charAt(fileExt.length()-1);
			String ext = fileExt.substring(0, fileExt.length()-1);

			if(Character.isDigit(charAt))
				fileExt = ext+(Integer.toString(charAt)+1);
			else
				fileExt = ext+1;
		}
	}

	/**
	 * 
	 * @param file
	 * @param startWhith
	 * @return ordered by date array of files (oldest first)
	 */
	protected File[] getFilesStartWith(File file, final String startWhith) {
		FilenameFilter ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(startWhith);
				}
			};
		File[] listFiles = file.listFiles(ff);

		if(listFiles!=null)
			Arrays.sort(listFiles, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.lastModified()<o2.lastModified() ? -1 : o1.lastModified()>o2.lastModified() ? 1 : 0;
				}
			});

		return listFiles;
	}

	private void renameFile(){

		checkFileSize();

		File file = new File(this.file.getParentFile(), fileName+"-"+InfoPanel.calculateTime((System.currentTimeMillis()-startTime)/1000).replace(':', '_')+fileExt);

		if(this.file.renameTo(file))
			this.file = file;
	}

	private void deleteExtraFiles(int maxFilesQuantity, File file, final String startWhith) {
	
		File[] files = getFilesStartWith(file, startWhith);
	
		if(files!=null){
			int countToDelete = files.length - maxFilesQuantity;
	
			if(countToDelete>0)
				for(int i=0; i<countToDelete; i++)
					files[i].delete();
		}
	}

	public static String getDate(String pattern) {
		DateFormat dateFormat = new SimpleDateFormat(pattern);
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}

	public void stop() {
		for(DumpController dc:dumpsList)
			dc.setRun(false);
	}

	@Override
	protected void finalize() throws Throwable {
		stop();
	}

	public void setWaitTime(int waitTime) {

		for (DumpController dc:dumpsList)
			dc.setWaitTime(waitTime);
	}
}
