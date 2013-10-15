package irt.data;

import irt.tools.panel.head.Console;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Unit {

	public static final String DEVICE_TYPE = "device-type";
	public static final String DEVICE_SERIAL_NUMBER = "device-serial-number";
	private int id;
//	private String partNumberStr;
	private String link;
	private String deviceType;
	
	private byte[] program;
	private Map<String, String> variables;
	private List<String[]> tables;
	private byte bootloaderVersion;

//	public Unit(int id, String partNumberStr, String link, String deviceType) {
//		this.id = id;
//		this.partNumberStr = partNumberStr;
//		this.link = link;
//		this.deviceType =deviceType;
//	}

	public Unit() {
	}

	public int getId() {
		return id;
	}

	public String getLink() {
		return link;
	}

	public void setFlashStr(String flashStr) throws UnsupportedEncodingException {
//System.out.println(flashStr);
		variables = new TreeMap<>();
		tables = new ArrayList<>();

		Console.appendLn(flashStr, "* Flash:\n");
		if(flashStr!=null){

			flashStr.replaceAll("\r", "");
			String[] split = flashStr.split("\n");
			for(String s:split){
				int index = s.indexOf('#');

				if(index==0)
					continue;

				if(index>0)
					s = s.substring(0,index);

				s = s.trim();

				if(s.isEmpty() || s.charAt(0)<'A' || s.charAt(0)>'z')
					continue;
				else{
					String[] keyValue = s.split(" ", 2);
					if(keyValue.length!=0 && !(keyValue[0] = keyValue[0].trim()).isEmpty())
						if(keyValue[0].contains("-lut-")){
							tables.add(new String[]{keyValue[0],keyValue[1].trim().replaceAll("\\s+", " ")});//replaceAll("\\s+", " ") - remove extra spaces
						}else{
							if(keyValue.length>1){
								split = keyValue[1].split(" ", 2);

								if(split.length>1){
									keyValue[0] = keyValue[0]+" "+split[0];
									keyValue[1]	= split[1];
								}

								variables.put(keyValue[0], keyValue[1].trim());
							}else
								variables.put(keyValue[0], "");
						}
				}
			}
			Collections.sort(tables, new Comparator<String[]>() {
				public int compare(String[] a,String[] b){
					long compareTo = b[0].compareTo(a[0]);
					if(compareTo == 0){
						String splitStrA = a[1].split("\\s+")[0].replaceAll("[\\D.-]", "");
						String splitStrB = b[1].split("\\s+")[0].replaceAll("[\\D.-]", "");
						System.out.println("splitStrA:"+splitStrA+", splitStrB:"+splitStrB);
						if(!(splitStrA.isEmpty() || splitStrB.isEmpty()))
							compareTo = (long) (Double.parseDouble(splitStrA)-Double.parseDouble(splitStrB));
						else if(!splitStrA.isEmpty())
							compareTo = 1;
						else if(!splitStrB.isEmpty())
							compareTo = -1;
						else
							compareTo = 0;	
					}
					return (int) compareTo;
				}
			});
		}else
			variables = null;
	}

	public byte[] getProgram() {
		return program;
	}

	public synchronized void setProgram(byte[] hexFile) {
		this.program = hexFile;
	}

	public String getSerialNumber() {
		return variables.get(DEVICE_SERIAL_NUMBER);
	}

	public boolean isSet() {
		return variables!=null;
	}

	public void setSerialNumber(String serialNumber) {
		if(variables==null)
			variables = new TreeMap<>();
		variables.put(DEVICE_SERIAL_NUMBER, serialNumber);
	}

	public boolean isDeviceTypeMatches() {
		boolean isMatches = false;
		String deviceType = variables.get(DEVICE_TYPE);

		if(deviceType==null){
			variables.put(DEVICE_TYPE, this.deviceType);
			isMatches = true;
		}else
			isMatches = deviceType.equals(this.deviceType);

		return isMatches;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void resetDeviceType() {
		deviceType = variables.get(DEVICE_TYPE);
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public List<String[]> getTables() {
		return tables;
	}

	public void clear() {
		variables = null;
		tables = null;
	}

	@Override
	public String toString() {
		String str = "";

		if(variables!=null){
			Set<String> keys = variables.keySet();
			for(String s:keys)
				str += s+" "+variables.get(s)+"\n";
		}

		if(tables!=null){
			for(String[] ss:tables){
				for(String s:ss)
					str += s+" ";
				str += "\n";
			}
		}

		return str;
	}

	public void setBootloaderVersion(byte bootloaderVersion) {
		this.bootloaderVersion = bootloaderVersion;
	}

	public byte getBootloaderVersion() {
		return bootloaderVersion;
	}
}
