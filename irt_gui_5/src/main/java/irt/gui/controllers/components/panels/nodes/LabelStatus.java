
package irt.gui.controllers.components;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.controllers.UpdateController;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.ParameterHeaderCode;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.alarms.AlarmStatusPacket.AlarmSeverities;
import irt.gui.data.packet.observable.measurement.StatusPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;

public class LabelStatus extends FieldsControllerAbstract implements Initializable{



	public static final int REFERENCE_10MHZ = 1100;
	private final String LOCKED = AlarmSeverities.NO_ALARM.getStyleClass();
	private final String NOTLOCKED = AlarmSeverities.CRITICAL.getStyleClass();

	@FXML private Label inputOwerdriveLabel;
	@FXML private Label muteLabel;
	@FXML private Label lockLabel;
    @FXML private Label pll1Label;
    @FXML private Label pll2Label;
    @FXML private Label pll3Label;
    @FXML private Label aopcLabel;
    @FXML private Label lnbPowerLabel;

	private ResourceBundle 		bundle;

	private StatusPacket statusPacket;
	private Integer status;
	private ObservableList<Node> children;

	public LabelStatus() {

		try {

			statusPacket = new StatusPacket();

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
	}

	@Override public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;

		final Pane parent = (Pane) inputOwerdriveLabel.getParent();
		children = parent.getChildren();

		setLokDetails(false, null);
		setAopc(false, null);
		setLnbPowerEnable(false, null);

		addPacketToSend(statusPacket);
		doUpdate(true);

		UpdateController.addController(this);
	}

    @FXML  void onCopyStatisBytes(ActionEvent event) {

		final String text = muteLabel.getTooltip().getText();

		final ClipboardContent cc = new ClipboardContent();
		cc.putString(text);
		Clipboard.getSystemClipboard().setContent(cc) ;
    } 

	@Override
	protected void updateFields(LinkedPacket packet) throws Exception {

		if(packet.getPacketHeader().getPacketError()==PacketErrors.NO_ERROR){

			StatusPacket p = new StatusPacket(packet.getAnswer(), true);

			final boolean isFcm = p.getLinkHeader().getAddr()==0;

			Optional
			.ofNullable(p.getPayloads())
			.map(List::stream)
			.orElse(Stream.empty())
			.findAny()
			.ifPresent(pl->{

				final Integer status = pl.getInt(0);

				if(status.equals(this.status))
					return;

				this.status = status;
				Platform.runLater(()->{
					muteLabel.setTooltip(new Tooltip(ToHex.bytesToHex(Packet.toBytes(status))));
				});


				StatusByte sm = FcmStatusBits.MUTE;
				if(isFcm){
					setFcmLockStatus(status);

				}else{
					setBucLockStatus(status);
				}

				setLokDetails(isFcm, status);
				setMuteStatus(sm.isOn(status));
				setAopc(isFcm, status);
				setLnbPowerEnable(isFcm, status);
			});
		}
	}

	private void setLokDetails(boolean isFcm, Integer status) {

		final Integer deviceType = InfoController.getDeviceType();
		if(deviceType!=null && deviceType==REFERENCE_10MHZ){
			String text;

			if(ComboBox10MHzReferenceSource.Ref10MHzStatusBits.AUTOSENSE.isOn(status))
				text = ComboBox10MHzReferenceSource.Ref10MHzStatusBits.AUTOSENSE.name();

			else if(ComboBox10MHzReferenceSource.Ref10MHzStatusBits.EXTERNAL.isOn(status))
				text = ComboBox10MHzReferenceSource.Ref10MHzStatusBits.EXTERNAL.name();

			else if(ComboBox10MHzReferenceSource.Ref10MHzStatusBits.INTERNAL.isOn(status))
				text = ComboBox10MHzReferenceSource.Ref10MHzStatusBits.INTERNAL.name();

			else
				text = ComboBox10MHzReferenceSource.Ref10MHzStatusBits.UNDEFINED.name();

			Platform.runLater(()->pll1Label.setText(text));
			addLabel( pll1Label, FcmStatusBits.LOCK1.isOn(status));
		}else if(isFcm){
			addLabel( pll1Label, FcmStatusBits.LOCK1.isOn(status));
			addLabel( pll2Label, FcmStatusBits.LOCK2.isOn(status));
			addLabel( pll3Label, FcmStatusBits.LOCK3.isOn(status));
			addLabel( inputOwerdriveLabel, FcmStatusBits.INPUT_OWERDRIVE.isOn(status));
		}else{
			children.remove(inputOwerdriveLabel);
			children.remove(pll1Label);
			children.remove(pll2Label);
			children.remove(pll3Label);
		}
	}

	private void addLabel(final Label label, final boolean isOn) {

		Platform.runLater(()->{
			if(isOn){

				if(children.contains(label))
					return;

				children.add(label);
			}else
				children.remove(label);
		});
	}

	private void setMuteStatus(boolean isOn) {

		final String muted = AlarmSeverities.INFO.getStyleClass();
		final String unmuted = AlarmSeverities.NO_ALARM.getStyleClass();

		if(isOn){
			Platform.runLater(()->{

				final String value = bundle.getString("mute.muted");
				if(muteLabel.getText().equals(value))
					return;

				muteLabel.setText(value);
			});
			setStyleClass(muteLabel, muted, unmuted);

		}else{
			Platform.runLater(()->{

				final String value = bundle.getString("mute.unmuted");
				if(muteLabel.getText().equals(value))
					return;
				
				muteLabel.setText(value);
			});
			setStyleClass(muteLabel, unmuted, muted);
		}
	}

	private void setBucLockStatus(Integer status) {

		if(BucStatusBits.UNKNOWN.isOn(status))
			Platform.runLater(()->children.remove(lockLabel));

		else{

			if(!children.contains(lockLabel))
				Platform.runLater(()->children.add(lockLabel));

			if(BucStatusBits.LOCK.isOn(status)){
				String value = bundle.getString("lock.locked");
				Platform.runLater(()->{
					if(lockLabel.getText().equals(value))
						return;

					lockLabel.setText(value);
				});
				setStyleClass(lockLabel, LOCKED, NOTLOCKED);

			}else{
				String value = bundle.getString("lock.unlocked");
				Platform.runLater(()->{
					if(lockLabel.getText().equals(value))
						return;

					lockLabel.setText(value);
				});
				setStyleClass(lockLabel, NOTLOCKED, LOCKED);
			}
		}
	}

	private void setFcmLockStatus(Integer status) {
//		logger.error(status);

		final Integer deviceType = InfoController.getDeviceType();
		boolean on;

		if(deviceType!=null && deviceType==REFERENCE_10MHZ)
			on = ComboBox10MHzReferenceSource.Ref10MHzStatusBits.LOCK_SUMMARY.isOn(status);
		else
			on = FcmStatusBits.LOCK.isOn(status);

		if(on){
			String value = bundle.getString("lock.locked");
			Platform.runLater(()->{
				if(lockLabel.getText().equals(value))
					return;

				lockLabel.setText(value);
			});
			setStyleClass(lockLabel, LOCKED, NOTLOCKED);

		}else{
			String value = bundle.getString("lock.unlocked");
			Platform.runLater(()->{
				if(lockLabel.getText().equals(value))
					return;

				lockLabel.setText(value);
			});
			setStyleClass(lockLabel, NOTLOCKED, LOCKED);
		}
	}

	private void setAopc(boolean isFcm, Integer status) {

		if(isFcm){

			final FcmAopcStatusBits valueOf = FcmAopcStatusBits.valueOf(status);
			if(valueOf==FcmAopcStatusBits.NONE)
				children.remove(aopcLabel);

			else{
				if(!children.contains(aopcLabel))
					Platform.runLater(()->children.add(aopcLabel));

				final String text = "AOPC:"+valueOf.toString();
				Platform.runLater(()->{

					if(aopcLabel.getText().equals(text))
						return;

					aopcLabel.setText(text);
					aopcLabel.setTooltip(new Tooltip(text));
				});
			}
		}else
			children.remove(aopcLabel);
	}

	private void setLnbPowerEnable(boolean isFcm, Integer status) {

		if(isFcm){

			final FcmLnbPowerStatusBits valueOf = FcmLnbPowerStatusBits.valueOf(status);
			if(valueOf==FcmLnbPowerStatusBits.NONE)
				children.remove(lnbPowerLabel);

			else{
				if(!children.contains(lnbPowerLabel))
					Platform.runLater(()->children.add(lnbPowerLabel));

				final String text = "LNB Power:"+valueOf.toString();
				Platform.runLater(()->{

					if(lnbPowerLabel.getText().equals(text))
						return;

					lnbPowerLabel.setText(text);
					lnbPowerLabel.setTooltip(new Tooltip(text));
				});
			}
		}else
			children.remove(lnbPowerLabel);
	}

	private void setStyleClass(Label label, String styleClassToAdd, String styleClassToRemove) {
		Platform.runLater(()->{
			final ObservableList<String> styleClass = label.getStyleClass();
			styleClass.remove(styleClassToRemove);
			if(!styleClass.contains(styleClassToAdd))
				styleClass.add(styleClassToAdd);
		});
	}

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(3);
	}

	@Override
	public void doUpdate(boolean update) {
		super.doUpdate(update);
		muteLabel.getParent().setDisable(!update);
	}

	public interface StatusByte{
		boolean isOn(Integer status);
	}

	public enum BucStatusBits implements StatusByte{
		MUTE	(1, 1),		//0 - unmuted; 1 - muted 
		UNKNOWN	(0, 6),		//0,1 - unknown; 2,3 - lock; 4,5 - unlock;
		LOCK	(2, 6);

		private int value;
		private int mask;

		private BucStatusBits(int value, int mask){
			this.value = value;
			this.mask = mask;
		}

		@Override public boolean isOn(Integer status) {
			return (status & mask) == value;
		}
	}

	public enum FcmAopcStatusBits{
		NONE,
		OFF,
		NORMAL,
		LOW,
		HIGH,
		SUSPENDED,
		OVERDRIVE;

		public static final int MASK = 7;//3 bits
		public static final int SHIFT = 8;//[10:8]

		public static FcmAopcStatusBits valueOf(int status){
			int index = (status>>SHIFT) & MASK;
			return FcmAopcStatusBits.values()[index];
		}
	}

	public enum FcmLnbPowerStatusBits{
		NONE,
		UNDEFINED1,
		ENABLED,
		DISABLED,
		UNDEFINED4,
		UNDEFINED5,
		UNDEFINED6;

		public static final int MASK = 7;//3 bits
		public static final int SHIFT = 11;//[13:11]

		public static FcmLnbPowerStatusBits valueOf(int status){
			int index = (status>>SHIFT) & MASK;
			return FcmLnbPowerStatusBits.values()[index];
		}
	}

	public enum FcmStatusBits implements StatusByte{

		LOCK1			,	//FCM_STATUS_LOCK_DETECT_PLL1
		LOCK2			,	//FCM_STATUS_LOCK_DETECT_PLL2
		MUTE			,	//FCM_STATUS_MUTE
		MUTE_TTL		,	//FCM_STATUS_TTL_MUTE_CONTROL

		LOCK3			,	//FCM_STATUS_LOCK_DETECT_PLL3
		LOCK			,	//FCM_STATUS_LOCK_DETECT_SUMMARY
		INPUT_OWERDRIVE	,	//FCM_STATUS_INPUT_OVERDRIVE
		LOW_POWER;

		private int value;

		private FcmStatusBits(){
			value = 1<<ordinal();
		}

		@Override public boolean isOn(Integer status) {
			return (status & value) != 0;
		}
	}

	@Override
	public void run() {

		Optional.ofNullable(InfoController.getDeviceType()).filter(dt->dt==REFERENCE_10MHZ).ifPresent(dt->{
			
			statusPacket
			.getPayloads()
			.stream()
			.findAny()
			.ifPresent(pl->{
				pl.getParameterHeader().setParameterHeaderCode(ParameterHeaderCode.M_STATUS_10MHZ_REF);
			});
		});

		super.run();
	}
}
