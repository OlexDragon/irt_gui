package irt.controller.control;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.data.MyThreadFactory;
import irt.data.packet.AttenuationPacket;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.interfaces.LinkedPacket;

public class UnitAttenuationController implements UnitController{

	private final Logger logger = LogManager.getLogger();

	private ScheduledFuture<?> scheduledFuture;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private JTextField txtGain;
	private JSlider slider;
	private JTextField txtStep;

	private AttenuationPacket packet;

	private ChangeListener sliderChange = e->{logger.error("sliderChange");};

	public UnitAttenuationController(Byte linkAddr, JTextField txtGain, JSlider slider, JTextField txtStep) {
		this.txtGain = txtGain;
		this.slider = slider;
		this.txtStep = txtStep;

		packet = new AttenuationPacket(linkAddr, null);
	}

	@Override
	public void run() {
		GuiControllerAbstract.getComPortThreadQueue().add(packet);
	}

	@Override
	public void start() {
		logger.error("start()");

		if(Optional.ofNullable(scheduledFuture).map(sch->!sch.isCancelled()).orElse(false))
			return;

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		scheduledFuture = service.scheduleAtFixedRate(this, 1, 5, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {
		logger.error("stop()");

		if(Optional.ofNullable(scheduledFuture).map(sch->sch.isCancelled()).orElse(true))
			return;

		scheduledFuture.cancel(true);
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
	}

	@Override
	public void onPacketRecived(Packet packet) {
		Optional
		.of(packet)
		.filter(comparePacketID())
		.filter(compareUnitAddress())
		.ifPresent(updateFields());
	}

	private Consumer<? super Packet> updateFields() {
		return p->{
			Optional
			.ofNullable(p.getPayloads())
			.flatMap(pls->pls.stream().findAny())
			.map(pl->pl.getShort(0))
			.map(setSliderValue())
			.ifPresent(logger::error);
		};
	}

	private Function<? super Short, ? extends Double> setSliderValue() {
		return sh->{

			slider.removeChangeListener(sliderChange);
			slider.setValue(sh);
			slider.addChangeListener(sliderChange);

			return sh/10.0;
		};
	}

	private Predicate<? super Packet> comparePacketID() {
		return p->{

			final PacketHeader packetHeader = packet.getHeader();
			final short packetId = packetHeader.getPacketId();

			return p.getHeader().getPacketId()==packetId;
		};
	}

	private Predicate<? super Packet> compareUnitAddress() {
		return p->{

			final LinkHeader linkHeader = packet.getLinkHeader();
			final byte addr = linkHeader.getAddr();

			if(p instanceof LinkedPacket)
				return ((LinkedPacket)p).getLinkHeader().getAddr()==addr;

			return addr==0;
		};
	}
}
