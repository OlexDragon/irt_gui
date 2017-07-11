package irt.data;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacketImp;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;

public class DeviceInfo {

	protected final Logger logger = (Logger) LogManager.getLogger();

	public static final int REVISION_FIRST_BYTE = 4;
	public static final int SUBTYPE_FIRST_BYTE 	= 8;
	public static final int SIZE 				= 12;

	public static enum Protocol{
								ALL,
								CONVERTER,
								LINKED,
								DEMO;
	};

	public enum DeviceType{
		BIAS_BOARD		(2, Protocol.LINKED, "BIAS_BOARD"),
		PICOBUC_L_TO_KU	(100, Protocol.LINKED, "PICOBUC_L_TO_KU"),
		PICOBUC_L_TO_C 	(101, Protocol.LINKED, "PICOBUC_L_TO_C"),
		SSPA 			(102, Protocol.LINKED, "SSPA"),
		FUTURE_BIAS_BOARD(199, Protocol.LINKED, "FUTURE_BIAS_BOARD"),
		HPB_L_TO_KU		(200, Protocol.LINKED, "HPB_L_TO_KU"),
		HPB_L_TO_C		(201, Protocol.LINKED, "HPB_L_TO_C"),
		HPB_SSPA		(202, Protocol.LINKED, "HPB_SSPA"),
		KA_BAND			(210, Protocol.LINKED, "KA_BAND"),
		KA_SSPA			(211, Protocol.LINKED, "KA_SSPA"),

		DLRS			(410, Protocol.LINKED, "DLRS"),

		CONVERTER_L_TO_KU_OUTDOOR (500, Protocol.CONVERTER, "L to Ku Converter"),
		CONVERTER_70_TO_L		(1001, Protocol.CONVERTER, "70 to L Converter"),
		CONVERTER_L_TO_70		(1002, Protocol.CONVERTER, "L to 70 Converter"),
		CONVERTER_140_TO_L		(1003, Protocol.CONVERTER, "140 to L Converter"),
		CONVERTER_L_TO_140		(1004, Protocol.CONVERTER, "L to 140 Converter"),
		CONVERTER_L_TO_KU		(1005, Protocol.CONVERTER, "L to Lu Converter"),
		CONVERTER_L_TO_C		(1006, Protocol.CONVERTER, "L to C Converter"),
		CONVERTER_70_TO_KY		(1007, Protocol.CONVERTER, "70 to Ku Converter"),
		CONVERTER_KU_TO_70		(1008, Protocol.CONVERTER, "Ku to 70 Converter"),
		CONVERTER_140_TO_KU		(1009, Protocol.CONVERTER, "140 to Ku Converter"),
		CONVERTER_KU_TO_140		(1010, Protocol.CONVERTER, "Ku to 140 Converter"),
		CONVERTER_KU_TO_L		(1011, Protocol.CONVERTER, "Lu to L Converter"),
		CONVERTER_C_TO_L		(1012, Protocol.CONVERTER, "C to L Converter"),
		CONVERTER_L_TO_DBS		(1013, Protocol.CONVERTER, "L to DBS Converter"),
		CONVERTER_L_TO_KA		(1019, Protocol.CONVERTER, "L to KA Converter"),
		CONVERTER_SSPA 			(1051, Protocol.CONVERTER, "L to SSPA Converter"),
		CONVERTER_MODUL			(1052, Protocol.CONVERTER, "Modul"),

		BIAS_BOARD_MODUL	(2001, Protocol.LINKED, "Bias Board Modul"),

		IMPOSSIBLE				( 0, null, "Impossible meaning");

		public final int TYPE_ID;
		public final Protocol PROTOCOL;
		public final String DESCRIPTION;

		private DeviceType(int typeId, Protocol protocol, String description){
			TYPE_ID = typeId;
			PROTOCOL = protocol;
			DESCRIPTION = description;
		}

		public static Optional<DeviceType> valueOf(int typeId){
			return Arrays.stream(values()).parallel().filter(dt->dt.TYPE_ID==typeId).findAny();
		}

		public String toStrong(){
			return DESCRIPTION;
		}
	}

	private LinkHeader linkHeader;
	private int typeId;
	private Optional<DeviceType> deviceType = Optional.empty();
	private int revision;
	private int subtype;
	private Optional<String> unitPartNumber = Optional.empty();
	private Optional<String> serialNumber = Optional.empty();
	private Optional<String> firmwareVersion = Optional.empty();
	private Optional<String> firmwareBuildDate = Optional.empty();
	private int uptimeCounter;
	private Optional<String> unitName = Optional.empty();

	public DeviceInfo(Packet packet) {
		set(packet);
	}

	public DeviceInfo() {}

	public Optional<String> getSerialNumber() {
		return serialNumber;
	}

	public int getRevision() {
		return revision;
	}

	public int getSubtype() {
		return subtype;
	}

	public int getTypeId() {
		return typeId;
	}

	public Optional<DeviceType> getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(int typeId) {
		this.typeId = typeId;
		this.deviceType = DeviceType.valueOf(typeId);
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public void setSubtype(int subtype) {
		this.subtype = subtype;
	}

	public void setSerialNumber(StringData serialNumber) {
		this.serialNumber = Optional.ofNullable(serialNumber).map(StringData::toString);
	}

	public boolean set(Packet packet) {
		boolean isSet = false;
		if(packet!=null && packet.getHeader()!=null && packet.getHeader().getGroupId()==PacketImp.GROUP_ID_DEVICE_INFO){

			linkHeader = packet instanceof LinkedPacket ? ((LinkedPacketImp)packet).getLinkHeader() : null;
			List<Payload> payloads = packet.getPayloads();

			if(payloads!=null){
				for (Payload pl : payloads)
					switch (pl.getParameterHeader().getCode()) {
					case Payload.DI_DEVICE_TYPE:
						set(pl.getBuffer());
						break;
					case Payload.DI_DEVICE_SN:
						serialNumber = Optional.of(pl.getStringData()).map(StringData::toString);
						break;
					case Payload.DI_FIRMWARE_VERSION:
						firmwareVersion = Optional.of(pl.getStringData()).map(StringData::toString);
						break;
					case Payload.DI_FIRMWARE_BUILD_DATE:
						firmwareBuildDate = Optional.of(pl.getStringData()).map(StringData::toString);
						break;
					case Payload.DI_UNIT_UPTIME_COUNTER:
						uptimeCounter = pl.getInt(0);
						break;
					case Payload.DI_UNIT_NAME:
						unitName = Optional.of(pl.getStringData()).map(StringData::toString);
						break;
					case Payload.DI_UNIT_PART_NUMBER:
						unitPartNumber = Optional.of(pl.getStringData()).map(StringData::toString);
						break;
					default:
						logger.warn("not used - {}", packet);
					}
				isSet = true;
			}
		}
		return isSet;
	}

	public byte[] set(byte[]data){
		if(data!=null && data.length>=SIZE){
			typeId = (int) PacketImp.shiftAndAdd(Arrays.copyOf(data, REVISION_FIRST_BYTE));
			deviceType = DeviceType.valueOf(typeId);
			revision = (int) PacketImp.shiftAndAdd(Arrays.copyOfRange(data, REVISION_FIRST_BYTE,SUBTYPE_FIRST_BYTE));
			subtype = (int) PacketImp.shiftAndAdd(Arrays.copyOfRange(data, SUBTYPE_FIRST_BYTE,SIZE));
		}

		return data!=null && data.length>SIZE ? Arrays.copyOfRange(data, SIZE, data.length) : null;
	}

	public LinkHeader getLinkHeader() {
		return Optional.ofNullable(linkHeader).orElse(new LinkHeader((byte)0, (byte)0, (short) 0));
	}

	public Optional<String> getFirmwareVersion() {
		return firmwareVersion;
	}

	public Optional<String> getFirmwareBuildDate() {
		return firmwareBuildDate;
	}

	public int getUptimeCounter() {
		return uptimeCounter;
	}

	public void setUptimeCounter(int uptimeCounter) {
		this.uptimeCounter = uptimeCounter;
	}

	public Optional<String> getUnitName() {
		return unitName;
	}

	public Optional<String> getUnitPartNumber() {
		return unitPartNumber;
	}

	public boolean hasSlaveBiasBoard() {
		return 	deviceType
				.filter(dt->getSubtype()>10)
				.filter(dt->dt.TYPE_ID>=DeviceType.BIAS_BOARD.TYPE_ID)
				.filter(dt->dt.TYPE_ID<=DeviceType.SSPA.TYPE_ID)
				.map(dt->true)
				.orElse(false);
	}

	@Override
	public int hashCode() {
		return 31  + ((serialNumber == null) ? 0 : serialNumber.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeviceInfo other = (DeviceInfo) obj;
		if (serialNumber == null) {
			if (other.serialNumber != null)
				return false;
		} else if (!serialNumber.equals(other.serialNumber))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "\n\tDeviceInfo [linkHeader=" + linkHeader + ", type=" + typeId + ", revision=" + revision + ", subtype=" + subtype + ", serialNumber=" + serialNumber.orElse(null)
				+ ", firmwareVersion=" + firmwareVersion.orElse(null) + ", firmwareBuildDate=" + firmwareBuildDate.orElse(null) + ", uptimeCounter=" + uptimeCounter + ", unitName="
				+ unitName.orElse(null) + "]";
	}

	public void set(DeviceInfo deviceInfo) {

		deviceInfo
		.getSerialNumber()
		.filter(sn->!sn.equals(serialNumber))
		.orElseThrow(()->new IllegalArgumentException(this +  deviceInfo.toString()));

		setDeviceType(deviceInfo.typeId);
		setRevision(deviceInfo.revision);
		setSubtype(deviceInfo.subtype);
		setUptimeCounter(deviceInfo.uptimeCounter);
	}
}
