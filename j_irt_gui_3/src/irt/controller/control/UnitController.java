package irt.controller.control;

import irt.data.listener.PacketListener;

public interface UnitController extends Runnable, PacketListener{

	void start();
	void stop();
}
