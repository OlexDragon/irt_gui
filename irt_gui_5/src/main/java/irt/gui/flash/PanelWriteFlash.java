
package irt.gui.flash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.components.SerialPortController;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.flash.EmptyPacket;
import irt.gui.data.packet.observable.flash.WritePacket;
import irt.gui.flash.PanelFlash.UnitAddress;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;

public class PanelWriteFlash extends Observable{
	private final Logger logger = LogManager.getLogger();

	private final WritePacket writePacket = new WritePacket();
	private final LinkedPacket dataPacket = new EmptyPacket(){ @Override public byte[] toBytes() { return dataToSend; }};

	private int writeToAddress;
	private byte[] dataToSend;

	private int filePosition;
	private byte[] fileAsBytes;

	@FXML private ProgressIndicator 		progressIndicator;

	private final Observer sendDataObsorver = (o, arg)->{

		if (PanelFlash.checkAswer("Send data: ", (LinkedPacket) o, progressIndicator)) {

			filePosition += PanelFlash.MAX_VAR_RAM_SIZE;
			writeToAddress += PanelFlash.MAX_VAR_RAM_SIZE;

			float value = (float)filePosition / fileAsBytes.length;
			Platform.runLater(()->progressIndicator.setProgress(value));

			if (filePosition < fileAsBytes.length) {
				// Send command 'WRITE TO MEMORY'
				SerialPortController.QUEUE.add(writePacket, false);
			}
		}
	};

	private final Observer sendAddressObsorver = (o, arg)->{

		if(PanelFlash.checkAswer("Send Address: ", (LinkedPacket)o, progressIndicator)){

			dataToSend = PanelFlash.LENGTH;
			dataPacket.deleteObservers();
			dataPacket.addObserver(sendDataObsorver);
			dataToSend = prepareDataToSend();

			if(dataToSend!=null)
				SerialPortController.QUEUE.add(dataPacket, false);
		}
	};

	private final Observer writePacketObserver = (o, arg)->{

		if(PanelFlash.checkAswer("Write command", (LinkedPacket)o, progressIndicator)){

			byte[] address = Packet.toBytes(writeToAddress);
			dataToSend = PanelFlash.addCheckSum(address);
			dataPacket.deleteObservers();
			dataPacket.addObserver(sendAddressObsorver);

			//Send ADDRESS
			SerialPortController.QUEUE.add(dataPacket, false);
		}
	};

	@FXML void initialize() {
		writePacket.addObserver(writePacketObserver);
		
	}

	private byte[] prepareDataToSend() {

		if(filePosition > fileAsBytes.length)
			return null;

		final int endPosition = filePosition + PanelFlash.MAX_VAR_RAM_SIZE;

		byte[] tmp;
		int whatIsLeft = fileAsBytes.length-filePosition;
		//CD00264342.pdf p.19(2): "N+1 should always be a multiple of 4."
		int remainder = whatIsLeft % 4;
		final int toAdd = 4 - remainder;

		if(endPosition <= fileAsBytes.length)
			tmp = Arrays.copyOfRange(fileAsBytes, filePosition, endPosition);

		else{
			tmp = Arrays.copyOfRange(fileAsBytes, filePosition, fileAsBytes.length);
			tmp = Arrays.copyOf(tmp, whatIsLeft + toAdd);
			Arrays.fill(tmp, tmp.length-toAdd, tmp.length, (byte)0xFF);
		}

		byte[] data = new byte[tmp.length + 1];
		data[0] = (byte) (tmp.length - 1);

		System.arraycopy(tmp, 0, data, 1, tmp.length);

		return  PanelFlash.addCheckSum(data);
	}

	public void write(UnitAddress unitAddress, File file) throws FileNotFoundException, IOException {
		fileAsBytes = fileToBytes(file);
		writeToAddress = unitAddress.getAddr();
		filePosition = 0;

		//Send command 'WRITE TO MEMORY'
		SerialPortController.QUEUE.add(writePacket, false);
	}

	private byte[] fileToBytes(File file) throws FileNotFoundException, IOException {

		byte fileContents[] = new byte[(int) file.length()];

		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			fileInputStream.read(fileContents);
		}
		return fileContents;
	}
}
