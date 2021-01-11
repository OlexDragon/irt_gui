package irt.data;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketIDs;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;

public class DeviceInfo implements PacketListener {

	protected final Logger logger = LogManager.getLogger();

	public static final int REVISION_FIRST_BYTE = 4;
	public static final int SUBTYPE_FIRST_BYTE 	= 8;
	public static final int SIZE 				= 12;

	public static enum Protocol{
								ALL,
								CONVERTER,
								LINKED,
								DEMO;
	};

	public static enum HardwareType{
		CONVERTER,
		CONTROLLER,
		BAIS,
		HP_BAIS,
		KA_BIAS,
		NEW_BAIS
	}

	public enum DeviceType{
		MAIN_CONTROLLER	(1, Protocol.LINKED, "MAIN CONTROLLER"	, HardwareType.CONTROLLER),
		BIAS_BOARD		(2, Protocol.LINKED, "BIAS_BOARD"		, HardwareType.BAIS),
		PICOBUC_L_TO_KU	(100, Protocol.LINKED, "PICOBUC_L_TO_KU", HardwareType.BAIS),
		PICOBUC_L_TO_C 	(101, Protocol.LINKED, "PICOBUC_L_TO_C"	, HardwareType.BAIS),
		C_SSPA 			(102, Protocol.LINKED, "C Band SSPA"	, HardwareType.BAIS),
		FUTURE_BIAS_BOARD(199, Protocol.LINKED, "FUTURE_BIAS_BOARD", HardwareType.BAIS),
		HPB_L_TO_KU		(200, Protocol.LINKED, "HPB_L_TO_KU"	, HardwareType.HP_BAIS),
		HPB_L_TO_C		(201, Protocol.LINKED, "HPB_L_TO_C"		, HardwareType.HP_BAIS),
		HPB_SSPA		(202, Protocol.LINKED, "HPB_SSPA"		, HardwareType.HP_BAIS),
		KA_BAND			(210, Protocol.LINKED, "KA_BAND"		, HardwareType.KA_BIAS),
		KA_SSPA			(211, Protocol.LINKED, "KA_SSPA"		, HardwareType.KA_BIAS),

		KU_RACK_MOUNT	(250, Protocol.LINKED, "KU rack mount", HardwareType.CONTROLLER),
		C_RACK_MOUNT	(251, Protocol.LINKED, "C rack mount", HardwareType.CONTROLLER),
		C_SSPA_RACK_MOUNT(252, Protocol.LINKED, "C rack mount SSPA", HardwareType.CONTROLLER),

		TRANSCEIVER		(260, Protocol.LINKED, "Transceiver", HardwareType.BAIS),

		IR_PC			(310, Protocol.LINKED, "Redundancy protection controller", HardwareType.CONTROLLER),

		DLRS			(410, Protocol.LINKED, "DLRS", HardwareType.CONTROLLER),	//Down link Redundancy System
		DLRS2			(411, Protocol.LINKED, "DLRS2", HardwareType.CONTROLLER),

		CONVERTER_L_TO_KU_OUTDOOR (500, Protocol.LINKED, "L to Ku Converter"	, HardwareType.CONVERTER),
		CONVERTER_70_TO_L		(1001, Protocol.CONVERTER, "70 to L Converter"	, HardwareType.CONVERTER),
		CONVERTER_L_TO_70		(1002, Protocol.CONVERTER, "L to 70 Converter"	, HardwareType.CONVERTER),
		CONVERTER_140_TO_L		(1003, Protocol.CONVERTER, "140 to L Converter"	, HardwareType.CONVERTER),
		CONVERTER_L_TO_140		(1004, Protocol.CONVERTER, "L to 140 Converter"	, HardwareType.CONVERTER),
		CONVERTER_L_TO_KU		(1005, Protocol.CONVERTER, "L to Lu Converter"	, HardwareType.CONVERTER),
		CONVERTER_L_TO_C		(1006, Protocol.CONVERTER, "L to C Converter"	, HardwareType.CONVERTER),
		CONVERTER_70_TO_KY		(1007, Protocol.CONVERTER, "70 to Ku Converter"	, HardwareType.CONVERTER),
		CONVERTER_KU_TO_70		(1008, Protocol.CONVERTER, "Ku to 70 Converter"	, HardwareType.CONVERTER),
		CONVERTER_140_TO_KU		(1009, Protocol.CONVERTER, "140 to Ku Converter", HardwareType.CONVERTER),
		CONVERTER_KU_TO_140		(1010, Protocol.CONVERTER, "Ku to 140 Converter", HardwareType.CONVERTER),
		CONVERTER_KU_TO_L		(1011, Protocol.CONVERTER, "Lu to L Converter"	, HardwareType.CONVERTER),
		CONVERTER_C_TO_L		(1012, Protocol.CONVERTER, "C to L Converter"	, HardwareType.CONVERTER),
		CONVERTER_L_TO_DBS		(1013, Protocol.CONVERTER, "L to DBS Converter"	, HardwareType.CONVERTER),
		CONVERTER_L_TO_KA		(1019, Protocol.CONVERTER, "L to KA Converter"	, HardwareType.CONVERTER),
		CONVERTER_L_TO_X		(1021, Protocol.CONVERTER, "L to X Converter"	, HardwareType.CONVERTER),
		CONVERTER_SSPA 			(1051, Protocol.CONVERTER, "L to SSPA Converter", HardwareType.CONVERTER),
		CONVERTER_MODUL			(1052, Protocol.CONVERTER, "Modul"				, HardwareType.CONVERTER),
		CONVERTER_MODUL_C_BAND	(1053, Protocol.CONVERTER, "C Band Modul"		, HardwareType.CONVERTER),

		REFERENCE_BOARD		(1100, Protocol.CONVERTER, "Reference Board"		, HardwareType.CONVERTER),

		BIAS_BOARD_MODUL	(2001, Protocol.CONVERTER, "Bias Board Modul", HardwareType.BAIS),

		IMPOSSIBLE				( 0, null, "Impossible meaning", null);

		public final int TYPE_ID;
		public final Protocol PROTOCOL;
		public final String DESCRIPTION;
		public final HardwareType HARDWARE_TYPE;

		private DeviceType(int typeId, Protocol protocol, String description, HardwareType hardwareType){
			TYPE_ID = typeId;
			PROTOCOL = protocol;
			DESCRIPTION = description;
			HARDWARE_TYPE = hardwareType;
		}

		public Boolean isFCM() {
			return PROTOCOL==Protocol.CONVERTER;
		}

		public String toStrong(){
			return DESCRIPTION;
		}

		public static Optional<DeviceType> valueOf(int typeId){
			return Arrays.stream(values()).parallel().filter(dt->dt.TYPE_ID==typeId).findAny();
		}

		public static Boolean isFCM(Integer typeId){

			//0 - device type is not defined
			if(typeId==null || typeId==0)
				return null;

			final Optional<DeviceType> valueOf = valueOf(typeId);
			return valueOf.map(dt->dt.PROTOCOL).map(pr->pr==Protocol.CONVERTER).orElse(null);
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
		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
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
		if(this.typeId == typeId)
			return;
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
		if(packet!=null && packet.getHeader()!=null && PacketGroupIDs.DEVICE_INFO.match(packet.getHeader().getGroupId())){

			linkHeader = packet instanceof LinkedPacket ? ((LinkedPacket)packet).getLinkHeader() : null;
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
				.filter(dt->dt.TYPE_ID<=DeviceType.C_SSPA.TYPE_ID)
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
		.filter(sn->serialNumber.filter(s->sn.equals(s)).isPresent())
		.orElseThrow(()->new IllegalArgumentException(this +  deviceInfo.toString()));

		setDeviceType(deviceInfo.typeId);
		setRevision(deviceInfo.revision);
		setSubtype(deviceInfo.subtype);
		setUptimeCounter(deviceInfo.uptimeCounter);
	}

	@Override
	public void onPacketReceived(Packet packet) {
		new ThreadWorker(()->{
	
			try {

				DeviceInfo
				.parsePacket(packet)
				.filter(di->di.serialNumber.equals(serialNumber))
				.ifPresent(this::set);

			}catch (Exception e) {
				logger.catching(e);
			}
		}, "DeviceInfo.onPacketReceived()");
	}

	static public Optional<DeviceInfo> parsePacket(Packet packet){

		return Optional
		.ofNullable(packet)
		.filter(p->p.getHeader()!=null)
		.filter(p->PacketIDs.DEVICE_INFO.match(p.getHeader().getPacketId()))
		.filter(p->p.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
		.map(DeviceInfo::new);		
	}

	public Byte getAddr() {
		return Optional.ofNullable(linkHeader).map(LinkHeader::getAddr).orElse((byte) 0);
	}
}
