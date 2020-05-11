package irt.controller.file;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.FutureTask;

import irt.data.DeviceInfo;
import irt.tools.fx.interfaces.StopInterface;

public class ProfileScannerFT extends FutureTask<Optional<Path>> implements StopInterface {

	private static ProfileScanner CALLABLE;

	public ProfileScannerFT(DeviceInfo deviceInfo) {
		super(CALLABLE = new ProfileScanner(deviceInfo.getSerialNumber().map(sn->sn + ".bin")));
	}

	@Override
	public void stop() {
		CALLABLE.stop();
	}
}
