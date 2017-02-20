package irt.data.value.enumes;

import java.util.Arrays;

import irt.data.value.interfaces.StatusBits;

public enum StatusBitsFcm implements StatusBits{

	LOCK1			,	//FCM_STATUS_LOCK_DETECT_PLL1
	LOCK2			,	//FCM_STATUS_LOCK_DETECT_PLL2
	MUTE			,	//FCM_STATUS_MUTE
	MUTE_TTL		,	//FCM_STATUS_TTL_MUTE_CONTROL

	LOCK3			,	//FCM_STATUS_LOCK_DETECT_PLL3
	LOCK			,	//FCM_STATUS_LOCK_DETECT_SUMMARY
	INPUT_OWERDRIVE	,	//FCM_STATUS_INPUT_OVERDRIVE
	LOW_POWER;

	private int value;

	private StatusBitsFcm(){
		value = 1<<ordinal();
	}

	@Override public boolean isOn(Integer status) {
		return (status & value) != 0;
	}

	public static StatusBits[] values(final Integer status) {
		return Arrays.stream(StatusBitsFcm.values()).filter(v->v.isOn(status)).toArray(StatusBits[]::new);
	}
}
