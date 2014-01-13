package irt.tools.panel.ip_address;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class GridbagPanel extends JPanel {
	private static final long serialVersionUID = -4130961815124624638L;
	private GridBagLayout layout;
	private GridBagConstraints constraints;

	public GridbagPanel() {
	    layout = new GridBagLayout();
	    constraints = new GridBagConstraints();
	    constraints.fill = GridBagConstraints.NONE;
	    constraints.anchor = GridBagConstraints.WEST;       
	    constraints.insets = new Insets(0, 0, 0, 0);
	    setLayout(layout);
	}

	public void setHorizontalFill() {
	    constraints.fill = GridBagConstraints.HORIZONTAL;
	}

	public void setNoneFill() {
	    constraints.fill = GridBagConstraints.NONE;
	}

	public void add(Component component, int x, int y, int width, int height, int weightX, int weightY) {
	    GridBagLayout gbl = (GridBagLayout) getLayout();

	    gbl.setConstraints(component, constraints);

	    add(component);
	}

	public void add(Component component, int x, int y, int width, int height) {
	    add(component, x, y, width, height, 0, 0);
	}

	public void setBothFill() {
	    constraints.fill = GridBagConstraints.BOTH;
	}

	public void setInsets(Insets insets) {
	    constraints.insets = insets;

	}

}
