package irt.tools.panel.subpanel;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Optional;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import irt.controller.DefaultController;
import irt.controller.GuiControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.controller.serial_port.value.setter.DeviceDebagSetter;
import irt.data.DeviceInfo.DeviceType;
import irt.data.IdValue;
import irt.data.IdValueForComboBox;
import irt.data.Listeners;
import irt.data.RegisterValue;
import irt.data.listener.PacketListener;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.Value;

public class PLL_HMC807LP6CE_Reg9 extends JPanel {
	private static final int ADDRESS = 9;

	private static final int INDEX = 6;

	private static final long serialVersionUID = -4043876414280738077L;

	protected final Logger logger = (Logger) LogManager.getLogger();

	private static final IdValueForComboBox[] OffsetControl55uAStep = new IdValueForComboBox[]{
			new IdValueForComboBox((short) 0, "0 μA"),
			new IdValueForComboBox((short) 1, "55 μA"),
			new IdValueForComboBox((short) 2, "110 μA"),
			new IdValueForComboBox((short) 3, "165 μA"),
			new IdValueForComboBox((short) 4, "220 μA"),
			new IdValueForComboBox((short) 5, "275 μA"),
			new IdValueForComboBox((short) 6, "330 μA"),
			new IdValueForComboBox((short) 7, "385 μA")
	};

	private static final IdValueForComboBox[] OffsetControl7uAStep = new IdValueForComboBox[]{
			new IdValueForComboBox((short) 0, "0 μA"),
			new IdValueForComboBox((short) 1, "7 μA"),
			new IdValueForComboBox((short) 2, "14 μA"),
			new IdValueForComboBox((short) 4, "28 μA"),
			new IdValueForComboBox((short) 8, "56 μA"),
			new IdValueForComboBox((short) 15, "105 μA")
	};

	private static final IdValueForComboBox[] OffsetControl500uAStep = new IdValueForComboBox[]{
			new IdValueForComboBox((short) 0, "0 μA"),
			new IdValueForComboBox((short) 1, "500 μA"),
			new IdValueForComboBox((short) 2, "1000 μA"),
			new IdValueForComboBox((short) 3, "1500 μA"),
			new IdValueForComboBox((short) 4, "2000 μA"),
			new IdValueForComboBox((short) 5, "2500 μA"),
			new IdValueForComboBox((short) 6, "3000 μA"),
			new IdValueForComboBox((short) 7, "3500 μA")
	};

	private JComboBox<IdValueForComboBox> cfg_cp_UPtrim_sel;
	private JComboBox<IdValueForComboBox> cp_DNtrim_sel;
	private JComboBox<IdValueForComboBox> cp_UPcurrent_sel;
	private JComboBox<IdValueForComboBox> cp_DNcurrent_sel;
	private JComboBox<IdValueForComboBox> cp_UPoffset_sel;
	private JComboBox<IdValueForComboBox> cp_DNoffset_sel;
	private JTextField textField;

	private DefaultController controller;
	private RegisterValue value;
	private JButton btnClear;

	public PLL_HMC807LP6CE_Reg9(final Optional<DeviceType> deviceType) {

		final ItemListener aListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED)
					calculate();
			}
		};

		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent event) {

				controller = new DefaultController(deviceType, "PLL reg.N9", new DeviceDebagSetter(null,
						INDEX,
						ADDRESS,
						PacketWork.PACKET_ID_FCM_DEVICE_DEBUG_PLL_REG,
						PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE), Style.CHECK_ONCE){

							@Override
							protected PacketListener getNewPacketListener() {
								return new PacketListener() {

									@Override
									public void onPacketRecived(Packet packet) {
										PacketHeader header = packet.getHeader();
										if(header.getPacketId()==PacketWork.PACKET_ID_FCM_DEVICE_DEBUG_PLL_REG){

											if(header.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE){
												Payload payload = packet.getPayload(PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE);
												if(payload!=null){
													RegisterValue value = payload.getRegisterValue();
													RegisterValue oldValue = PLL_HMC807LP6CE_Reg9.this.value;
													PLL_HMC807LP6CE_Reg9.this.logger.trace("Old Value={}, New Value={}", oldValue, value);

													if(oldValue==null || !oldValue.equals(value)){
														PLL_HMC807LP6CE_Reg9.this.logger.debug("Value={}, {}",value, packet);
														oldValue = value;
														parse(value.getValue().getValue());
														textField.setText("0x"+Long.toHexString(value.getValue().getValue()).toUpperCase());
													}
												}else
													textField.setText("Payload N/A");
											}else
												textField.setText("N/A");
										}
									}

									private void parse(long value) {

										IdValueForComboBox idValueForComboBox = new IdValueForComboBox(parse(value, 2, 3), null);
										cp_UPoffset_sel.removeItemListener(aListener);
										cp_UPoffset_sel.setSelectedItem(idValueForComboBox);
										cp_UPoffset_sel.addItemListener(aListener);

										idValueForComboBox = new IdValueForComboBox(parse(value, 7, 3), null);
										cp_DNoffset_sel.removeItemListener(aListener);
										cp_DNoffset_sel.setSelectedItem(idValueForComboBox);
										cp_DNoffset_sel.addItemListener(aListener);

										idValueForComboBox = new IdValueForComboBox(parse(value, 10, 4), null);
										cfg_cp_UPtrim_sel.removeItemListener(aListener);
										cfg_cp_UPtrim_sel.setSelectedItem(idValueForComboBox);
										cfg_cp_UPtrim_sel.addItemListener(aListener);

										idValueForComboBox = new IdValueForComboBox(parse(value, 14, 4), null);
										cp_DNtrim_sel.removeItemListener(aListener);
										cp_DNtrim_sel.setSelectedItem(idValueForComboBox);
										cp_DNtrim_sel.addItemListener(aListener);

										idValueForComboBox = new IdValueForComboBox(parse(value, 18, 3), null);
										cp_UPcurrent_sel.removeItemListener(aListener);
										cp_UPcurrent_sel.setSelectedItem(idValueForComboBox);
										cp_UPcurrent_sel.addItemListener(aListener);

										idValueForComboBox = new IdValueForComboBox(parse(value, 21, 3), null);
										cp_DNcurrent_sel.removeItemListener(aListener);
										cp_DNcurrent_sel.setSelectedItem(idValueForComboBox);
										cp_DNcurrent_sel.addItemListener(aListener);
									}

									private short parse(long value, int shift, int width) {
										PLL_HMC807LP6CE_Reg9.this.logger.entry(value, shift, width);

										long filter = getFilter(shift, width); PLL_HMC807LP6CE_Reg9.this.logger.trace("Filter={}", filter);

										long tmp = value & filter;

										return PLL_HMC807LP6CE_Reg9.this.logger.exit((short) (tmp>>shift));
									}

									private long getFilter(int shift, int width) {
										long filter = 0;

										for(int i=0; i<width; i++){
											filter = filter<<1;
											filter += 1;
										}

										return filter<<shift;
									}
								};
							}
				};
				controller.setWaitTime(10000);
				Thread t = new Thread(controller);
				int priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.setDaemon(true);
				t.start();
			}
			public void ancestorMoved(AncestorEvent event) {
			}
			public void ancestorRemoved(AncestorEvent event) {
				if(controller!=null)
					controller.stop();
			}
		});
		setBorder(new TitledBorder(null, "Charge Pump", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JLabel lblChargePumpUp = new JLabel("UP Offset Control 55 \u03BCA/step");
		JLabel lblChargePumpDn = new JLabel("DN Offset Control 55 \u03BCA/step");

		ComboBoxModel<IdValueForComboBox> comboBoxModel = new DefaultComboBoxModel<>(OffsetControl55uAStep);
		cp_UPoffset_sel = new JComboBox<>(comboBoxModel);
		cp_UPoffset_sel.addItemListener(aListener);
		cp_UPoffset_sel.addPopupMenuListener(Listeners.popupMenuListener);
		comboBoxModel = new DefaultComboBoxModel<>(OffsetControl55uAStep);
		cp_DNoffset_sel = new JComboBox<>(comboBoxModel);
		cp_DNoffset_sel.addItemListener(aListener);
		cp_DNoffset_sel.addPopupMenuListener(Listeners.popupMenuListener);
		
		
		JLabel lblChargePumpUp_1 = new JLabel("UP Current Trim 7 \u03BCA/step");
		JLabel lblChargePumpDn_1 = new JLabel("DN Current Trim 7 \u03BCA/step");

		comboBoxModel = new DefaultComboBoxModel<>(OffsetControl7uAStep);
		cfg_cp_UPtrim_sel = new JComboBox<>(comboBoxModel);
		cfg_cp_UPtrim_sel.addItemListener(aListener);
		cfg_cp_UPtrim_sel.addPopupMenuListener(Listeners.popupMenuListener);
		comboBoxModel = new DefaultComboBoxModel<>(OffsetControl7uAStep);
		cp_DNtrim_sel = new JComboBox<>(comboBoxModel);
		cp_DNtrim_sel.addItemListener(aListener);
		cp_DNtrim_sel.addPopupMenuListener(Listeners.popupMenuListener);

		JLabel lblChargePumpUp_3 = new JLabel("DN MAIN Current Cntrl 500 \u03BCA step");
		JLabel lblChargePumpUp_2 = new JLabel("UP MAIN Current Cntrl 500 \u03BCA step");
		
		comboBoxModel = new DefaultComboBoxModel<>(OffsetControl500uAStep);
		cp_UPcurrent_sel = new JComboBox<>(comboBoxModel);
		cp_UPcurrent_sel.addItemListener(aListener);
		cp_UPcurrent_sel.addPopupMenuListener(Listeners.popupMenuListener);
		comboBoxModel = new DefaultComboBoxModel<>(OffsetControl500uAStep);
		cp_DNcurrent_sel = new JComboBox<>(comboBoxModel);
		cp_DNcurrent_sel.addItemListener(aListener);
		cp_DNcurrent_sel.addPopupMenuListener(Listeners.popupMenuListener);
		
		textField = new JTextField();
		textField.setColumns(10);
		
		JButton btnSet = new JButton("Set");
		btnSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					send();
				}catch(Exception ex){
					logger.catching(ex);
				}
			}
		});
		
		btnClear = new JButton("Clear The Flags");
		btnClear.setMargin(new Insets(0, 0, 0, 0));
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					ConfigurationSetter packetWork = new ConfigurationSetter(null, PacketImp.PARAMETER_CONFIG_FCM_FLAGS,
							PacketWork.PACKET_ID_CONFIGURATION_FCM_FLAGS);
					packetWork.preparePacketToSend(0);
					GuiControllerAbstract.getComPortThreadQueue().add(packetWork);
				} catch (Exception ex) {
					logger.catching(ex);
				}
			}
		});

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(cfg_cp_UPtrim_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblChargePumpUp_1))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(cp_DNtrim_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblChargePumpDn_1))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(cp_UPoffset_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblChargePumpUp))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(cp_DNoffset_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblChargePumpDn))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(cp_UPcurrent_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblChargePumpUp_2))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(cp_DNcurrent_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblChargePumpUp_3))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnSet)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnClear)))
					.addContainerGap(160, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(cfg_cp_UPtrim_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblChargePumpUp_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(cp_DNtrim_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblChargePumpDn_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(cp_UPoffset_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblChargePumpUp))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblChargePumpDn)
						.addComponent(cp_DNoffset_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(cp_UPcurrent_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblChargePumpUp_2))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(cp_DNcurrent_sel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblChargePumpUp_3))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnSet)
						.addComponent(btnClear, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(82, Short.MAX_VALUE))
		);
		groupLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cp_UPoffset_sel, cp_DNoffset_sel, cfg_cp_UPtrim_sel, cp_DNtrim_sel, cp_UPcurrent_sel, cp_DNcurrent_sel});
		setLayout(groupLayout);
	}

	protected void calculate() {
		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				int shift = shift(((IdValue)cp_UPoffset_sel	.getSelectedItem()).getID(), 2);
				shift += 	shift(((IdValue)cp_DNoffset_sel	.getSelectedItem()).getID(), 7);
				shift += 	shift(((IdValue)cfg_cp_UPtrim_sel.getSelectedItem()).getID(),10);
				shift += 	shift(((IdValue)cp_DNtrim_sel	.getSelectedItem()).getID(), 14);
				shift += 	shift(((IdValue)cp_UPcurrent_sel.getSelectedItem()).getID(), 18);
				shift += 	shift(((IdValue)cp_DNcurrent_sel.getSelectedItem()).getID(), 21);
				textField.setText("0x"+Integer.toHexString(shift).toUpperCase());
				send();
				return null;
			}

			private int shift(int id, int shiftBy) {
				logger.entry(id, shiftBy);
				return logger.exit(id<<shiftBy);
			}
		}.execute();
	}

	public void send() {
		DeviceDebagSetter packetWork = (DeviceDebagSetter) controller.getPacketWork();
		String text = textField.getText();
		try {
			if (text != null && !text.isEmpty()) {
				long value;
				if (text.startsWith("0x"))
					value = Long.parseLong(text.substring(2), 16);
				else
					value = Long.parseLong(text);

				this.value = new RegisterValue(INDEX, ADDRESS, new Value(value, 0, Long.MAX_VALUE, 0));
				packetWork.preparePacketToSend(this.value);
				logger.debug("text={}, value={}", text, this.value);
				controller.setSend(true);
			}else
				textField.setText("Put input value.");
		} catch (Exception ex) {
			textField.setText("Incorrect input.");
		}
	}
}
