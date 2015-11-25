
package irt.tools;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxUI;

import irt.data.Listeners;

public class IrtComboBox<T> extends JComboBox<T> {
	private static final long serialVersionUID = 3184099659158422530L;

	public IrtComboBox() {

		setUI(new BasicComboBoxUI(){ @SuppressWarnings("serial") @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0; }};}});
		addPopupMenuListener(Listeners.popupMenuListener);
	}

}
