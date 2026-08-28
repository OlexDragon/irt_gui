package irt.gui.web.services.distributor;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import irt.gui.web.beans.Packet;
import irt.gui.web.beans.RequestPacket;
import irt.gui.web.controllers.ConsoleRestController;
import irt.gui.web.exceptions.IrtSerialPortIOException;
import irt.gui.web.services.serialPort.IrtSerialPort;
import irt.gui.web.services.serialPort.JSerialCommConsole;
import irt.gui.web.services.serialPort.JSerialCommFlash;

@Component
public class DefaultSerialDispatcher implements SerialDispatcher {

    private static final Logger logger = LogManager.getLogger();

    private final IrtSerialPort serialPort;
    private final JSerialCommFlash serialPortFlash;
    private JSerialCommConsole serialPortConsole;

    public DefaultSerialDispatcher(
            @Qualifier("jSerialComm") IrtSerialPort serialPort,
            @Qualifier("jSerialCommFlash") JSerialCommFlash serialPortFlash
    ) {
        this.serialPort = serialPort;
        this.serialPortFlash = serialPortFlash;
    }

    @Override
    public RequestPacket dispatch(RequestPacket requestPacket) throws Exception {

        final String name = Optional.ofNullable(requestPacket.getName()).orElse("");

        switch (name) {
            case "Flash":
                sendFlash(requestPacket);
                break;
            case "Console":
                sendConsole(requestPacket);
                break;
            default:
                sendNormal(requestPacket);
        }

        return requestPacket;
    }

    private void sendFlash(RequestPacket requestPacket) throws IrtSerialPortIOException {
        logger.traceEntry("{}", requestPacket);

        final String portName = requestPacket.getSerialPort();
        final int timeout = Optional.ofNullable(requestPacket.getTimeout()).orElse(100);
        final byte[] bytes = requestPacket.getBytes();

        if (bytes == null || bytes.length == 0) {
            requestPacket.setError("There is no data to send.");
            return;
        }

        final Integer baudrate = requestPacket.getBaudrate();
        serialPortFlash.setExpectedLength(requestPacket.getExpectedLength());
        byte[] received = serialPortFlash.send(portName, timeout, bytes, baudrate);
        if (received == null)
            return;

        logger.error("{} : {}", received.length, received);

        requestPacket.setAnswer(received);
        logger.debug(requestPacket);
    }

    private void sendConsole(RequestPacket requestPacket) throws IrtSerialPortIOException {
        logger.traceEntry("{}", requestPacket);

        if (serialPortConsole == null)
            serialPortConsole = new JSerialCommConsole();

        final String portName = requestPacket.getSerialPort();
        final int timeout = requestPacket.getTimeout();
        final byte[] bytes = requestPacket.getBytes();

        if (bytes == null || bytes.length == 0) {
            requestPacket.setError("There is no data to send.");
            return;
        }

        final Integer baudrate = requestPacket.getBaudrate();
        serialPortConsole.setExpectedEnd(requestPacket.getAnswer());
        byte[] received = serialPortConsole.send(portName, timeout, bytes, baudrate);
        requestPacket.setAnswer(received);
        logger.debug(requestPacket);
    }

    private void sendNormal(RequestPacket requestPacket) throws Exception {

        logger.traceEntry("{}", requestPacket);

        final String portName = requestPacket.getSerialPort();
        final int timeout = Optional.ofNullable(requestPacket.getTimeout()).orElse(100);
        final byte[] bytes = requestPacket.getBytes();
        final Integer baudrate = requestPacket.getBaudrate();

        if (bytes == null || bytes.length == 0) {
            requestPacket.setError("There is no data to send.");
            return;
        }

        final byte flagSequence =
                (byte) (requestPacket.getId() == ConsoleRestController.PACKET_ID
                        ? '\n'
                        : Packet.FLAG_SEQUENCE);

        boolean retried = false;

        while (true) {

            byte[] received = serialPort.send(
                    portName,
                    timeout,
                    bytes,
                    baudrate
            );

            if (received == null)
                return;

            received = read(
                    received,
                    portName,
                    timeout,
                    baudrate,
                    flagSequence
            );

            Packet packet = new Packet(
                    received,
                    requestPacket.getUnitAddr() == 0
            );

            final int receivedId = packet.getPacketId();
            final int expectedId = requestPacket.getId();

            /*
             * Preserve previous behavior:
             *
             * First unexpected packet:
             *   ACK it
             *   retry the original request
             *
             * Second unexpected packet:
             *   stop retrying and accept it as before.
             */
            if (!retried && receivedId != expectedId) {

                retried = true;

                logger.debug(
                        "Unexpected packet received. Expected={}, received={}. Retrying request.",
                        expectedId,
                        receivedId
                );

                sendAcknowledgement(
                        packet,
                        portName,
                        baudrate
                );

                continue;
            }

            final int lastIndex = packet.getLastIndex() + 1;

            received = read(
                    Arrays.copyOfRange(
                            received,
                            lastIndex,
                            received.length
                    ),
                    portName,
                    timeout,
                    baudrate,
                    flagSequence
            );

            requestPacket.setAnswer(received);

            logger.debug(requestPacket);

            sendAcknowledgement(
                    packet,
                    portName,
                    baudrate
            );

            return;
        }
    }
 
    private void sendAcknowledgement(Packet packet, final String portName, final Integer baudrate) {
        Optional.ofNullable(packet.getAcknowledgement())
                .filter(a -> a.length > 0)
                .ifPresent(a -> {
                    try {
                        serialPort.send(portName, null, a, baudrate);
                    } catch (IrtSerialPortIOException e) {
                        throw new irt.gui.web.exceptions.IrtSerialPortRTException(e.getLocalizedMessage(), e);
                    }
                });
    }

    byte[] read(
            byte[] initial,
            String portName,
            Integer timeout,
            Integer baudrate,
            byte flagSequence
    ) throws IrtSerialPortIOException, InterruptedException {

        byte[] buffer = initial;

        final int maxIterations = 200;
        int iterations = 0;

        while (iterations++ < maxIterations) {

            if (flagSequence == Packet.FLAG_SEQUENCE) {

                int count = 0;

                for (byte b : buffer) {
                    if (b == flagSequence && ++count >= 2)
                        return buffer;
                }

            } else {

                for (byte b : buffer) {
                    if (b == flagSequence)
                        return buffer;
                }
            }

            byte[] more = serialPort.read(
                    portName,
                    timeout,
                    baudrate
            );

            if (more == null || more.length == 0)
                return buffer;

            byte[] combined = Arrays.copyOf(
                    buffer,
                    buffer.length + more.length
            );

            System.arraycopy(
                    more,
                    0,
                    combined,
                    buffer.length,
                    more.length
            );

            buffer = combined;

            TimeUnit.MILLISECONDS.sleep(10);
        }

        return buffer;
    }
}
