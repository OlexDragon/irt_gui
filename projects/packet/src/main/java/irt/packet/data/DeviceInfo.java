package irt.packet.data;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.StringData;
import irt.packet.LinkHeader;
import irt.packet.Packet;
import irt.packet.ParameterHeader;
import irt.packet.Payload;
import irt.packet.observable.InfoPacket;

public class DeviceInfo {

	protected final Logger logger = LogManager.getLogger();

	public static final int REVISION_FIRST_BYTE = 4;
	public static final int SUBTYPE_FIRST_BYTE 	= 8;
	public static final int SIZE 				= 12;

	public enum DeviceType{
		DEVICE_TYPE_BIAS_BOARD			(2		,"Bias Board"),
		DEVICE_TYPE_PICOBUC_L_TO_KU		(100 	,"L to Ku"),
		DEVICE_TYPE_PICOBUC_L_TO_C		(101 	,"L to C"),
		DEVICE_TYPE_SSPA				(102 	,"SSPA"),
		DEVICE_TYPE_FUTURE_BIAS_BOARD	(199 	,"Future Bias Board"),
		DEVICE_TYPE_HPB_L_TO_KU			(200 	,"L to Ku"),
		DEVICE_TYPE_HPB_L_TO_C			(201 	,"L to C"),
		DEVICE_TYPE_HPB_SSPA			(202 	,"HPB SSPA"),

		DEVICE_TYPE_DLRS				(410 	,"DLRS"),

		DEVICE_TYPE_L_TO_KU_OUTDOOR 	(500 	,"L to Ku Outdoor"),
		DEVICE_TYPE_70_TO_L				(1001 	,"70 to L Converter"),
		DEVICE_TYPE_L_TO_70				(1002 	,"L to 70 Converter"),
		DEVICE_TYPE_140_TO_L			(1003 	,"140 to L Converter"),
		DEVICE_TYPE_L_TO_140			(1004 	,"L to 140 Converter"),
		DEVICE_TYPE_L_TO_KU				(1005 	,"L to Ku Converter"),
		DEVICE_TYPE_L_TO_C				(1006 	,"L to C Converter"),
		DEVICE_TYPE_70_TO_KY			(1007 	,"70 to Ku Converter"),
		DEVICE_TYPE_KU_TO_70			(1008 	,"Ku to 70 Converter"),
		DEVICE_TYPE_140_TO_KU			(1009 	,"140 to Ku Converter"),
		DEVICE_TYPE_KU_TO_140			(1010 	,"Ku to 140 Converter"),
		DEVICE_TYPE_KU_TO_L				(1011 	,"Ku to L Converter"),
		DEVICE_TYPE_C_TO_L				(1012 	,"C to L Converter"),
		DEVICE_TYPE_SSPA_MODUL	 		(1051 	,"SSPA Modul"),
		DEVICE_TYPE_MODUL				(1052 	,"Modul"),
		DEVICE_TYPE_BIAS_BOARD_MODUL	(2001	,"Bias Board Modul");

		private int type;
		private String text;

		private DeviceType(int type, String text){
			this.type = type;
			this.text = text;
		}

		public static Optional<DeviceType> valueOf(int type){
			return Arrays.stream(DeviceType.values()).parallel().filter(dt->dt.type==type).findAny();
		}

		@Override
		public String toString(){
			return String.format("%s - %s", type, text);
		}
	}

			private LinkHeader linkHeader;
	private int type;
	private int subtype;
	private int revision;
	private StringData unitPartNumber;
	private StringData serialNumber = new StringData(null);
	private StringData firmwareVersion;
	private StringData firmwareBuildDate;
	private int uptimeCounter;
	private StringData unitName;

	public DeviceInfo(InfoPacket packet) {
		set(packet);
	}

	public DeviceInfo() {}

	public String getTypeStr() {
		return DeviceType.valueOf(type).map(dt->dt.toString()).orElse("Reconnect the Unit");
	}

	public StringData getSerialNumber() {
		return serialNumber;
	}

	public int getRevision() {
		return revision;
	}

	public int getSubtype() {
		return subtype;
	}

	public Optional<DeviceType> getType() {
		return DeviceType.valueOf(type);
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public void setSubtype(int subtype) {
		this.subtype = subtype;
	}

	public void setSerialNumber(StringData serialNumber) {
		this.serialNumber = serialNumber;
	}

	public boolean set(InfoPacket packet) {
		boolean isSet = false;
		if(packet!=null && packet.getPacketHeader()!=null){
			linkHeader = packet.getLinkHeader();
			List<Payload> payloads = packet.getPayloads();
			if(payloads!=null){
				for (Payload pl : payloads) {
					ParameterHeader ph = pl.getParameterHeader();
					switch (ph.getParameterHeaderCode()) {
					case DI_DEVICE_TYPE:
						set(pl.getBuffer());
						break;
					case DI_DEVICE_SN:
						serialNumber = pl.getStringData();
						break;
					case DI_FIRMWARE_VERSION:
						firmwareVersion = pl.getStringData();
						break;
					case DI_FIRMWARE_BUILD_DATE:
						firmwareBuildDate = pl.getStringData();
						break;
					case DI_UNIT_UPTIME_COUNTER:
						uptimeCounter = pl.getInt(0);
						break;
					case DI_UNIT_NAME:
						unitName = pl.getStringData();
						break;
					case DI_UNIT_PART_NUMBER:
						unitPartNumber = pl.getStringData();
						break;
					default:
						logger.warn("not used - {}", packet);
					}
				}
				isSet = true;
			}
		}
		return isSet;
	}

	public byte[] set(byte[]data){
		if(data!=null && data.length>=SIZE){
			type = (int) Packet.shiftAndAdd(Arrays.copyOf(data, REVISION_FIRST_BYTE));
			revision = (int) Packet.shiftAndAdd(Arrays.copyOfRange(data, REVISION_FIRST_BYTE,SUBTYPE_FIRST_BYTE));
			subtype = (int) Packet.shiftAndAdd(Arrays.copyOfRange(data, SUBTYPE_FIRST_BYTE,SIZE));
		}

		return data!=null && data.length>SIZE ? Arrays.copyOfRange(data, SIZE, data.length) : null;
	}

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	public StringData getFirmwareVersion() {
		return firmwareVersion;
	}

	public StringData getFirmwareBuildDate() {
		return firmwareBuildDate;
	}

	public int getUptimeCounter() {
		return uptimeCounter;
	}

	public StringData getUnitName() {
		return unitName;
	}

	public StringData getUnitPartNumber() {
		return unitPartNumber;
	}

	public boolean hasSlaveBiasBoard() {
		final Optional<DeviceType> t = getType();
		return 	t.filter(dt->dt==DeviceType.DEVICE_TYPE_BIAS_BOARD).isPresent()
				&& t.filter(dt->dt==DeviceType.DEVICE_TYPE_SSPA).isPresent()
				&& getSubtype()>=10;
	}

	@Override
	public String toString() {
		return "\n\tDeviceInfo [linkHeader=" + linkHeader + ", type=" + type + ", revision=" + revision + ", subtype=" + subtype + ", serialNumber=" + serialNumber
				+ ", firmwareVersion=" + firmwareVersion + ", firmwareBuildDate=" + firmwareBuildDate + ", uptimeCounter=" + uptimeCounter + ", unitName="
				+ unitName + "]";
	}
}
