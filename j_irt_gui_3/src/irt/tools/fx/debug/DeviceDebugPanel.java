package irt.tools.fx.debug;

import java.awt.event.HierarchyEvent;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiController;
import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.ThreadWorker;
import irt.data.RegisterValue;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketIDs;
import irt.data.packet.denice_debag.DeviceDebugPacket;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.value.Value;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;

public class DeviceDebugPanel extends JFXPanel {
	private final static long serialVersionUID = -5736281933687114379L;
	private final static Logger logger = LogManager.getLogger();

	private final Preferences pref = GuiController.getPrefs();

	private final Map<CheckBox, ScheduledFuture<?>> mapScheduledFuture = new HashMap<CheckBox, ScheduledFuture<?>>();
	private ScheduledExecutorService service;
	private final byte linkAddr;

	private DebugPanelFx root;

	public DeviceDebugPanel(byte linkAddr) {

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->root.stop()));

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				Platform.runLater(()->root.start());
			}
			public void ancestorRemoved(AncestorEvent event) {
				Platform.runLater(()->root.stop());
			}
			public void ancestorMoved(AncestorEvent event) { }
		});

		this.linkAddr = linkAddr;

		Platform.runLater(()->{
			root = new DebugPanelFx();
			Scene scene = new Scene(root);
			setScene(scene);
		});
	}


	public class DebugPanelFx extends AnchorPane implements PacketListener{

		public DebugPanelFx() {

			Thread currentThread = Thread.currentThread();
			currentThread.setName(getClass().getSimpleName() + "-" + currentThread.getId());

			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DebugPanel.fxml"));
	        fxmlLoader.setRoot(this);
	        fxmlLoader.setController(this);

	        try {
	            fxmlLoader.load();
	        } catch (IOException exception) {
	            throw new RuntimeException(exception);
	        }
		}

		@FXML private CheckBox cb1;
		@FXML private CheckBox cb2;
		@FXML private CheckBox cb3;
		@FXML private CheckBox cb4;

		@FXML private TextField tfIndex1;
		@FXML private TextField tfIndex2;
		@FXML private TextField tfIndex3;
		@FXML private TextField tfIndex4;

		@FXML private TextField tfAddr1;
		@FXML private TextField tfAddr2;
		@FXML private TextField tfAddr3;
		@FXML private TextField tfAddr4;

		@FXML private TextField tfValue1;
		@FXML private TextField tfValue2;
		@FXML private TextField tfValue3;
		@FXML private TextField tfValue4;

		@FXML private Button btnSet1;
		@FXML private Button btnSet2;
		@FXML private Button btnSet3;
		@FXML private Button btnSet4;

	    @FXML private ToggleButton btnCalMode;


	    @FXML private CheckBox inHex;

		@FXML protected void initialize() {

			tfIndex1.textProperty().addListener(textListener);
			tfIndex2.textProperty().addListener(textListener);
			tfIndex3.textProperty().addListener(textListener);
			tfIndex4.textProperty().addListener(textListener);
			Optional.ofNullable(pref.get("tfIndex1", null)).ifPresent(text->Platform.runLater(()->tfIndex1.setText(text)));
			Optional.ofNullable(pref.get("tfIndex2", null)).ifPresent(text->Platform.runLater(()->tfIndex2.setText(text)));
			Optional.ofNullable(pref.get("tfIndex3", null)).ifPresent(text->Platform.runLater(()->tfIndex3.setText(text)));
			Optional.ofNullable(pref.get("tfIndex4", null)).ifPresent(text->Platform.runLater(()->tfIndex4.setText(text)));

			tfAddr1.textProperty().addListener(textListener);
			tfAddr2.textProperty().addListener(textListener);
			tfAddr3.textProperty().addListener(textListener);
			tfAddr4.textProperty().addListener(textListener);
			Optional.ofNullable(pref.get("tfAddr1", null)).ifPresent(text->Platform.runLater(()->tfAddr1.setText(text)));
			Optional.ofNullable(pref.get("tfAddr2", null)).ifPresent(text->Platform.runLater(()->tfAddr2.setText(text)));
			Optional.ofNullable(pref.get("tfAddr3", null)).ifPresent(text->Platform.runLater(()->tfAddr3.setText(text)));
			Optional.ofNullable(pref.get("tfAddr4", null)).ifPresent(text->Platform.runLater(()->tfAddr4.setText(text)));

//			tfValue1.textProperty().addListener(textListener);
//			tfValue2.textProperty().addListener(textListener);
//			tfValue3.textProperty().addListener(textListener);
//			tfValue4.textProperty().addListener(textListener);

			cb1.selectedProperty().addListener(cbChangeListener);
			cb2.selectedProperty().addListener(cbChangeListener);
			cb3.selectedProperty().addListener(cbChangeListener);
			cb4.selectedProperty().addListener(cbChangeListener);

			inHex.setSelected(pref.getBoolean(inHex.getId(), false));
		}

		@FXML void onGet(ActionEvent e) {
			Node source = (Node) e.getSource();
			get(source);			
		}

		@FXML void onSet(ActionEvent ae) {

			Node source = (Node) ae.getSource();

			try {

				set(source);

			}catch(NumberFormatException e) {

				Alert alert = new Alert(AlertType.ERROR);
				alert.initModality(Modality.APPLICATION_MODAL);
				alert.setTitle("Number Format Exception");
				alert.setHeaderText("The entered value contains one error.");
				alert.setContentText("Try entering a new value.");

				alert.showAndWait();
			}
		}

	    @FXML  void onMenuSelect(ActionEvent e) {

	    	Optional
	    	.of((ContextMenu) e.getSource())
	    	.map(ContextMenu::getId)
	    	.map(id->id.replaceAll("\\D", ""))
	    	.filter(idIndex->!idIndex.isEmpty())
	    	.ifPresent(
	    			idIndex->
	    			Optional
	    			.ofNullable(lookup("#tfIndex" + idIndex))
	    			.map(TextField.class::cast)
	    			.ifPresent(
	    					tfIndex->
	    					Optional
	    					.ofNullable(lookup("#tfAddr" + idIndex))
	    					.map(TextField.class::cast)
	    					.ifPresent(tfAddr->{

	    				    	MenuItem target = (MenuItem) e.getTarget();

	    				    	//get index and address
	    				    	String text = target.getText();
	    				    	List<String> collect = Optional

	    				    			.ofNullable(text.indexOf("index"))
	    				    			.filter(index->index>=0)
	    				    			.map(index->text.substring(index))
	    				    			.map(t->t.split("\\D"))
	    				    			.map(Arrays::stream)
	    				    			.orElse(Stream.empty())
	    				    			.filter(t->!t.isEmpty())
	    				    			.collect(Collectors.toList());

	    				    	if(collect.size()>1)
	    				    		Platform.runLater(()->{
	    				    			tfIndex.setText(collect.get(0));
	    				    			tfAddr.setText(collect.get(1));
	    				    		});
	    					})));
	    }

	    private String idIndex;
	    @FXML void onKeyPressed(KeyEvent e) {
	    	if(idIndex!=null || e.getCode()!=KeyCode.SHIFT)
	    		return;

	    	TextField source = (TextField) e.getSource();
	    	idIndex = source.getId().replaceAll("\\D", "");
	    	Platform.runLater(()->source.setText("" + step));
	    }

	    private int step = 10;
	    @FXML void onKeyReleased(KeyEvent e) {

    		TextField textField = (TextField)e.getSource();

	    	if(e.getCode().equals(KeyCode.SHIFT)) {

	    		idIndex = null;
	    		get(textField);
	    		return;
	    	}

	    	if(!(e.getCode().equals(KeyCode.UP) || e.getCode().equals(KeyCode.DOWN)))
	    		return;

	    	if(idIndex==null) {

	    		Optional<Integer> oValue = Optional

	    				.of(textField.getText())
	    				.filter(t->!t.isEmpty())
	    				.map(Integer::parseInt);

	    		Optional<Integer> oNewValue;
				if(e.getCode().equals(KeyCode.UP))

					oNewValue = oValue
	    			.map(v->v + step);
	    		else
					oNewValue = oValue
	    			.map(v->v - step)
	    			.map(v->v<0 ? 0 : v);
					
				oNewValue
				.ifPresent(
						newValue->
						Platform.runLater(
								()->{
									textField.setText("" + newValue);
									set(textField);
								}));
	    	}else {
	    		step = e.getCode().equals(KeyCode.UP) ? step*10 : step/10;
	    		if(step<1) step = 100;
	    		if(step>100) step = 1;
				Platform.runLater(()->textField.setText("" + step));
	    	}
	    }

	    @FXML void onHex(ActionEvent event) {

	    	final boolean selected = inHex.isSelected();
			pref.putBoolean(inHex.getId(), selected);

			try {

				lookupAll("TextField").parallelStream().filter(tf->tf.getId().startsWith("tfValue")).map(TextField.class::cast).filter(tf->!tf.getText().isEmpty()).forEach(tf->setText(tf, Integer.parseInt(tf.getText(), selected ? 10 : 16)));

			}catch(NumberFormatException e) {

				Alert alert = new Alert(AlertType.ERROR);
				alert.initModality(Modality.APPLICATION_MODAL);
				alert.setTitle("Number Format Exception");
				alert.setHeaderText("The entered value contains one error.");
				alert.setContentText(e.getCause().getMessage());

				alert.showAndWait();
			}
	    }

	    @FXML void onCalOn() {

	    }

	    @FXML void onInitialize() {

	    }

		private void get(Node source) {
			createGetPacket(source).ifPresent(p->GuiControllerAbstract.getComPortThreadQueue().add(p));
		}

		private void set(Node node) {
			createSetPacket(node).ifPresent(p->GuiControllerAbstract.getComPortThreadQueue().add(p));
		}

		@Override
		public void onPacketReceived(Packet packet) {

			Optional<Packet> oPacket = Optional.ofNullable(packet);
			Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);

			if(!oHeader.map(PacketHeader::getPacketId).filter(PacketIDs.DEVICE_DEBUG_PACKET::match).isPresent())
				return;

			if(oPacket
					.filter(LinkedPacket.class::isInstance)
					.map(LinkedPacket.class::cast)
					.map(LinkedPacket::getLinkHeader)
					.map(LinkHeader::getAddr)
					.orElse((byte)0) != linkAddr)
				return;

			logger.traceEntry("{}", packet);

			if(oHeader
					.map(PacketHeader::getOption)
					.filter(o->o!=PacketImp.ERROR_NO_ERROR)
					.isPresent()) {

				logger.warn(packet);
				return;
			}

			PacketIDs
			.DEVICE_DEBUG_PACKET
			.valueOf(packet)
			.map(RegisterValue.class::cast)
			.ifPresent(
					registerValue->{

						setValue(registerValue, tfIndex1, tfAddr1, tfValue1);
						setValue(registerValue, tfIndex2, tfAddr2, tfValue2);
						setValue(registerValue, tfIndex3, tfAddr3, tfValue3);
						setValue(registerValue, tfIndex4, tfAddr4, tfValue4);
					});
		}

		private void setValue(RegisterValue registerValue, TextField tfIndex, TextField tfAddr, TextField tfValue) {

			if(Optional.ofNullable(idIndex).filter(index->index.equals(tfIndex.getId().replaceAll("\\D", ""))).isPresent())
				return;

			getValue(tfIndex, 10).filter(v->v==registerValue.getIndex())
			.flatMap(v->getValue(tfAddr, 10)).filter(v->v==registerValue.getAddr())
			.map(v->registerValue.getValue())
			.map(Value::getValue)
			.map(Long::intValue)
			.ifPresent(v->setText(tfValue, v));
		}

		private void setText(TextField tfValue, int value) {

			final String text = Integer.toString(value, inHex.isSelected() ? 16 : 10);
			Platform.runLater(()->tfValue.setText(text));
		}

		// ************************************************************************ //
		//																			//
		//								Text Listener								//
		//																			//
		// ************************************************************************ //
		private ChangeListener<? super String> textListener = (o,oV,nV)->{

			TextField textField = (TextField)((StringProperty)o).getBean();

			//remove non digits
			if (!nV.matches("\\d*")) {
	            String tmp = nV.replaceAll("\\D", "");
				textField.setText(tmp);
	            if(tmp.equals(oV))
	            	return;
	        }

			// Remember setting
			String id = textField.getId();
			pref.put(id, nV);

			// Enable buttons
			String idIndex = id.replaceAll("\\D", "");
			boolean indexPresent = getValue(lookup("#tfIndex" + idIndex), 10).isPresent();
			boolean addrPresent = getValue(lookup("#tfAddr" + idIndex), 10).isPresent();
			boolean valuePresent = getValue(lookup("#tfValue" + idIndex), inHex.isSelected() ? 16 : 10).isPresent();

			final boolean disableCheckBox = !(indexPresent && addrPresent);

			Optional
			.ofNullable(lookup("#cb" + idIndex))
			.map(CheckBox.class::cast)
			.ifPresent(
					cb->
					Platform.runLater(
							()->{

								cb.setDisable(disableCheckBox);

								// if index or address changed stop auto get
								if(id.startsWith("tfIndex") || id.startsWith("tfAddr"))
									cb.setSelected(false);
							}));

			final boolean disableSetButton = disableCheckBox || !valuePresent;

			Optional
			.ofNullable(lookup("#btnSet" + idIndex))
			.ifPresent(node->Platform.runLater(()->node.setDisable(disableSetButton)));
		};

		// ************************************************************************ //
		//																			//
		//							Boolean Listener								//
		//																			//
		// ************************************************************************ //
		private ChangeListener<Boolean> cbChangeListener = (o,oV,nV)->{

			CheckBox checkBox = (CheckBox)((BooleanProperty)o).getBean();
			stopFuture(checkBox);
			if(nV) {
				startFuture(checkBox);
			}
		};

		private ScheduledFuture<?> screateFuture(DeviceDebugPacket p) {
			logger.trace(p);
			final ComPortThreadQueue queue = GuiControllerAbstract.getComPortThreadQueue();
			return service.scheduleAtFixedRate(()->queue.add(p), 0, 3, TimeUnit.SECONDS);
		}

		private Optional<DeviceDebugPacket> createGetPacket(Node node) {
			return getIndexAndAddress(node)
					.map(ib->new DeviceDebugPacket(linkAddr, ib.get(), ib.get(), null, PacketIDs.DEVICE_DEBUG_PACKET));
		}

		private Optional<DeviceDebugPacket> createSetPacket(Node node) {
			return getIndexAndAddressValue(node)
					.map(ib->new DeviceDebugPacket(linkAddr, ib.get(), ib.get(), ib.get(), PacketIDs.DEVICE_DEBUG_PACKET));
		}

		/**
		 * @param node
		 * @return  Optional<IntBuffer> where first int is index second address
		 */
		private Optional<IntBuffer> getIndexAndAddress(Node node) {

			String idIndex = node.getId().replaceAll("\\D", "");

			return getValue(lookup("#tfIndex" + idIndex), 10)
					.flatMap(
							index->
							getValue(lookup("#tfAddr" + idIndex), 10)
							.map(addr->(IntBuffer)IntBuffer
									.allocate(2)
									.put(index)
									.put(addr)
									.position(0)));
		}

		/**
		 * @param node
		 * @return  Optional<IntBuffer> where first int is index second is address and third is value
		 */
		private Optional<IntBuffer> getIndexAndAddressValue(Node node) {

			String idIndex = node.getId().replaceAll("\\D", "");

			return getValue(lookup("#tfIndex" + idIndex), 10)
					.flatMap(
							index->
							getValue(lookup("#tfAddr" + idIndex), 10)
							.flatMap(
									addr->
									getValue(lookup("#tfValue" + idIndex), inHex.isSelected() ? 16 : 10)
									.map(
											val->(IntBuffer)IntBuffer
											.allocate(3)
											.put(index)
											.put(addr)
											.put(val)
											.position(0))))
							;
		}

		private Optional<Integer> getValue(Node node, int radix){
			return Optional
					.ofNullable(node)
					.map(TextField.class::cast)
					.map(TextField::getText)
					.filter(t->!t.isEmpty())
					.map(text->Integer.parseInt(text, radix));
		}

		private void startFuture(CheckBox checkBox) {
			createGetPacket(checkBox)
			.ifPresent(p->mapScheduledFuture.put(checkBox, screateFuture(p)));
		}

		private void stopFuture(CheckBox checkBox) {
			Optional.ofNullable(mapScheduledFuture.get(checkBox)).filter(futur->!futur.isDone()).ifPresent(future->future.cancel(true));
		}

		public void start(){

			if(!Optional.ofNullable(service).filter(sfr->!sfr.isShutdown()).isPresent()) {
				service = Executors.newScheduledThreadPool(4, new ThreadWorker("DebugPanelFx.service"));
				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
			}

			mapScheduledFuture
			.entrySet()
			.stream()
			.filter(

					entry->!
					Optional
					.ofNullable(entry.getValue())
					.filter(v->!v.isDone())
					.isPresent())

			.forEach(

					entry->{
						createGetPacket(entry.getKey())
						.ifPresent(p->entry.setValue(screateFuture(p)));
					});
					
		}

		public void stop(){

			GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);

			mapScheduledFuture
			.entrySet()
			.stream()
			.map(
					entry->
					Optional
					.ofNullable(entry.getValue())
					.filter(v->!v.isDone()))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.forEach(future->future.cancel(true));

			Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
		}
	}
}
