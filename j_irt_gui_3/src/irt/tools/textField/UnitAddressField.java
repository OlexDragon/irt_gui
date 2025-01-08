package irt.tools.textField;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.prefs.Preferences;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiController;
import irt.controller.GuiControllerAbstract;
import irt.tools.panel.head.IrtPanel;

public class UnitAddressField extends JTextField {
	private static final long serialVersionUID = 7712969376488543032L;
	private final Logger logger = LogManager.getLogger();

	private static UnitAddressField thisFiald;

	public static final byte[] DEFAULT_ADDRESS = new byte[]{(byte) 254};
	public final String[] DEFAULT_ADDRESS_STR = new String[]{Integer.toString(DEFAULT_ADDRESS[0])};

	private final String PREF_KEY_ADDRESS;
	private final Preferences prefs = GuiController.getPrefs();

	private byte[] selectedAdress;

	public UnitAddressField(String pref_key_address) {

		thisFiald = this;
		PREF_KEY_ADDRESS = pref_key_address;
		setSelectedAddress();

		setHorizontalAlignment(SwingConstants.CENTER);
		setFont(new Font("Tahoma", Font.BOLD, 18));
		setColumns(8);
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(this, popupMenu);
		
		JMenuItem mntmDefault = new JMenuItem("Default");
		mntmDefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectedAdress = DEFAULT_ADDRESS;
				prefs.putByteArray(PREF_KEY_ADDRESS, selectedAdress);
				setText();
				setToolTipText();
				GuiControllerAbstract.setAddresses(selectedAdress);
			}
		});
		popupMenu.add(mntmDefault);

		setText();
		setToolTipText();
		GuiControllerAbstract.setAddresses(selectedAdress);

		addActionListener(new AddressListener());
	}

	private void setToolTipText() {
		StringBuilder sb = new StringBuilder("Unit Address( ");
		sb.append(selectedAddressToString()).append(" }");
		setToolTipText(sb.toString());
	}

	private void setSelectedAddress() {
		String p = IrtPanel.PROPERTIES.getProperty("addresses");

		selectedAdress = Optional
							.ofNullable(p)
							.map(s->stringToByteArray(s))
							.filter(a->a.length!=0)
							.orElse(prefs.getByteArray(PREF_KEY_ADDRESS, DEFAULT_ADDRESS));

		Arrays.sort(selectedAdress);
		logger.trace("{}", selectedAdress);
	}

	private void setText() {

		final String toSet = selectedAddressToString();
		final String text = getText();

		if(!toSet.equals(text))
			setText(toSet);
	}

	private String selectedAddressToString() {

		StringBuilder sb = new StringBuilder();

		for(byte b:selectedAdress){

			if(sb.length()!=0)
				sb.append(",");

			sb.append(b&0xFF); 
		}

		final String toSet = sb.toString();
		return toSet;
	}

	private byte[] stringToByteArray(final String text) {
		logger.info(text);
		final int[] intArray = Optional
									.ofNullable(text)
									.map(t -> t.replaceAll("\\D+", " "))
									.map(t -> t.split(" "))
									.map(a -> Arrays.stream(a))
									.orElse(Arrays.stream(DEFAULT_ADDRESS_STR))
									.filter(s->!s.isEmpty())
									.mapToInt(s->Integer.parseInt(s))
//									.filter(i->i>0)
									.filter(i->i<=(DEFAULT_ADDRESS[0]&0xFF))
									.sorted()
									.toArray();

		final int length = intArray.length;
		byte[] sa = length==0 ? DEFAULT_ADDRESS : new byte[length];

		for(int i=0; i<length; i++) sa[i] = (byte) intArray[i];

		logger.debug("{}", sa);
		return sa;
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	public static void setAddress(Byte addr) {

		if(thisFiald==null)
			return;

		final String text = new Integer(addr &0xff).toString();
		thisFiald.setText(text);
		for(ActionListener a: thisFiald.getActionListeners())
			a.actionPerformed(null);
	}

	// class AddressListener **********************************************************************************
	private class AddressListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {

			selectedAdress = stringToByteArray(getText());

			prefs.putByteArray(PREF_KEY_ADDRESS, selectedAdress);
			setText();
			setToolTipText();
			GuiControllerAbstract.setAddresses(selectedAdress);
		}
	}
}
