package irt.data;

import irt.controller.serial_port.ComPort;
import irt.controller.serial_port.value.Getter.ValueChangeListenerClass;
import irt.data.event.ValueChangeEvent;
import irt.data.packet.Packet;
import irt.tools.panel.head.Console;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JProgressBar;

import jssc.SerialPortException;

public class MicrocontrollerSTM32 extends ValueChangeListenerClass implements Runnable{

	public static final int ADDR_PROGR	= 0x08000000;
	public static final int[] ADDR_FLASH	= new int[]{0x080C0000, 0x080E0000};

	public static final byte COMMAND_CONNECT 	= 0x7F;
	public static final byte ACK 		= 0x79;
	public static final byte NACK 		= 0x1F;

	private static final int COMMAND_NON = 0;
	public static final byte GET_VERSION_AND_READ_PROTECTION_STATUS	= 0x01;
	public static final byte COMMAND_GET_ID									= 0x02;
	public static final byte COMMAND_GO										= 0x21;
	public static final byte[] COMMAND_GET 							= new byte[]{0x00,(byte) 0xFF};
	public static final byte[] COMMAND_READ_MEMORY					= new byte[]{0x11,(byte) 0xEE};
	public static final byte[] COMMAND_WRITE_MEMORY					= new byte[]{0x31,(byte) 0xCE};
	public static final byte[] COMMAND_ERASE						= new byte[]{0x43,(byte) 0xBC};
	public static final byte[] COMMAND_EXTENDED_ERASE				= new byte[]{0x44,(byte) 0xBB};
	public static final byte[] COMMAND_WRITE_PROTECT				= new byte[]{0x63,(byte) 0x9C};
	public static final byte[] COMMAND_WRITE_UNPROTECT				= new byte[]{0x73,(byte) 0x8C};
	public static final byte COMMAND_READOUT_PROTECT						= (byte) 0x82;
	public static final byte COMMAND_READOUT_UNPROTECT						= (byte) 0x92;

	private static final int MAX_VAR_RAM_SIZE = 256;//K Bytes

	public static final int CONNECT	= 1;
	public static final int READ	= 2;
	public static final int WRITE	= 3;
	public static final int VERSION	= 4;//boot loader version
	public static final int MESSAGE	= 5;
	public static final int ERROR	= 6;
	public static final int GET_SUPORTED_COMMANDS	= 7;
	public static final int PREPARE_TO_WRITE		= 8;

	private byte[] supportedCommands;
	private int addr;
	private byte[] flash;
	private byte[] pagesToErase;//2bytes for page
	private int command;

	private ComPort serialPort;
	private boolean running = true;
	private Unit unit;
	private int addressIndex;
	private JProgressBar progressBar;

	public MicrocontrollerSTM32(ComPort serialPort) {
		this.serialPort = serialPort;
	}

	public boolean readFlash() throws SerialPortException, UnsupportedEncodingException {

		boolean isRead = false;

		synchronized (this) {

			int length = MAX_VAR_RAM_SIZE-1;
			int addr = ADDR_FLASH[addressIndex];
			flash = null;
			serialPort.clear();

			//4 loops equals 1K Bytes
			for(int i=0; i<((1024*128)/MAX_VAR_RAM_SIZE); i++){//max 128K bite
//				"read Command");
				if(sendCommand(COMMAND_READ_MEMORY)){
//					"Send Address");
					if(sendStartAddress(addr)){
//						"Send Length");
						if(sendLength((byte) length)){
							byte[] read = serialPort.readBytes(MAX_VAR_RAM_SIZE);
							if(read!=null){
								flash = addArray(flash, read);
								if(Arrays.equals(
										Arrays.copyOfRange(read, read.length-3, read.length),
										new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF})){
									fireValueChangeListener(new ValueChangeEvent("The FLASH was read", MESSAGE));
									isRead = true;
									break;
								}
							}else{
								fireValueChangeListener(new ValueChangeEvent("4.Could not Read from the Unit try to reset them", ERROR));
								break;
							}
						}else{
							fireValueChangeListener(new ValueChangeEvent("3.Could not Read from the Unit try to reset them", ERROR));
							break;
						}
					}else{
						fireValueChangeListener(new ValueChangeEvent("2.Could not Read from the Unit try to reset them", ERROR));
						break;
					}
				}else{
					fireValueChangeListener(new ValueChangeEvent("1.Could not Read from the Unit try to reset them", ERROR));
					break;
				}
				addr += MAX_VAR_RAM_SIZE;
			}
		}
		if(flash!=null)
			fireValueChangeListener(new ValueChangeEvent(new String(flash, "UTF-8"), READ));

		return isRead;
	}

	private byte[] addArray(byte[] flash, byte[] read) {

		if(flash==null)
			flash = read;
		else{
			int oldSize = flash.length;
			flash = Arrays.copyOf(flash, oldSize+read.length);
			System.arraycopy(read, 0, flash, oldSize, read.length);
		}

		return flash;
	}

	public void writeToFlash(Object obj){

		if(obj instanceof byte[])
			flash = (byte[]) obj;
		else
			unit = (Unit) obj;

		if(supportedCommands==null)
			setCommand(GET_SUPORTED_COMMANDS);
		else
			setCommand(PREPARE_TO_WRITE);
	}

	protected void prepareToWrite() {
		if(containsCommand(COMMAND_EXTENDED_ERASE[0]))
			if(unit!=null)
				prepareVariables();
			else
				prepareProgram();
		else
			fireValueChangeListener(new ValueChangeEvent("Not Suiported Command:"+Arrays.toString(COMMAND_EXTENDED_ERASE), ERROR));
	}

	protected void prepareProgram() {
		int[] pages = new int[]{16*1024,16*1024,16*1024,16*1024,64*1024,128*1024,128*1024,128*1024,128*1024,128*1024};
		if(flash!=null){
			addEnd();
			int length = flash.length;
			int pageCount = 0;

			for(int i:pages)
				if(length>0){
					pageCount++;
					length=length-i;
				}else
					break;

			pagesToErase = new byte[pageCount*2];

			for(int i=0; i<pageCount; i++){
				byte[] b = Packet.toBytes((short)i);
				int index = i*2;
				pagesToErase[index]		= b[0];
				pagesToErase[++index]	= b[1];
			}
			
			addr  = ADDR_PROGR;
		}else
			fireValueChangeListener(new ValueChangeEvent("Select the Program File", ERROR));
	}

	protected void prepareVariables() {
		if(prepareToWrite(unit))
			addr = ADDR_FLASH[addressIndex];
		else
			fireValueChangeListener(new ValueChangeEvent("Put the Data To Write", ERROR));
		unit = null;
	}

	private boolean writeToFlash() throws SerialPortException, UnsupportedEncodingException, InterruptedException {

		boolean isLoaded = false;
		int length = 256;
		int readFrom = 0;
		double onePercent = flash.length/100.0;
		DecimalFormat decimalFormat = new DecimalFormat("#.00");

		if (addr != ADDR_PROGR)
			if (flash.length < 128 * 1024)
				if (addr == ADDR_FLASH[0])
					pagesToErase = new byte[] { 0, 10 };
				else
					pagesToErase = new byte[] { 0, 11 };
			else
				pagesToErase = new byte[] { 0, 10, 0, 11 };

		if(eraseFlashPage())	
			while(readFrom < flash.length){

				if(sendCommand(COMMAND_WRITE_MEMORY)){
					if(sendStartAddress(addr)){
						int readTo = readFrom+length;
						int to = readTo<=flash.length ? readTo : flash.length;
						if(!sendData(Arrays.copyOfRange(flash, readFrom, to ))){
							fireValueChangeListener(new ValueChangeEvent("3. Could Not Write to the Flash Memory", ERROR));
							break;
						}
					}
				}else{
					fireValueChangeListener(new ValueChangeEvent("2. Could Not Write to the Flash Memory", ERROR));
					break;
				}
				addr += length;
				readFrom += length;
				double percent = readFrom/onePercent;
				progressBar.setValue((int) percent);
				fireValueChangeListener(new ValueChangeEvent(decimalFormat.format(percent)+" %", MESSAGE));
			}
		else
			fireValueChangeListener(new ValueChangeEvent("1. Could Not Write to the Flash Memory", ERROR));

		if(readFrom >= flash.length){
			fireValueChangeListener(new ValueChangeEvent("Upload Operation Finished Successfully", MESSAGE));
			isLoaded = true;
		}

		flash = null;

		return isLoaded;
	}

	private boolean containsCommand(byte command) {
		boolean contains = false;
		if(supportedCommands!=null)
			for(byte b:supportedCommands)
				if(b==command){
					contains = true;
					break;
				}
		return contains;
	}

	private boolean sendData(byte[] data) throws SerialPortException {

		Console.TEXT_AREA.append("sendData-"+new String(data)+"\n");

		byte[] toSend = new byte[data.length+1];
		System.arraycopy(data, 0, toSend, 1, data.length);
		toSend[0] = (byte) (data.length - 1);
		toSend = addCheckSum(toSend);

		serialPort.clear();
		serialPort.writeBytes(toSend);

		byte[] readBytes = serialPort.readBytes(1, 100);

		return readBytes!=null && readBytes[0]==ACK;
	}

	private boolean eraseFlashPage() throws SerialPortException{

//		Console.TEXT_AREA.append("Erase Flash-"+Arrays.toString(pagesToErase)+"\n");

		byte[] readBytes = null;

		if(sendCommand(COMMAND_EXTENDED_ERASE)){
			fireValueChangeListener(new ValueChangeEvent("Erasing The Flash Memory", MESSAGE));
			byte[] toSend = addLength(pagesToErase);
			toSend = addCheckSum(toSend);
			serialPort.clear();
//			"Erase Pages");
			serialPort.writeBytes(toSend);
//			"write - "+Arrays.toString(toSend));
			readBytes = serialPort.readBytes(1, 10000);
		}

		return readBytes!=null && readBytes[0]==ACK;
	}

	private byte[] addLength(byte[] pages) {
		byte[] toSend = new byte[pages.length+2];
		int pageCount = pages.length/2 - 1;
		toSend[0] = (byte) (pageCount>>8);
		toSend[1] = (byte) pageCount;
		System.arraycopy(pages, 0, toSend, 2, pages.length);
		return toSend;
	}

	public boolean writeUnprotect() throws SerialPortException, InterruptedException {

		serialPort.clear();
		serialPort.writeBytes(COMMAND_WRITE_UNPROTECT);
		boolean isAck = serialPort.readBytes(2)[1]==ACK;
		synchronized (this) {
			wait(500);
		}
		return isAck ? connect() : false;
	}

	private boolean sendCommand(byte[] command) throws SerialPortException {

		Console.appendLn(getCommandStr(command), "sendCommand");

		serialPort.clear();
		serialPort.writeBytes(command);
//		"write - "+Arrays.toString(command));
		byte[] readBytes = serialPort.readBytes(1,100);

		return readBytes!=null && readBytes[0]==ACK;
	}

	public String getCommandStr(byte[] command) {
		String str = null;

		switch(command[0]){
		case 0:
			str = "COMMAND_GET(0)";
			break;
		case 0x11:
			str = "COMMAND_READ_MEMORY(0x11)";
			break;
		case 0x31:
			str = "COMMAND_WRITE_MEMORY(0x31)";
			break;
		case 0x43:
			str = "COMMAND_ERASE(0x43)";
			break;
		case 0x44:
			str = "COMMAND_EXTENDED_ERASE(0x44)";
			break;
		case 0x63:
			str = "COMMAND_WRITE_PROTECT(0x63)";
			break;
		case 0x73:
			str = "COMMAND_WRITE_UNPROTECT(0x73)";
		}
		return str;
	}

	private boolean sendLength(byte length) throws SerialPortException {


		serialPort.clear();
		Console.appendLn(""+(length&0xff),"sendLength");
		serialPort.writeBytes(new byte[]{length, (byte) (length^0xFF)});

		byte[] readBytes = serialPort.readBytes(1, 100);

		return readBytes!=null && readBytes[0]==ACK;
	}

	private boolean sendStartAddress(int startAddress) throws SerialPortException {

		byte[] addCheckSum = addCheckSum(getBytes(startAddress));

		serialPort.clear();
		Console.appendLn("0x"+Integer.toHexString(startAddress).toUpperCase(),"sendStartAddress");
		serialPort.writeBytes(addCheckSum);
		byte[] readBytes = serialPort.readBytes(1, 100);

		return readBytes!=null && readBytes[0]==ACK;
	}

	private byte[] addCheckSum(byte[] original) {
		byte[] result = Arrays.copyOf(original, original.length+1);
		result[original.length] = getCheckSum(original);
		return result;
	}

	private byte getCheckSum(byte[] original) {
		byte xor = 0;

		for(byte b:original)
			xor ^= b;

		return xor;
	}

	private byte[] getBytes(int flashAddr) {
		return new byte[]{
				(byte) (flashAddr>>24),
				(byte) (flashAddr>>16),
				(byte) (flashAddr>>8),
				(byte) flashAddr
		};
	}

	public boolean connect() throws SerialPortException, InterruptedException {

		fireValueChangeListener(new ValueChangeEvent("Connecting", MESSAGE));
		boolean isAck = false;
		synchronized (this) {
			if(!serialPort.isOpened())
				serialPort.openPort();

			serialPort.clear();//clear buffer

//			Console.appendLn(""+COMMAND_CONNECT, "Connect:");

			serialPort.writeByte(COMMAND_CONNECT);
			byte[] readBytes = serialPort.readBytes(1, 100);
			if(readBytes!=null){
				isAck = readBytes[0]==ACK;
			}
			supportedCommands = null;
		}

		if(isAck){
//			Console.TEXT_AREA.append(" (ACK)\n");
			fireValueChangeListener(new ValueChangeEvent("Connected", MESSAGE));
		}else{
//			Console.TEXT_AREA.append(" (NACK)\n");
			fireValueChangeListener(new ValueChangeEvent("Could not connect to the Unit try to reset them", ERROR));
		}

		return isAck;
	}

	private boolean getSuportedCommands() throws SerialPortException {

		boolean isAck = false;
		synchronized (this) {
			serialPort.clear();//clear buffer
//			Console.appendLn("Get suported Commands", "Command");
			serialPort.writeBytes(COMMAND_GET);
			byte[] b = serialPort.readBytes(1, 5000);

			if(b!=null && b[0]==ACK)
				b = serialPort.readBytes(1, 5000);
			else
				b = null;

			if(b!=null)
				b = serialPort.readBytes(++b[0], 100);

			if(b!=null){
				fireValueChangeListener(new ValueChangeEvent(new Byte(b[0]), VERSION));
				supportedCommands = Arrays.copyOfRange(b, 1, b.length-1);
				b = serialPort.readBytes(1, 500);
				if(b!=null && b[0]==ACK)
					isAck = true;
			}

		}
		return isAck;
	}

	@Override
	protected void finalize() throws Throwable {
		if(serialPort!=null && serialPort.isOpened())
			serialPort.closePort();
	}

	public boolean prepareToWrite(Unit unit) {
		String str = "# IRT Technologies board environment config\r\n" +
					"# First two lines must start from this text - do not modify\r\n" +
					"# MD5 checksum will be placed here\r\n";

		Map<String, String> variables = unit.getVariables();

		if(!variables.isEmpty()){
			for(String s:variables.keySet()){
				String string = variables.get(s);
				str += s+" "+string+"\r\n";
			}

			List<String[]> tables = unit.getTables();
			for(String[] s:tables){
				str += s[0]+" "+s[1]+"\r\n";
			}

			flash = (str+'\0').getBytes();
			addEnd();
		}else
			flash = null;

		return flash!=null && flash.length!=0;
	}

	public void addEnd() {
		int length = flash.length;
		int end = length%4;
		if(end>0){
			flash = Arrays.copyOf(flash, length+4-end);
			Arrays.fill(flash, length, flash.length, (byte)0xff);
		}
	}

	public void setFlash(byte[] flash) {
		this.flash = flash;
		addEnd();

		int[] flashSize = new int[]{16,32,48,64,128,256,384,512,640,768,996,1024};//Kilobytes
		int i;
		int y;
		for(i=0; i<flashSize.length;)
			if(flash.length<flashSize[i++]*1024)
				break;
		pagesToErase = new byte[i*2];
		for(i=0, y=1; i<pagesToErase.length && y<pagesToErase.length; y+=2, i++)
			pagesToErase[y] = (byte) i;
	}

	public ComPort getComPort() {
		return serialPort;
	}

	public void setComPort(ComPort comPort) {
		serialPort = comPort;
	}

	@Override
	public void run() {

		while(running){
			long start = System.currentTimeMillis();
			try {
				switch(command){
				case CONNECT:
					Console.appendLn("< CONNECT");
					connect();
					command = COMMAND_NON;
					Console.append((System.currentTimeMillis()-start)+" msec >", ";\nConnection time");
					break;
				case READ:
					Console.appendLn("< READ");
					readFlash();
					command = COMMAND_NON;
					Console.append((System.currentTimeMillis()-start)+" msec >", ";\nRead time");
					break;
				case GET_SUPORTED_COMMANDS:
					Console.appendLn("< GET_SUPORTED_COMMANDS");
					getSuportedCommands();
					command = PREPARE_TO_WRITE;
					Console.append((System.currentTimeMillis()-start)+" msec >", ";\nGET_SUPORTED_COMMANDS time");
					break;
				case WRITE:
					Console.appendLn("< WRITE");
					writeToFlash();
					command = COMMAND_NON;
					Console.appendLn((System.currentTimeMillis()-start)+" msec >", ";\nWrite time");
					break;
				case PREPARE_TO_WRITE:
					Console.appendLn("< PREPARE_TO_WRITE");
					prepareToWrite();
					command = WRITE;
					Console.appendLn((System.currentTimeMillis()-start)+" msec >", ";\nPREPARE_TO_WRITE Time");
				}
			} catch (UnsupportedEncodingException | SerialPortException | InterruptedException e) { e.printStackTrace(); }

			if(command==COMMAND_NON){
				synchronized (this) { try {
					wait();
				} catch (InterruptedException e) { e.printStackTrace(); }}
			}
		}
	}

	public void setCommand(int command) {
		this.command = command;
		Console.appendLn(""+command, "setCommand");
		synchronized (this) {
			notify();
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
		synchronized (this) {
			notify();
		}
	}

	public int getAddressIndex() {
		return addressIndex;
	}

	public void setAddressIndex(int addressIndex) {
		this.addressIndex = addressIndex;
	}

	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}
}
