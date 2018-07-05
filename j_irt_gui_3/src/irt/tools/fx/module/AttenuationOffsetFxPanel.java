
package irt.tools.fx.module;

import java.awt.event.HierarchyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketSuper;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.MonitorPanelFx;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

public class AttenuationOffsetFxPanel extends JFXPanel implements PacketListener, Runnable{
	private static final long serialVersionUID = -2525638137111723616L;
	private final Logger logger = LogManager.getLogger();

	private ScheduledFuture<?> scheduleAtFixedRate;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private final List<PacketSuper> packets = new ArrayList<>();

	public AttenuationOffsetFxPanel(byte linkAddr) {

		Platform.runLater(()->{
			try{

				Parent root = new AttenuationOffsetFx();

				Scene scene = new Scene((Parent) root);
				setScene(scene);

			}catch (Exception e) {
		        logger.catching(e);
			}

		});

		addAncestorListener(new AncestorListener() {

			public void ancestorMoved(AncestorEvent arg0) {}
			public void ancestorAdded(AncestorEvent arg0) {

				final boolean present = Optional.ofNullable(scheduleAtFixedRate).filter(shr->!shr.isDone()).isPresent();
				if(!present)
					GuiControllerAbstract.getComPortThreadQueue().addPacketListener(AttenuationOffsetFxPanel.this);
					scheduleAtFixedRate = service.scheduleAtFixedRate(AttenuationOffsetFxPanel.this, 0, 10, TimeUnit.SECONDS);
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				GuiControllerAbstract.getComPortThreadQueue().removePacketListener(AttenuationOffsetFxPanel.this);
				Optional.ofNullable(scheduleAtFixedRate).filter(shr->!shr.isDone()).ifPresent(shr->shr.cancel(true));
			}
		});

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(
						c->{
							GuiControllerAbstract.getComPortThreadQueue().removePacketListener(AttenuationOffsetFxPanel.this);
							service.shutdownNow();
						}));
	}

	public class AttenuationOffsetFx extends AnchorPane{
		
		public AttenuationOffsetFx() {

			FXMLLoader fxmlLoader = new FXMLLoader(MonitorPanelFx.class.getResource("AttenuationOffset.fxml"));
	        fxmlLoader.setRoot(this);
	        fxmlLoader.setController(this);

	        try {
	            fxmlLoader.load();
	        } catch (IOException exception) {
	            throw new RuntimeException(exception);
	        }
		}
	}

	@Override
	public void run() {
		final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();
		packets.forEach(comPortThreadQueue::add);
	}

	@Override
	public void onPacketRecived(Packet packet) {
	}
}
