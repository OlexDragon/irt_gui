package irt.tools.panel.head;

import irt.tools.label.VarticalLabel;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

@SuppressWarnings("serial")
public class Panel extends JPanel {

	protected int MIN_WIDTH = 25;
	protected int MID_WIDTH = 310;
	protected int MAX_WIDTH = 615;
	protected int MIN_HEIGHT = 25;
	protected int MAX_HEIGHT = 444;
	protected int BTN_WIDTH;

	protected Color backgroundColor = new Color(0x0B,0x17,0x3B);

	protected VarticalLabel verticalLabel;
	protected JPanel userPanel;
	protected JPanel extraPanel;

	public Panel( String verticalLabelText, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {

		if(minWidth>0)
			MIN_WIDTH =  minWidth;
		if(midWidth>0)
			MID_WIDTH = midWidth;
		if(maxWidth>0)
			MAX_WIDTH =  maxWidth;
		if(minHeight>0)
			MIN_HEIGHT = minHeight;
		if(maxHeight>0)
			MAX_HEIGHT = maxHeight;
		setSize(MAX_WIDTH, MAX_HEIGHT);

		setOpaque(false);
		setLayout(null);

		JButton btnMaxSize = new JButton("");
		btnMaxSize.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(isMidSize())
					setMaxSize();
				else
					setMidSize();
				getParent().getParent().getParent().repaint();
			}
		});
		btnMaxSize.setBounds(MID_WIDTH-15, MIN_HEIGHT, 15, MAX_HEIGHT-MIN_HEIGHT);
		add(btnMaxSize);
		BTN_WIDTH = btnMaxSize.getWidth();

		verticalLabel = new VarticalLabel(verticalLabelText, false);
		verticalLabel.setBounds(0, MIN_HEIGHT, MIN_WIDTH, getHeight()-MIN_HEIGHT);
		verticalLabel.setOpaque(true);
		verticalLabel.setBackground(new Color(0, 153, 255));
		verticalLabel.setForeground(getForeground());
		verticalLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		verticalLabel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		verticalLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if(isThinSize())
					setMidSize();
				else
					setThinSize();
				getParent().getParent().getParent().repaint();
			}
		});
		add(verticalLabel);

		userPanel = new JPanel();
		Color color = new Color(0x0B,0x17,0x3B);
		userPanel.setBackground(color);
		userPanel.setBounds(MIN_WIDTH, MIN_HEIGHT, MID_WIDTH-MIN_WIDTH-BTN_WIDTH, MAX_HEIGHT-MIN_HEIGHT);
		userPanel.setLayout(null);
		add(userPanel);

		extraPanel = new	 JPanel();	
		extraPanel.setBackground(new Color(0,0x33,0x33));
		extraPanel.setBounds(MID_WIDTH, MIN_HEIGHT, MAX_WIDTH-MID_WIDTH, MAX_HEIGHT-MIN_HEIGHT);
		extraPanel.setLayout(null);
		add(extraPanel);
	}

	public boolean isMinSize(){
		return getWidth()==MIN_WIDTH && getHeight()==MIN_HEIGHT;
	}

	public boolean isMidSize(){
		return getWidth()==MID_WIDTH && getHeight()==MAX_HEIGHT;
	}

	public boolean isMaxSize(){
		return getWidth()==MAX_WIDTH && getHeight()==MAX_HEIGHT;
	}

	public boolean isThinSize(){
		return getWidth()==MIN_WIDTH && getHeight()==MAX_HEIGHT;
	}

	public void setMinSize(){
		if(getWidth()!=MIN_WIDTH || getHeight()!=MIN_HEIGHT)
			setSize(MIN_WIDTH, MIN_HEIGHT);
	}

	public void setMidSize(){
		setSize(MID_WIDTH, MAX_HEIGHT);
	}

	public void setMaxSize(){
		setSize(MAX_WIDTH, MAX_HEIGHT);
	}

	public void setThinSize(){
		if(getWidth()!=MIN_WIDTH || getHeight()!=MAX_HEIGHT)
			setSize(MIN_WIDTH, MAX_HEIGHT);
	}

	public void refresh() {
		System.out.println("Panel.refresh");
	}
}
