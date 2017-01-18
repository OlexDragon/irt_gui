
package irt.fx.control.prologix.enums;

import static org.junit.Assert.*;

import java.util.InputMismatchException;

import org.junit.Test;

import irt.data.prologix.Eos;
import irt.data.prologix.PrologixCommands;
import irt.data.prologix.PrologixDeviceType;

public class PrologixCommandsTest {

	@Test(expected=InputMismatchException.class)
	public void addrExceptionTest() {
		
		PrologixCommands.ADDR.setValue(true);
	}

	@Test
	public void addrTest() {

		PrologixCommands.ADDR.setValue(22);

		assertNull(PrologixCommands.ADDR.getOldValue());
		Integer v = (Integer)PrologixCommands.ADDR.getValue();
		assertNotNull(v);
		assertEquals(22, (int)v);

		byte[] command = PrologixCommands.ADDR.getCommand();
		assertArrayEquals(new byte[]{ 43, 43, 97, 100, 100, 114, 32, 50, 50, 10}, command);
		assertEquals("++addr 22\n", new String(command));

		assertNull(PrologixCommands.ADDR.getValue());
		assertEquals(22, PrologixCommands.ADDR.getOldValue());

		command = PrologixCommands.ADDR.getCommand();
		assertArrayEquals(new byte[]{ 43, 43, 97, 100, 100, 114, 10}, command);
		assertEquals("++addr\n", new String(command));

		assertNull(PrologixCommands.ADDR.getOldValue());
		assertNull(PrologixCommands.ADDR.getValue());
	}

	@Test(expected=InputMismatchException.class)
	public void modeExceptionTest() {
		
		PrologixCommands.MODE.setValue(true);
	}

	@Test
	public void modeTest() {
		
		PrologixCommands.MODE.setValue(PrologixDeviceType.FOR_BOTH);
		assertEquals(PrologixDeviceType.FOR_BOTH, PrologixCommands.MODE.getValue());

		PrologixCommands.MODE.setValue("controller");
		assertEquals(PrologixDeviceType.CONTROLLER, PrologixCommands.MODE.getValue());

		PrologixCommands.MODE.setValue(0);
		assertEquals(PrologixDeviceType.DEVICE, PrologixCommands.MODE.getValue());

		PrologixCommands.MODE.setValue("1");
		assertEquals(PrologixDeviceType.CONTROLLER, PrologixCommands.MODE.getValue());

		PrologixCommands.MODE.setValue('0');
		assertNull(PrologixCommands.MODE.getOldValue());
		assertEquals(PrologixDeviceType.DEVICE, PrologixCommands.MODE.getValue());

		byte[] command = PrologixCommands.MODE.getCommand();
		assertArrayEquals(new byte[]{ 43, 43, 109, 111, 100, 101, 32, 48, 10}, command);
		assertEquals("++mode 0\n", new String(command));

		assertNull(PrologixCommands.MODE.getValue());
		assertEquals(PrologixDeviceType.DEVICE, PrologixCommands.MODE.getOldValue());

		command = PrologixCommands.MODE.getCommand();
		assertArrayEquals(new byte[]{ 43, 43, 109, 111, 100, 101, 10}, command);
		assertEquals("++mode\n", new String(command));

		assertNull(PrologixCommands.MODE.getOldValue());
		assertNull(PrologixCommands.MODE.getValue());

	}
	@Test(expected=InputMismatchException.class)
	public void saveConfigExceptionTest() {		
		PrologixCommands.SAVECFG.setValue(Eos.CR_LF);
	}

	@Test
	public void saveConfigTest() {

		PrologixCommands.SAVECFG.setValue(false);

		assertNull(PrologixCommands.SAVECFG.getOldValue());
		assertNotNull(PrologixCommands.SAVECFG.getValue());
		assertEquals(false, PrologixCommands.SAVECFG.getValue());

		byte[] command = PrologixCommands.SAVECFG.getCommand();
		assertArrayEquals(new byte[]{ 43, 43, 115, 97, 118, 101, 99, 102, 103, 32, 48, 10}, command);
		assertEquals("++savecfg 0\n", new String(command));

		assertNull(PrologixCommands.SAVECFG.getValue());
		assertEquals(false, PrologixCommands.SAVECFG.getOldValue());

		command = PrologixCommands.SAVECFG.getCommand();
		assertArrayEquals(new byte[]{ 43, 43, 115, 97, 118, 101, 99, 102, 103, 10}, command);
		assertEquals("++savecfg\n", new String(command));

		assertNull(PrologixCommands.SAVECFG.getOldValue());
		assertNull(PrologixCommands.SAVECFG.getValue());

		PrologixCommands.SAVECFG.setValue(new byte[]{48, 13, 10});
		assertEquals(false, PrologixCommands.SAVECFG.getValue());

		PrologixCommands.SAVECFG.setValue(new byte[]{49, 13, 10});
		assertEquals(true, PrologixCommands.SAVECFG.getValue());
	}

	@Test
	public void eosTest() {

		PrologixCommands.EOS.setValue(Eos.CR_LF);

		assertNull(PrologixCommands.EOS.getOldValue());
		assertNotNull(PrologixCommands.EOS.getValue());
		assertEquals(Eos.CR_LF, PrologixCommands.EOS.getValue());

		byte[] command = PrologixCommands.EOS.getCommand();
		assertArrayEquals(new byte[]{ 43, 43, 101, 111, 115, 32, 48, 10}, command);
		assertEquals("++eos 0\n", new String(command));

		assertNull(PrologixCommands.EOS.getValue());
		assertEquals(Eos.CR_LF, PrologixCommands.EOS.getOldValue());

		command = PrologixCommands.EOS.getCommand();
		assertArrayEquals(new byte[]{ 43, 43, 101, 111, 115, 10}, command);
		assertEquals("++eos\n", new String(command));

		assertNull(PrologixCommands.EOS.getOldValue());
		assertNull(PrologixCommands.EOS.getValue());
	}
}
