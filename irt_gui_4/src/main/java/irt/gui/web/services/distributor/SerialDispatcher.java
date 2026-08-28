package irt.gui.web.services.distributor;

import irt.gui.web.beans.RequestPacket;

public interface SerialDispatcher {

    RequestPacket dispatch(RequestPacket requestPacket)
            throws Exception; // refine later (IrtSerialPortIOException, etc.)
}
