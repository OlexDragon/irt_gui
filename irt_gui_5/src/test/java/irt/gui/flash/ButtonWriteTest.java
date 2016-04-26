
package irt.gui.flash;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import irt.gui.data.ToHex;

public class ButtonWriteTest {
	private final Logger logger = LogManager.getLogger();

	byte[] fileAsBytes = new byte[]{8, 14, 8, 0, 14};

	@Test
	public void test1() {

		//test part of prepareDataToSend()

		int filePosition = 0;
 
		byte[] data = partOfPrepareDataToSend(filePosition, fileAsBytes);

		logger.trace("\nfileAsBytes: {}\ndata:        {}", ToHex.bytesToHex(fileAsBytes), ToHex.bytesToHex(data));
		assertNotEquals(fileAsBytes.length, data.length);
		assertEquals(10, data.length);
	}

	@Test
	public void test2() {

		//test part of prepareDataToSend()

		int filePosition = 4;
 
		byte[] data = partOfPrepareDataToSend(filePosition, fileAsBytes);

		logger.trace("\nfileAsBytes: {}\ndata:                    {}", ToHex.bytesToHex(fileAsBytes), ToHex.bytesToHex(data));
		assertNotEquals(fileAsBytes.length, data.length);
	}

	@Test
	public void test3() {

		//test part of prepareDataToSend()

		int filePosition = 1;
 
		byte[] data = partOfPrepareDataToSend(filePosition, fileAsBytes);

		logger.trace("\nfileAsBytes: {}\ndata:           {}", ToHex.bytesToHex(fileAsBytes), ToHex.bytesToHex(data));
		assertNotEquals(fileAsBytes.length, data.length);
	}

	@Test
	public void test4() {

		//test part of prepareDataToSend()

		int filePosition = 2;
 
		byte[] data = partOfPrepareDataToSend(filePosition, fileAsBytes);

		logger.trace("\nfileAsBytes: {}\ndata:              {}", ToHex.bytesToHex(fileAsBytes), ToHex.bytesToHex(data));
		assertNotEquals(fileAsBytes.length, data.length);
	}

	private byte[] partOfPrepareDataToSend(int filePosition, byte[] fileAsBytes) {
		if(filePosition > fileAsBytes.length)
			return null;

		final int endPosition = filePosition + PanelFlash.MAX_VAR_RAM_SIZE;

		byte[] tmp;
		int whatIsLeft = fileAsBytes.length-filePosition;
		//CD00264342.pdf p.19(2): "N+1 should always be a multiple of 4."
		int remainder = whatIsLeft % 4;
		final int toAdd = 4 - remainder;
		logger.error("endPosition:{}, fileAsBytes.length:{}, whatIsLeft:{}, remainder:{}, toAdd:{}", endPosition, fileAsBytes.length, whatIsLeft, remainder, toAdd);

		if(endPosition <= fileAsBytes.length)
			tmp = Arrays.copyOfRange(fileAsBytes, filePosition, endPosition);
		else{
			tmp = Arrays.copyOfRange(fileAsBytes, filePosition, fileAsBytes.length);
			logger.error(ToHex.bytesToHex(tmp));
			tmp = Arrays.copyOf(tmp, whatIsLeft + toAdd);
			Arrays.fill(tmp, tmp.length-toAdd, tmp.length, (byte)0xFF);
			logger.error(ToHex.bytesToHex(tmp));
		}

		byte[] data = new byte[tmp.length + 1];
		data[0] = (byte) (tmp.length - 1);

		System.arraycopy(tmp, 0, data, 1, tmp.length);

		return  PanelFlash.addCheckSum(data);
	}

}
