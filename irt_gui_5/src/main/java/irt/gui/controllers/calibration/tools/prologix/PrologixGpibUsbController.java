package irt.gui.controllers.calibration.tools.prologix;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import irt.gui.controllers.calibration.tools.prologix.enums.Eos;
import irt.gui.controllers.calibration.tools.prologix.enums.PrologixDeviceType;
import irt.gui.controllers.enums.FalseOrTrue;

public class PrologixGpibUsbController {

	protected static final Logger logger = (Logger) LogManager.getLogger();

	public static final int NONE 	= 3;
	public static final int DON 	= 1;
	public static final int NOT_FAUND = 0;
	public static final int UNRECOGNIZED_COMMAND = -1;

	public static final String WRITE_END = Eos.LF.toString();
	public static final String READ_END = Eos.CR_LF.toString();

	private int addr;
	private FalseOrTrue eoi;
	/**
	 * where 0-CR+LF, 1-CR, 2-LF, 3-None;
	 */
	private Eos 		eos;
	private FalseOrTrue eot_enable;
	private char 		eot_char;
	/**
	 * where true(1)-Controller, false(0)-Device;
	 */
	private PrologixDeviceType	mode;
	private FalseOrTrue savecfg;
	private String		ver;
	private FalseOrTrue auto;
	/**
	 * timeout value between 1 and 3000 (milliseconds);
	 */
	private int 		read_tmo_ms;
	private FalseOrTrue lon;
	private int 		status;

	public int getAddr() {
		return addr;
	}
	public FalseOrTrue isEoi() {
		return eoi;
	}
	public Eos getEos() {
		return eos;
	}
	public FalseOrTrue getEot_enable() {
		return eot_enable;
	}
	public char getEot_char() {
		return eot_char;
	}
	public PrologixDeviceType getMode() {
		return mode;
	}
	public FalseOrTrue getSavecfg() {
		return savecfg;
	}
	public String getVer() {
		return ver;
	}
	public FalseOrTrue getAuto() {
		return auto;
	}
	public int getRead_tmo_ms() {
		return read_tmo_ms;
	}
	public FalseOrTrue getLon() {
		return lon;
	}
	public int getStatus() {
		return status;
	}
	public void setAddr(int addr) {
		this.addr = addr;
	}
	public void setEoi(FalseOrTrue eoi) {
		this.eoi = eoi;
	}
	public void setEos(int eos) {
		this.eos = Eos.values()[eos];
	}
	public void setEot_enable(FalseOrTrue eot_enable) {
		this.eot_enable = eot_enable;
	}
	public void setEot_char(char eot_char) {
		this.eot_char = eot_char;
	}
	public void setMode(PrologixDeviceType mode) {
		this.mode = mode;
	}
	public void setSavecfg(FalseOrTrue savecfg) {
		this.savecfg = savecfg;
	}
	public void setVer(String ver) {
		this.ver = ver;
	}
	public void setAuto(FalseOrTrue auto) {
		this.auto = auto;
	}
	public void setRead_tmo_ms(int read_tmo_ms) throws IllegalArgumentException{
		if(read_tmo_ms>0 && read_tmo_ms<=3000)
			this.read_tmo_ms = read_tmo_ms;
		else
			throw new IllegalArgumentException("Value "+read_tmo_ms+" out of range.(min=1, max=3000");
	}
	public void setLon(FalseOrTrue lon) {
		this.lon = lon;
	}
	public void setStatus(int status) {
		this.status = status;
	}
}
