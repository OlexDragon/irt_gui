package irt.data.value;

import irt.tools.label.LED;

import java.awt.Color;

public class StaticComponents {

	private static LED ledRx = new LED(Color.GREEN, "");

	public static LED getLedRx() {
		return ledRx;
	}

}
