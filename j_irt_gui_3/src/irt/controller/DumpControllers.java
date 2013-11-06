package irt.controller;

import irt.controller.serial_port.value.getter.Getter;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.tools.panel.head.UnitsContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class DumpControllers {

//	private static final int MAX_FILE_QUANTITY = 50;
//	private static final int MAX_FILE_SIZE = 5000;
//	private static final String DUMP = "dump";

	public static final String DUMP_WAIT = "DUMP_WAIT";

	private static LoggerContext ctx = setSysSerialNumber(null);
	private final Logger logger = (Logger) LogManager.getLogger();
	private final Logger dumper = (Logger) LogManager.getLogger("dumper");
	private final Marker marker = MarkerManager.getMarker("FileWork");

	private List<DumpController> dumpsList = new ArrayList<>();

	private volatile static Map<Integer, String> variables = new HashMap<>();

	private ValueChangeListener valueChangeListener = new ValueChangeListener() {

		@Override
		public void valueChanged(ValueChangeEvent valueChangeEvent) {

			logger.debug("valueChanged({})", valueChangeEvent);
			int id = valueChangeEvent.getID();
			String source = valueChangeEvent.getSource().toString();

			String value = variables.get(id);
			if(value==null || !value.equals(source)){
				variables.put(id, source);
//				deleteExtraFiles(DUMP);
//				checkFileSize();
//				renameFile();
//				new DumpToFile(parent, file, parseId(id), source);
				dumper.info(marker, parseId(id)+":"+info+"\n"+source);
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

	private String info;

//	private File file;
//
//	private long startTime;
//
//	private String fileName;
//	private String fileExt = ".log";
//
//	private UnitsContainer parent;
//	private File dir;
//	private String deviceInfoStr;

	public DumpControllers(UnitsContainer unitsPanel, LinkHeader linkHeader, DeviceInfo deviceInfo) {

		String serialNumber = deviceInfo.getSerialNumber().toString();

		setSysSerialNumber(serialNumber);
		info = "\nSN: "+deviceInfo.getSerialNumber();
		info += "\n"+deviceInfo.getUnitName();
		info += "\nVersion: "+deviceInfo.getFirmwareVersion();
		info += "\nBuilt Date: "+deviceInfo.getFirmwareBuildDate();
		info += "\nType: "+deviceInfo.getType();
		info += "\nSubtype: "+deviceInfo.getSubtype();
		info += "\nType: "+deviceInfo.getRevision();
		info += "\ncount: "+deviceInfo.getFirmwareBuildCounter();
		logger.debug("deviceInfo: "+info);

		int dumpWaitMinuts = GuiController.getPrefs().getInt(DUMP_WAIT, 10);
		int waitTime = 1000*60*dumpWaitMinuts;

		logger.debug("new DumpControllers({}, {}, {}, waitTime={} msec({} min))", unitsPanel, linkHeader, deviceInfo, waitTime, dumpWaitMinuts);

//		this.parent = unitsPanel;

//		createNewFile(serialNumber);
//		deviceInfoStr = deviceInfo.toString();
//		new DumpToFile(unitsPanel, file, "Start", deviceInfoStr);

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

	public static LoggerContext setSysSerialNumber(String serialNumber) {

		if(serialNumber==null)
			serialNumber ="UnknownSerialNumber";

		String sysSerialNumber = System.getProperty("serialNumber");

		if(sysSerialNumber==null || !sysSerialNumber.equals(serialNumber)){
			System.setProperty("serialNumber", serialNumber.replaceAll("[:\\\\/*?|<>]", "x"));

			ctx = (LoggerContext) LogManager.getContext(false);
			ctx.reconfigure();
		}

		return ctx;
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
//
//	private void createNewFile(String serialNumber) {
//		logger.trace("createNewFile(serialNumber={})", serialNumber);
//
//		serialNumber = serialNumber.replaceAll("[:\\\\/*?|<>]", "_");
//
//		logger.debug("createNewFile({}); validated", serialNumber);
//
//		startTime = System.currentTimeMillis();
//
//		dir = new File("c:"+File.separator+"irt"+File.separator+serialNumber);
//
//		if(!dir.isDirectory())
//			dir.mkdirs();
//
//		deleteExtraFiles(DUMP);
//
//		fileName = DUMP+serialNumber+'-'+getDate("yyyyMMddHHmmss");
//
//		createNewFile();
//	}
//
//	private void createNewFile() {
//		this.file = new File(dir, fileName + fileExt);
//
//		logger.trace("createNewFile: var this.file={}", this.file);
//
//		try {
//			this.file.createNewFile();
//		} catch (IOException e) {
//			logger.error(e);
//			JOptionPane.showMessageDialog(parent, "DumpControllers:createNewFile:"+e.getLocalizedMessage());
//		}
//	}
//
//	protected void checkFileSize() {
//		logger.trace("checkFileSize()");
//		if(file.length()>MAX_FILE_SIZE){
//
//			File[] files = getFilesStartWith(file.getParentFile(), fileName);
//			logger.debug(Arrays.toString(files));
//			fileExt = files[files.length-1].getName();
//			fileExt = fileExt.substring(fileExt.lastIndexOf("."));
//
//			char charAt = fileExt.charAt(fileExt.length()-1);
//			String ext = fileExt.substring(0, fileExt.length()-1);
//
//			if(Character.isDigit(charAt)){
//				ext = fileExt.replaceAll("\\d", "");
//				String digits = fileExt.replaceAll("\\D", "");
//				fileExt = ext+(Integer.parseInt(digits)+1);
//				logger.debug("checkFileSize(); fileName{} fileExt={}, ext={}, digits={}", fileName, fileExt, ext, digits);
//			}else{
//				ext = fileExt.substring(0, fileExt.length()-1);
//				fileExt = ext+1;
//			}
//			createNewFile();
//			new DumpToFile(parent, file, "Start", deviceInfoStr);
//		}
//	}
//
//	/**
//	 * 
//	 * @param file
//	 * @param startWhith
//	 * @return ordered by date array of files (oldest first)
//	 */
//	protected File[] getFilesStartWith(File file, final String startWhith) {
//		logger.trace("getFilesStartWith({}, startWhith={})", file, startWhith);
//		FilenameFilter ff = new FilenameFilter() {
//			@Override
//			public boolean accept(File dir, String name) {
//				return name.startsWith(startWhith);
//				}
//			};
//		File[] listFiles = file.listFiles(ff);
//
//		if(listFiles!=null)
//			Arrays.sort(listFiles, new Comparator<File>() {
//				@Override
//				public int compare(File o1, File o2) {
//					return o1.lastModified()<o2.lastModified() ? -1 : o1.lastModified()>o2.lastModified() ? 1 : 0;
//				}
//			});
//
//		return listFiles;
//	}
//
//	private void renameFile(){
//		logger.trace("renameFile(); {}", file);
//		logger.debug("renameFile(); fileName={}, fileExt={}", fileName, fileExt);
//
//		File file = new File(
//				this.file.getParentFile(),
//				fileName+"-"+InfoPanel.calculateTime(
//						(System.currentTimeMillis()-startTime)/1000).replace(':', '_')+fileExt);
//
//		logger.trace("renameFile(); {}", file);
//		if(this.file.renameTo(file))
//			this.file = file;
//	}
//
//	private void deleteExtraFiles(final String startWhith) {
//		logger.trace("deleteExtraFiles(maxFilesQuantity={}, {}, startWhith={})", MAX_FILE_QUANTITY, dir, startWhith);
//	
//		File[] files = getFilesStartWith(dir, startWhith);
//	
//		if(files!=null){
//			int countToDelete = files.length - MAX_FILE_QUANTITY;
//	
//			if(countToDelete>0)
//				for(int i=0; i<countToDelete; i++)
//					files[i].delete();
//		}
//	}
//
//
//	public static String getDate(String pattern) {
//		DateFormat dateFormat = new SimpleDateFormat(pattern);
//		Calendar cal = Calendar.getInstance();
//		return dateFormat.format(cal.getTime());
//	}

	public void stop() {
		logger.trace("stop()");
		for(DumpController dc:dumpsList)
			dc.setRun(false);
	}

	@Override
	protected void finalize() throws Throwable {
		dumper.info("Communication Lost");
		stop();
	}

	public void setWaitTime(int waitTime) {
		logger.trace("setWaitTime(waitTime={})", waitTime);

		for (DumpController dc:dumpsList)
			dc.setWaitTime(waitTime);
	}
}
