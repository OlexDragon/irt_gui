package irt.gui.controllers.calibration.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.controllers.LinkedPacketSender;
import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.controllers.calibration.tools.Tool;
import irt.gui.controllers.calibration.tools.Tool.Commands;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.packet.interfaces.PacketToSend;
import irt.gui.data.packet.observable.calibration.GetPowerMeterPacket;
import irt.gui.data.packet.observable.calibration.ToolsComandsPacket;
import irt.gui.data.packet.observable.calibration.ToolsPacket;
import irt.gui.data.packet.observable.calibration.prologix.PAddrPacket;
import irt.gui.data.packet.observable.calibration.prologix.PReadPacket;
import irt.gui.data.packet.observable.calibration.prologix.PrologixPacket;

public class AverageDoubleTest {

	private final Logger logger = LogManager.getLogger();
	private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new MyThreadFactory());
	private final ToolsPacket getPacket = new GetPowerMeterPacket();
	private Object address;
	private final PrologixPacket packetAddr = new PAddrPacket();
	private final PrologixPacket packetRead = new PReadPacket();
	private final LinkedPacketsQueue queue = new LinkedPacketsQueue();
	private final LinkedPacketSender serialPort = new LinkedPacketSender("COM7");
//	private final Observer observer = (o,arg)->{
//												ToolsPacket tp = (ToolsPacket)o;
//												String answer = new String(tp.getAnswer());
//												double d = Double.parseDouble(answer);
//												logger.trace(d);
//	};
//	private Future<Double> future;

	public AverageDoubleTest() {
		queue.setComPort(serialPort);
	}


	@Test
	public void powerMeterTest() throws InterruptedException, ExecutionException {

		Tool tools = new Tool() {
			
			@Override public void set(Commands command, Object valueToSend, Observer observer) { }
			
			@Override public void get(Commands command, Observer observer) {
				getPacket.addObserver(observer);
				send("13", getPacket);
			}

			@Override
			public <T> Future<T> get(Commands command) {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Auto-generated method stub");
			}

			@Override
			public void set(Commands command, Object valueToSend) {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Auto-generated method stub");
			}
		};

		try {
			serialPort.openPort();
//			future =
					EXECUTOR.submit(new AverageDouble(tools, Commands.GET));

		} catch (Exception e) {
			logger.catching(e);
		}
	}


	//Method copied from PanelPrologix
	public void send(String addr, PacketToSend packet) {
		logger.entry(addr, packet);

		List<PacketToSend> ps = new ArrayList<>();

		if(!addr.equals(address)){
//			address = addr;
			packetAddr.getCommand().setValue(addr);
			ps.add(packetAddr);
		}

		ps.add(packet);

		try {
			final ToolsComandsPacket p = new ToolsComandsPacket(ps);

			Optional
			.of(packet.getObservers())
			.filter(os->os.length>0)
			.ifPresent(os->{
				ps.add(packetRead);
				p.addObserver((o, arg)->packet.setAnswer(((PacketToSend)o).getAnswer()));
			});

			queue.add(p, false);
		} catch (Exception e) {
			logger.catching(e);
		}
	}
}
