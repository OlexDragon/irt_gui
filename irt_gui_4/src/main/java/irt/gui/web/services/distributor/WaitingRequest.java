package irt.gui.web.services.distributor;

import java.util.concurrent.FutureTask;

import irt.gui.web.beans.RequestPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class WaitingRequest {

	private final RequestPacket requestPacket;
	private final FutureTask<RequestPacket> future;
}
