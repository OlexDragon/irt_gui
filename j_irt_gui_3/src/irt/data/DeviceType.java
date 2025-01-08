package irt.data;

import java.util.Arrays;
import java.util.Optional;

public enum DeviceType{

	MAIN_CONTROLLER	(1, Protocol.LINKED, "MAIN CONTROLLER"	, HardwareType.CONTROLLER),
	BIAS_BOARD		(2, Protocol.LINKED, "BIAS_BOARD"		, HardwareType.BAIS),
	PICOBUC_L_TO_KU	(100, Protocol.LINKED, "PICOBUC_L_TO_KU", HardwareType.BAIS),
	PICOBUC_L_TO_C 	(101, Protocol.LINKED, "PICOBUC_L_TO_C"	, HardwareType.BAIS),
	C_SSPA 			(102, Protocol.LINKED, "C Band SSPA"	, HardwareType.BAIS),
	BUC_LP_KU	 	(110, Protocol.LINKED, "BUC Low Power Ku"	, HardwareType.BAIS),
	BUC_LP_C 		(111, Protocol.LINKED, "BUC Low Power C"	, HardwareType.BAIS),
	BUC_LP_SSPA		(112, Protocol.LINKED, "BUC Low Power SSPA", HardwareType.BAIS),
	BUC_LP_LC		(113, Protocol.LINKED, "BUC Low Power LOW C", HardwareType.BAIS),
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

	ORPC					(301, Protocol.LINKED, "Outdoor Redundancy Protection Controller", HardwareType.CONTROLLER),
	IR_PC					(310, Protocol.LINKED, "Redundancy protection controller", HardwareType.CONTROLLER),
	TWO_SWITCHS_CONTROLLER	(311, Protocol.LINKED, "2 switchs Redundancy protection controller", HardwareType.CONTROLLER),
	ORPC_GD					(313, Protocol.LINKED, "Outdoor Redundancy Protection Controller", HardwareType.CONTROLLER),
	RCP_FOR_ORCP			(314, Protocol.LINKED, "Remote Control Panel for ORCP", HardwareType.CONTROLLER),

	DLRS					(410, Protocol.LINKED, "DLRS", HardwareType.CONTROLLER),	//Down link Redundancy System
	DLRS2					(411, Protocol.LINKED, "DLRS2", HardwareType.CONTROLLER),
	LNB_REDUNDANCY_1x2		(412, Protocol.LINKED, "1:2 Redundancy LNB", HardwareType.CONTROLLER),

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

	REFERENCE_BOARD			(1100, Protocol.CONVERTER, "Reference Board"		, HardwareType.CONVERTER),

	BIAS_BOARD_MODUL		(2001, Protocol.CONVERTER, "Bias Board Modul", HardwareType.BAIS),

	IMPOSSIBLE				( 0, null, "Impossible meaning", null);

	public final int TYPE_ID; public int getTypeId() {return TYPE_ID;}
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