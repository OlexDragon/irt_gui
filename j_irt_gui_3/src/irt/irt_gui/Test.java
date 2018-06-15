
package irt.irt_gui;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.JsscComPort;
import irt.controller.serial_port.PureJavaComPort;
import irt.controller.serial_port.SerialPortInterface;
import irt.data.MyThreadFactory;
import irt.data.ToHex;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.redundancy.SwitchoverModePacket;

public class Test {

	private final static Logger logger = LogManager.getLogger();

	public static void main(String[] args) {

		byteStuffingTest();

		SerialPortInterface port = null;
		try {

			logger.error("\n\n************************   START   ***********************************\n\n");

//			final byte linkAddr = (byte) 101;
			final byte linkAddr = (byte) 254;
//			port = new PureJavaComPort("COM13");
			port = new JsscComPort("COM13");
			port.openPort();
			logger.error("Serial port {} is opend={}", port, port.isOpened());
			final ComPortThreadQueue comPortThreadQueue = new ComPortThreadQueue();
			comPortThreadQueue.setSerialPort(port);
			final PacketListenerTest packetListener = new PacketListenerTest();
			comPortThreadQueue.addPacketListener(packetListener);

			PacketAbstract[] packets = new PacketAbstract[]{

//					new DeviceInfoPacket(linkAddr),
//					new MeasurementPacket(linkAddr),
//					new AlarmsSummaryPacket(linkAddr),
//					new AlarmsIDsPacket(linkAddr),
//					new DeviceDebugInfoPacket(linkAddr, (byte)1),
//					new ModuleListPacket(linkAddr),
//					new ActiveModulePacket(linkAddr, null),
					new SwitchoverModePacket(linkAddr, null)
				};

			for(int i=0, x=0;i<=2000; i++, x++){
				if(x>=packets.length)
					x=0;
				final long start = System.currentTimeMillis();

				packetListener.start();
				comPortThreadQueue.add(packets[x]);
				logger.error("Sent packet {}", packets[x]);

				final Packet packet = packetListener.get();

				logger.error("*******   {}) time = {} *******", i, System.currentTimeMillis()-start);
				if(packet==null || packet.getHeader().getPacketType()!=PacketImp.PACKET_TYPE_RESPONSE){
					logger.error(">>>>> ????? Did not get answer: {}\n\n\t\t\t got {} answers. \n\n************************   STOP   ***********************************\n\n", packet, i);
					Thread.sleep(2000);
					return;
				}

				logger.error("{} : {}\n{}", Optional.ofNullable(packet).map(Packet::getPayloads).map(List::stream).orElse(Stream.empty()).map(Payload::getBuffer).map(String::new).toArray(), packet, Optional.ofNullable(packet).map(Packet::toBytes).map(ToHex::bytesToHex).orElse(null));
			}
			logger.error("\n\n************************   STOP   ***********************************\n\n");

			Thread.sleep(2000);

		} catch (Exception e) {
			logger.catching(e);
		}finally{
			if(port!=null)
				port.closePort();
		}
	}

	private static void byteStuffingTest() {
		byte[] bytes = new byte[]{0, 00, 00, 01, 0x5C, (byte) 0xAF, (byte) 0xEA, (byte) 0x80, 00, 00, 00, 01, 0x7D, 0x5D, 0x78, 0x40, 00};
		logger.error("*** Start byteStuffingTest - {}" + ToHex.bytesToHex(bytes));
		logger.error("*** End byteStuffingTest - {}" + ToHex.bytesToHex(PureJavaComPort.byteStuffing(bytes)));
	}

	public static class PacketListenerTest implements PacketListener, Callable<Packet>{

		private Packet packet;
		private FutureTask<Packet> task;
		private boolean notified;

		public void start() {
			packet = null;
			notified = false;
			task = new FutureTask<>(this);
			new MyThreadFactory().newThread(task).start();
		}

		@Override
		public void onPacketRecived(Packet packet) {
			logger.error(packet);
			this.packet = packet;
			synchronized (this) {
				notified = true;
				notify();
			}
		}

		@Override
		public Packet call() throws Exception {
			synchronized (this) {

				final long start = System.currentTimeMillis();

				while(!notified && System.currentTimeMillis()-start<PureJavaComPort.MAX_WAIT_TIME)
					try{
						wait(PureJavaComPort.MAX_WAIT_TIME);
					}catch (Exception e) {
						logger.catching(e);
					}

				logger.error("time: {}; notivied: {}", System.currentTimeMillis()-start, notified);
			}

			return packet;
		}

		public Packet get() throws InterruptedException, ExecutionException {
			return task.get();
		}		
	}
}
