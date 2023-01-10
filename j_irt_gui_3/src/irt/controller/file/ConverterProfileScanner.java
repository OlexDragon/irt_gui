package irt.controller.file;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import irt.controller.GuiControllerAbstract;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.DeviceDebugPacketIds;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketWork;
import irt.data.packet.PacketID;
import irt.data.packet.denice_debag.DeviceDebugPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.interfaces.StopInterface;

public class ConverterProfileScanner extends FutureTask<Optional<Path>> implements StopInterface {

	private static ConverterWorker CALLABLE;
	private static PacketWork packetWork;

	public ConverterProfileScanner(byte linkAddr) {
		super(CALLABLE = new ConverterWorker());
		packetWork = new DeviceDebugPacket(linkAddr, DeviceDebugPacketIds.DUMP_CONVERTER_INFO);
	}

	@Override
	public void stop() {
		CALLABLE.stop();
	}

	// ***************************************************************************************************************** //
	// 																													 //
	// 									 		class ConverterWorker													 //
	// 																													 //
	// ***************************************************************************************************************** //
	private static class ConverterWorker extends FutureTask<Optional<Path>>  implements Callable<Optional<Path>>, PacketListener{

		private static Optional<String> oFileName;
		private static ProfileScanner profileScanner;

		public ConverterWorker() {
			super(
					()->{
						profileScanner = new ProfileScanner(oFileName);
						FutureTask<Optional<Path>> ft = new FutureTask<>(profileScanner);
						new ThreadWorker(ft, "ConverterProfileScanner.ConverterWorker.Callable");
						return ft.get(10, TimeUnit.SECONDS);
					});
		}

		private static final String SERIAL_NUMBER = "Serial number: ";

		@Override
		public Optional<Path> call() throws Exception {
			Optional<Path> filePath = getFilePath();
			return filePath;
		}

		private Optional<Path> getFilePath() throws InterruptedException, ExecutionException, TimeoutException {
			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
			GuiControllerAbstract.getComPortThreadQueue().add(packetWork);
			return get(10, TimeUnit.SECONDS);
		}

		@Override
		public void onPacketReceived(Packet packet) {
			GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);

			Optional<Packet> oPacket = Optional.of(packet);
			Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);

			if(!oHeader.map(PacketHeader::getPacketId).filter(PacketID.DEVICE_DEBUG_CONVERTER_INFO_DUMP::match).isPresent())
				return;

			setFileName(oPacket
			.flatMap(PacketID.DEVICE_DEBUG_CONVERTER_INFO_DUMP::valueOf)
			.map(String.class::cast)
			.map(getConverterSerialNumber())
			.map(sn->sn+".bin"));
		}

		private void setFileName(Optional<String> oFileName) {
			ConverterWorker.oFileName = oFileName;
			ThreadWorker.runThread(this, "ConverterProfileScanner.ConverterWorker");
		}

		private Function<String, String> getConverterSerialNumber() {
			return text->{

				int indexOf = text.indexOf(SERIAL_NUMBER);
				String t;
				if(indexOf>=0)
					t = text.substring(indexOf + SERIAL_NUMBER.length());
				else 
					return null;

				indexOf = text.indexOf("\n");
				if(indexOf>0)
					t = t.substring(0, indexOf).trim();
				else
					return null;
				return t;
			};
		}

		public void stop() {
			GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
			Optional.ofNullable(profileScanner).ifPresent(ProfileScanner::stop);
		}
	}
}
