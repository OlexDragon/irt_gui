
package irt.tools.fx.module;

import java.awt.event.HierarchyEvent;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.ThreadWorker;
import irt.data.Range;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketIDs;
import irt.data.packet.configuration.Offset1to1toMultiPacket;
import irt.data.packet.configuration.OffsetRange;
import irt.data.packet.interfaces.Packet;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

public class AttenuationOffsetFxPanel extends JFXPanel{

	private static final long serialVersionUID = -2525638137111723616L;
	private final Logger logger = LogManager.getLogger();

	private ScheduledFuture<?> scheduleAtFixedRate;
	private ScheduledExecutorService service;

	private final List<PacketSuper> packets = new ArrayList<>();
	private AttenuationOffsetFx root;
	private byte linkAddr;

	private ComPortThreadQueue comPortThreadQueue;

	EventHandler<ActionEvent> eventHandler = e->sendCommand((TextField) e.getSource());

	private void sendCommand(TextField tf) {
		String t = tf.getText();

		if(t.isEmpty())
			return;

		byte i = ((Integer) tf.getUserData()).byteValue();
		double d = Double.parseDouble(t) * 10;
		short value = (short)d;
		Offset1to1toMultiPacket p = new Offset1to1toMultiPacket(linkAddr, i, value);
		comPortThreadQueue.add(p);
	}

	public AttenuationOffsetFxPanel(byte linkAddr, short[] array) {

		this.linkAddr = linkAddr;
		root = new AttenuationOffsetFx(array);
		packets.add(new OffsetRange(linkAddr));
		packets.add(new Offset1to1toMultiPacket(linkAddr, null, null));

		final String externalForm = getClass().getResource("AttenuationOffset.css").toExternalForm();

		Platform.runLater(
				()->{
					try{
						Scene scene = new Scene(root);
						scene.getStylesheets().add(externalForm);
						setScene(scene);

					}catch (Exception e) {
						logger.catching(e);
					}
				});

		addAncestorListener(new AncestorListener() {

			public void ancestorMoved(AncestorEvent arg0) {}
			public void ancestorAdded(AncestorEvent arg0) {

				if(Optional.ofNullable(scheduleAtFixedRate).filter(shr->!shr.isDone()).isPresent())
					return;

				if(!Optional.ofNullable(service).filter(s->!isShowing()).isPresent())
					service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("AttenuationOffsetFxPanel.service"));
				
				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(root);
				scheduleAtFixedRate = service.scheduleAtFixedRate(root, 1, 10, TimeUnit.SECONDS);
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				stop();
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
				.ifPresent(c->stop()));
	}

	//	********************************************************************************************** 	//
	//																									//
	//								class AttenuationOffsetFx											//
	//																									//
	//	********************************************************************************************** 	//
	public class AttenuationOffsetFx extends AnchorPane implements PacketListener, Runnable{
		
		private short[] array;

		public AttenuationOffsetFx(short[] array) {

			this.array = array;
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AttenuationOffset.fxml"));
	        fxmlLoader.setRoot(this);
	        fxmlLoader.setController(this);

	        try {
	            fxmlLoader.load();
	        } catch (IOException exception) {
	            throw new RuntimeException(exception);
	        }
		}

		@FXML private GridPane gridPane;

		private final NumberFormat nf = new DecimalFormat("0.0");
		private Range range;

		@FXML public void initialize() {

			comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();

			Optional.ofNullable(array).map(ShortBuffer::wrap).ifPresent(

					shortBuffer->

						IntStream.range(1, shortBuffer.get()+1).forEach(

								index->{

									final String text = nf.format(shortBuffer.get()/10.0);
									// Text formatter
									Pattern pattern = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");
									TextFormatter<String> formatter = new TextFormatter<>((UnaryOperator<TextFormatter.Change>) change->pattern.matcher(change.getControlNewText()).matches() ? change : null);

									TextField textField = new TextField(text);
									Platform.runLater(()->gridPane.add(textField, 1, index));
									textField.setUserData(index);
									textField.setTextFormatter(formatter);
									textField.setOnAction(eventHandler);

									RowConstraints rowConstraints = new RowConstraints();
									gridPane.getRowConstraints().add(rowConstraints);
									rowConstraints.setVgrow(Priority.ALWAYS);

									Text textTitle = new Text("Offet #" + index + ": ");
									Platform.runLater(()->gridPane.add(textTitle, 0, index));
									Button button = new Button("SET");
									button.setUserData(index);
									Platform.runLater(()->gridPane.add(button, 2, index));
									button.setOnAction(
											e->{
												Button source = (Button) e.getSource();
												gridPane
												.getChildren()
												.stream()
												.filter(TextField.class::isInstance)
												.map(TextField.class::cast)
												.filter(tf->tf.getUserData().equals(source.getUserData()))
												.findAny()
												.ifPresent(AttenuationOffsetFxPanel.this::sendCommand);
											});
								})
					);
		}

		@Override
		public void run() {
			packets.forEach(comPortThreadQueue::add);
		}

		@Override
		public void onPacketReceived(Packet packet) {
			Optional<Packet> oPacket = Optional.of(packet);
			Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);
			Optional<Short> oPacketId = oHeader.map(PacketHeader::getPacketId);

			// Range
			oPacketId
			.filter(PacketIDs.CONFIGURATION_OFFSET_RANGE::match)
			.flatMap(pId->PacketIDs.CONFIGURATION_OFFSET_RANGE.valueOf(packet))
			.map(Range.class::cast)
			.ifPresent(
					range->{

						if(this.range!=null)
							return;

						this.range = range;
						String min = nf.format(range.getMinimum()/10.0);
						String max = nf.format(range.getMaximum()/10.0);
						Text textNode = new Text("( min: " + min + " dB; max: " + max + " dB; }");
						Platform.runLater(()->gridPane.add(textNode, 1, 0));
					});

			oPacketId
			.filter(PacketIDs.CONFIGURATION_OFFSET_1_TO_MULTI::match)
			.flatMap(pId->PacketIDs.CONFIGURATION_OFFSET_1_TO_MULTI.valueOf(packet))
			.map(short[].class::cast)
			.ifPresent(
					array->
					gridPane
					.getChildren()
					.stream()
					.filter(TextField.class::isInstance)
					.map(TextField.class::cast)
					.filter(tf->((int)tf.getUserData())<array.length)
					.forEach(
							tf->{
								int userData = (int) tf.getUserData();
								final String text = nf.format(array[userData]/10.0);
								String t = tf.getText();
								if(!t.equals(text))
									Platform.runLater(()->tf.setText(text));}));
		}
	}

	private void stop() {
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(root);
		Optional.ofNullable(scheduleAtFixedRate).filter(shr->!shr.isDone()).ifPresent(shr->shr.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}
}
