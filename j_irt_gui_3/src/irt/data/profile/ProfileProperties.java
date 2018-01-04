
package irt.data.profile;

import java.util.Arrays;
import java.util.List;

public enum ProfileProperties {
	/**
	 * Defines a type of the device (range 0 to 2^^32-1).<br>
	 *  Right to now, the following types are implemented:<br>
	 *  1 – IFC main controller<br>
	 *  2 – Generic PicoBUC Module<br>
	 *  100 – Ku-band PicoBUC Module<br>
	 *  101 – C-band PicoBUC Module<br>
	 *  1001 – 70MHz to L-band Up-Converter Module<br>
	 *  1002 – L-band to 70MHz Down-Converter Module<br>
	 *  1003 – 140MHz to L-band Up-Converter Module<br>
	 *  1004 – L-band to 140MHz Down-Converter Module<br>
	 *  1005 – L-band to Ku-band Up-Converter Module<br>
	 *  1006 – L-band to C-band Up-Converter Module<br>
	 *  1007 – 70MHz to Ku-band Up-Converter Module<br>
	 *  1008 – Ku-band to 70MHz Down-Converter Module<br>
	 *  1009 – 140MHz to Ku-band Up-Converter  Module<br>
	 *  1010 – Ku-band to 140MHz Down-Converter Module
	 */
	DEVICE_TYPE			("device-type", 			Arrays.asList(new String[] { "Common" })),
	DEVICE_REVISION		("device-revision", 		Arrays.asList(new String[] { "Common" })),
	DEVICE_SUBTYPE		("device-subtype",			Arrays.asList(new String[] { "Common", "Optional"})),
	SERIAL_NUMBER("device-serial-number", 			Arrays.asList(new String[] { "Common" })),
	DEVICE_PART_NUMBER	("device-part-number", 		Arrays.asList(new String[] { "Common" })),
	PRODUCT_DESCRIPTION	("product-description", 	Arrays.asList(new String[] { "Common" })),
	CONTACT_INFORMATION	("contact-information ",	Arrays.asList(new String[] { "Common", "Optional" })),
	SYSTEM_NAME			("system-name", 			Arrays.asList(new String[] { "Common" })),
	MAC_ADDRESS			("mac-address", 			Arrays.asList(new String[] { "Common", "Network"})),
	ZERO_ATTENUATION_GAIN("zero-attenuation-gain",	Arrays.asList(new String[] { "Common", "User interface"})),

	INPUT_FREQUENCY		("input-frequency ",		Arrays.asList(new String[] { "FCM", "PLLs"})),
	REF_CLOCK			("ref-clock",				Arrays.asList(new String[] { "FCM", "PLLs"})),
	/**Defines the PLL1 output frequency (range 0 to 2^^64-1).<br>Value is 4120 MHz for 70 MHz converters and<br>4190 MHz – for 140 MHz converters*/
	PLL1_FREQUENCY		("pll1-frequency",			Arrays.asList(new String[] { "FCM", "PLLs"})),
	/**Third DAC configuration value (range 0 to 2^^16-1).<br>It’s relevant just to 70/140 MHz Up/Down converters*/
	DAC_I_VALUE			("dac-I-value",				Arrays.asList(new String[] { "FCM", "DACs"})),
	/**Third DAC configuration value (range 0 to 2^^16-1).<br>It’s relevant just to 70/140 MHz Up/Down converters*/
	DAC_Q_VALUE			("dac-I-value",				Arrays.asList(new String[] { "FCM", "DACs"})),
	/**Defines a RF gain offset parameter (range 0 to 2^^16-1). It’s relevant just for 70/140 MHz Up converters*/
	RF_GAIN_OFFSET		( "rf-gain-offset",			Arrays.asList(new String[] { "FCM", "DACs"})),
	/**Defines an attenuation offset (range 0 to 2^^32-1). This parameter is relevant just for 70/140 MHz Up/Down converters*/
	ATTENUATION_OFFSET	("attenuation-offset",		Arrays.asList(new String[] { "FCM", "DACs"})),
	ATTENUATION_COEFFICIENT("attenuation-coefficient",Arrays.asList(new String[]{"FCM", "DACs"})),

	FREQUENCY_RANGE			("frequency-range",			Arrays.asList(new String[] { "FCM", "User interface"})),
	FREQUENCY_SET			("frequency-set",			Arrays.asList(new String[] { "FCM", "User interface"})),
	ATTENUATION_RANGE		("attenuation-range",		Arrays.asList(new String[] { "FCM", "User interface"})),
	INPUT_POWER_LUT_SIZE	("in-power-lut-size",		Arrays.asList(new String[] { "FCM", "User interface"})),
	INPUT_POWER_LUT_ENTRY	("in-power-lut-entry",		Arrays.asList(new String[] { "FCM", "User interface"})),
	OUTPUT_POWER_LUT_SIZE	("out-power-lut-size",		Arrays.asList(new String[] { "FCM", "User interface"})),
	OUTPUT_POWER_LUT_ENTRY	("out-power-lut-entry",		Arrays.asList(new String[] { "FCM", "User interface"})),
	TEMPERATURE_LUT_SIZE	( "temperature-lut-size",	Arrays.asList(new String[] { "FCM", "User interface"})),
	TEMPERATURE_LUT_ENTRY	("temperature-lut-entry",	Arrays.asList(new String[] { "FCM", "User interface"})),
	GAIN_LUT_SIZE			( "gain-lut-size",			Arrays.asList(new String[] { "FCM", "User interface"})),
	GAIN_LUT_ENTRY			("gain-lut-entry",			Arrays.asList(new String[] { "FCM", "User interface"})),
	/**Defines the relation between gain and DAC value (range ±3.40282347e38).<br>This parameter is relevant just for L-band to C-/Ku- Band Up converters*/
	RF_GAIN_LUT_SIZE		("rf-gain-lut-size",		Arrays.asList(new String[] { "FCM", "User interface"})),
	/**Defines the size of rf-gain table (range 0 to 2^^32-1).<br>This parameter is relevant just for 70/140 MHz Up converters*/
	RF_GAIN_LUT_ENTRY		("rf-gain-lut-entry",		Arrays.asList(new String[] { "FCM", "User interface"})),
	
	POWER_LUT_SIZE			("power-lut-size",			Arrays.asList(new String[] { "BUC", "User interface"})),
	POWER_LUT_ENTRY			("power-lut-entry",			Arrays.asList(new String[] { "BUC", "User interface"})),
	/**Defines thresholds of the current (range 0 to 2^^32-1), where the first argument is index and selects which value to set:<br>
	* 0 – for ZERO current (value under it means the current is zero);<br>
	* 1 – for SW over current of the output device (HS1);<br>
	* 2 – for SW over current of the others (HS2);<br>
	* 3 – for HW over current of the output device (HS1);<br>
	* 4 – for HW over current of the others (HS2).*/
	DEVICE_THRESHOLD_CURRENT("device-threshold-current",		Arrays.asList(new String[] { "BUC", "User interface"})),
	/**Defines three thresholds of the temperature (range 0 to 2^^32-1), where the first argument is index and select which value is set:<br>
	* 1 – mute threshold on over temperature;<br>
	* 2 – unmute threshold on over temperature*/
	DEVICE_THRESHOLD_TEMPERATURE("device-threshold-temperature",Arrays.asList(new String[] { "BUC", "User interface"})),
	/**Defines the source detector of the output power is current of output device (mode 1),<br>
	 * on-board detector (mode 0) or<br>
	 * the input power plus gain (mode 2).<br>
	 * Default is mode 1*/
	POWER_DETECTOR_SOURCE	("power-detector-source",	Arrays.asList(new String[] { "BUC", "User interface"})),
	/**Defines a gain for zero attenuation that will be used for Output Power calculation (range ±2^^15). It’s valid just in case the “power-detector-source” is mode 2*/
	OUTPUT_POWER_ZERO_ATTENUATION_GAIN("out-power-zero-attenuation-gain",	Arrays.asList(new String[] { "BUC", "User interface"})),
	/**Defines the range for output frequency to 5.85-6.7 GHz instead of default 5.85-6.4 GHz for C-band type device*/
	C_BAND_FREQUENCY_RANGE_EXTENDED	("cband-frequency-range-extended",		Arrays.asList(new String[] { "BUC", "User interface"}));

	private String name;
	private List<String> properties;

	private ProfileProperties(String name, List<String> properties) {
		this.name = name;
		this.properties = properties;
	}

	@Override
	public String toString() {
		return name;
	}

	public List<String> getProperties() {
		return properties;
	}
}