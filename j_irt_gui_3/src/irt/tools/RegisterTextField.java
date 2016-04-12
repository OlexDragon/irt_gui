
package irt.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.data.MyThreadFactory;
import irt.data.PacketWork;
import irt.data.RegisterValue;
import irt.data.listener.PacketListener;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.RegisterPacket;
import irt.data.value.Value;

public class RegisterTextField extends JTextField implements PacketListener {
	private static final long serialVersionUID = 517630309792962880L;
	private final Logger logger = LogManager.getLogger();

	public final int MIN;
	public final int MAX;

	private PacketWork getPacket;
	private PacketWork setPacket;

	private final RegisterValue valueToSend;
	private final RegisterValue valueSaveRegister;

	private final 	ScheduledExecutorService	service = Executors.newScheduledThreadPool(5, new MyThreadFactory());
	private ScheduledFuture<?> scheduleAtFixedRate;

	private final TextFieldUpdater updater 		= new TextFieldUpdater();

	public RegisterTextField(Byte linkAddr, RegisterValue value, short packetId, int min, int max) {

		MIN = min;
		MAX = max;

		value.setValue(null); // if value is null packet type is REQIEST
		getPacket = new RegisterPacket(linkAddr, value, packetId);

		valueToSend = new RegisterValue(value);
		valueToSend.setValue( new Value(896, MIN, MAX, 0)); // if value not null packet type is COMMAND
		setPacket = new RegisterPacket(linkAddr, valueToSend, packetId);

		valueSaveRegister = new RegisterValue(value.getIndex(), value.getAddr()+3, new Value(0, 0, 0, 0));

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);

		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				send();
			}
		});
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if(!(Character.isDigit(c) || (c==KeyEvent.VK_BACK_SPACE || c==KeyEvent.VK_DELETE) || c==KeyEvent.VK_ENTER)){
				    getToolkit().beep();
				    e.consume();
				}
			}
			
			@Override public void keyReleased(KeyEvent e) { }
			@Override public void keyPressed(KeyEvent e) { }
		});
//		addFocusListener(new FocusListener() {
//			
//			@Override
//			public void focusLost(FocusEvent e) {
//				start();
//			}
//			
//			@Override
//			public void focusGained(FocusEvent e) {
//				stop();
//			}
//		});
	}

	@Override
	public void packetRecived(Packet packet) {
		updater.setPacket(packet);
		service.execute(updater);
	}

	public void start(){
		if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())
			scheduleAtFixedRate = service.scheduleAtFixedRate(new Sender(), 1, 3, TimeUnit.SECONDS);
	}

	public void stop(){
		if(scheduleAtFixedRate!=null)
			scheduleAtFixedRate.cancel(true);
	}

	public void saveRegister(){

		((RegisterPacket)setPacket).setValue(valueSaveRegister);
		GuiControllerAbstract.getComPortThreadQueue().add(setPacket);
	}

	public void send() {
		final String text = getText();
		if(text.isEmpty()){
		    getToolkit().beep();
		    return;
		}

		final int value = Integer.parseInt(text);
		valueToSend.getValue().setValue(value);
		((RegisterPacket)setPacket).setValue(valueToSend);

		GuiControllerAbstract.getComPortThreadQueue().add(setPacket);
	}

	//************************ class TextFieldUpdater *********************************
	private class TextFieldUpdater implements Runnable{

		private Packet packet;

		public void setPacket(Packet packet) {
			this.packet = packet;
		}

		@Override
		public void run() {
			final PacketHeader header = packet.getHeader();
			if(header.getOption()==PacketImp.NO_ERROR)

				try{

					if(packet.equals(getPacket)){

						final RegisterValue registerValue = packet.getPayload(0).getRegisterValue();
						if(registerValue.equals(valueSaveRegister))
							showSaved();

						else
							setValue(registerValue);
					}
				}catch(Exception e){
					logger.catching(e);
				}
		}

		private void setValue(final RegisterValue registerValue) {
			final String str = registerValue.getValue().toString();

			if(!str.equals(getText()))
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						setText(str);
					}
				});
		}

		private void showSaved() throws InterruptedException {
			stop();
			Thread.sleep(100);
			setText("SAVED");
			Thread.sleep(2000);
			start();
		}
	}

	//************************ class TextFieldUpdater *********************************
	private class Sender implements Runnable{
		@Override
		public void run() {
			GuiControllerAbstract.getComPortThreadQueue().add(getPacket);
		}
	}
}
