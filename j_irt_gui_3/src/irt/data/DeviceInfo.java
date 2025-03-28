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
import irt.data.packet.PacketID;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.UpdateFx;

public class DeviceInfo implements PacketListener {

	protected final Logger logger = LogManager.getLogger();

	public static final int REVISION_FIRST_BYTE = 4;
	public static final int SUBTYPE_FIRST_BYTE 	= 8;
	public static final int SIZE 				= 12;

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
					case Payload.DI_FIRMWARE_VERSION:
						firmwareVersion = Optional.of(pl.getStringData()).map(StringData::toString);
						break;
					case Payload.DI_FIRMWARE_BUILD_DATE:
						firmwareBuildDate = Optional.of(pl.getStringData()).map(StringData::toString);
						break;
					case Payload.DI_UNIT_UPTIME_COUNTER:
						uptimeCounter = pl.getInt(0);
						break;
					case Payload.DI_DEVICE_SN:
						serialNumber = Optional.of(pl.getStringData()).map(StringData::toString);
						serialNumber.ifPresent(UpdateFx::setSerialNumber);
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

	public static Optional<DeviceInfo> parsePacket(Packet packet){

		return Optional
		.ofNullable(packet)
		.filter(p->p.getHeader()!=null)
		.filter(p->p.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
		.filter(p->{ return PacketID.DEVICE_INFO.match(p.getHeader().getPacketId()) || PacketID.DEVICE_INFO_CONVERTER.match(p.getHeader().getPacketId()); })
		.map(DeviceInfo::new);		
	}

	public Byte getAddr() {
		return Optional.ofNullable(linkHeader).map(LinkHeader::getAddr).orElse((byte) 0);
	}
}
