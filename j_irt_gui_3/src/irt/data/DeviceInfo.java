package irt.data;

import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.data.packet.Payload;
import irt.tools.panel.subpanel.InfoPanel;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class DeviceInfo {

	protected final Logger logger = (Logger) LogManager.getLogger();

	public static final int REVISION_FIRST_BYTE = 4;
	public static final int SUBTYPE_FIRST_BYTE 	= 8;
	public static final int SIZE 				= 12;

	public static final int DEVICE_TYPE_BAIS_BOARD		= 2,
							DEVICE_TYPE_PICOBUC_L_TO_KU = 100,
							DEVICE_TYPE_PICOBUC_L_TO_C 	= 101,
							DEVICE_TYPE_SSPA 			= 102,

							DEVICE_TYPE_70_TO_L		= 1001,
							DEVICE_TYPE_L_TO_70		= 1002,
							DEVICE_TYPE_140_TO_L	= 1003,
							DEVICE_TYPE_L_TO_140	= 1004,
							DEVICE_TYPE_L_TO_KU		= 1005,
							DEVICE_TYPE_L_TO_C		= 1006,
							DEVICE_TYPE_70_TO_KY	= 1007,
							DEVICE_TYPE_KU_TO_70	= 1008,
							DEVICE_TYPE_140_TO_KU	= 1009,
							DEVICE_TYPE_KU_TO_140	= 1010,
							DEVICE_TYPE_SSPA_CONVERTER = 1051;

	private LinkHeader linkHeader;
	private int type;
	private int revision;
	private int subtype;
	private StringData unitPartNumber;
	private StringData serialNumber = new StringData(null);
	private StringData firmwareVersion;
	private StringData firmwareBuildDate;
	private int uptimeCounter;
	private StringData unitName;
	private InfoPanel infoPanel;

	public DeviceInfo(Packet packet) {
		set(packet);
	}

	public DeviceInfo() {}

	public String getTypeStr() {
		String deviceTypeStr = "Reconnect the Unit";

		switch(type){
		case DEVICE_TYPE_70_TO_L:
			deviceTypeStr = "70 to L Converter";
			break;
		case DEVICE_TYPE_L_TO_70:
			deviceTypeStr = "L to 70 Converter";
			break;
		case DEVICE_TYPE_140_TO_L:
			deviceTypeStr = "140 to L Converter";
			break;
		case DEVICE_TYPE_L_TO_140:
			deviceTypeStr = "L to 140 Converter";
			break;
		case DEVICE_TYPE_L_TO_KU:
			deviceTypeStr = "L to Ku Converter";
			break;
		default:
			deviceTypeStr = "N/A";
		}

		return deviceTypeStr;
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

	public int getType() {
		return type;
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

	public boolean set(Packet packet) {
		boolean isSet = false;
		if(packet!=null && packet.getHeader()!=null && packet.getHeader().getGroupId()==Packet.IRT_SLCP_PACKET_ID_DEVICE_INFO){
			linkHeader = packet instanceof LinkedPacket ? ((LinkedPacket)packet).getLinkHeader() : null;
			List<Payload> payloads = packet.getPayloads();
			if(payloads!=null){
				for (Payload pl : payloads)
					switch (pl.getParameterHeader().getCode()) {
					case Payload.DI_DEVICE_TYPE:
						set(pl.getBuffer());
						break;
					case Payload.DI_DEVICE_SN:
						serialNumber = pl.getStringData();
						break;
					case Payload.DI_FIRMWARE_VERSION:
						firmwareVersion = pl.getStringData();
						break;
					case Payload.DI_FIRMWARE_BUILD_DATE:
						firmwareBuildDate = pl.getStringData();
						break;
					case Payload.DI_UNIT_UPTIME_COUNTER:
						uptimeCounter = pl.getInt(0);
						break;
					case Payload.DI_UNIT_NAME:
						unitName = pl.getStringData();
						break;
					case Payload.DI_UNIT_PART_NUMBER:
						unitPartNumber = pl.getStringData();
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

	@Override
	public String toString() {
		return "DeviceInfo [linkHeader=" + linkHeader + ", type=" + type + ", revision=" + revision + ", subtype=" + subtype + ", serialNumber=" + serialNumber
				+ ", firmwareVersion=" + firmwareVersion + ", firmwareBuildDate=" + firmwareBuildDate + ", uptimeCounter=" + uptimeCounter + ", unitName="
				+ unitName + "]";
	}

	public void setError(String errorStr, Color errorColor) {
		infoPanel.setError(errorStr, errorColor);
	}

	public void setInfoPanel(InfoPanel infoPanel) {
		this.infoPanel = infoPanel;
		infoPanel.setInfo(this);
	}

	public StringData getUnitPartNumber() {
		return unitPartNumber;
	}
}
