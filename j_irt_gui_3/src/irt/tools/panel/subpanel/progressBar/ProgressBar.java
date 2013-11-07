package irt.tools.panel.subpanel.progressBar;

import irt.data.value.Value;
import irt.data.value.Value.Status;
import irt.data.value.ValueDouble;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class ProgressBar extends JPanel implements Observer{
	private static final long serialVersionUID = 1257848427070457804L;

	private final static Logger logger = (Logger) LogManager.getLogger();

	private static Value value = new ValueDouble(0, 0, Integer.MAX_VALUE, 0);

	public ProgressBar() {
		logger.trace("ProgressBar()");
		setToolTipText("");
		addComponentListener(new ComponentAdapter() {
			private int sectionsCount;

			@Override
			public void componentResized(ComponentEvent ce) {
				Component[] components = ProgressBar.this.getComponents();
				sectionsCount = components.length;
				for(int i=0; i<components.length; )
					setSectionSize(components[i], ++i);
			}

			private void setSectionSize(Component c, int sectionsNumber) {
				if(sectionsCount>sectionsNumber){
					Dimension size = getSize();
					size.height = size.height / sectionsCount * sectionsNumber;
					c.setPreferredSize(size);
					c.setSize(size);
					logger.trace(size);
				}
			}
		});
		setOpaque(false);

		value.addObserver(this);

		Section section1 = new Section();
		section1.setPreferredSize(new Dimension(10, 300));
		section1.setCorner(10);
		section1.setArc(false);
		section1.setUnderRangeColor(Color.RED);
		section1.setMoreThenRangeColor(Color.GREEN);
		value.addObserver(section1);
		
		Section section2 = new Section();
		section2.setPreferredSize(new Dimension(10, 300));
		section2.setCorner(10);
		section2.setArc(false);
		section2.setUnderRangeColor(Color.WHITE);
		section2.setMoreThenRangeColor(Color.GREEN);
		value.addObserver(section2);
		
		Section section3 = new Section();
		section3.setPreferredSize(new Dimension(10, 300));
		section3.setCorner(10);
		section3.setArc(false);
		section3.setUnderRangeColor(Color.WHITE);
		section3.setMoreThenRangeColor(Color.GREEN);
		value.addObserver(section3);
	
		Section section4 = new Section();
		section4.setPreferredSize(new Dimension(10, 300));
		section4.setCorner(10);
		section4.setArc(false);
		section4.setUnderRangeColor(Color.WHITE);
		section4.setMoreThenRangeColor(Color.GREEN);
		value.addObserver(section4);

		Section section5 = new Section();
		section5.setPreferredSize(new Dimension(10, 300));
		section5.setCorner(10);
		section5.setArc(false);
		setMinMaxValues();
		section5.setUnderRangeColor(Color.WHITE);
		value.addObserver(section5);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(section1, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
					.addComponent(section2, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
					.addComponent(section3, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
					.addComponent(section4, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
					.addComponent(section5, GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
					)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
					.addComponent(section5, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(section4, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(section3, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(section2, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(section1, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
		);
		section5.setLayout(null);
		section4.setLayout(null);
		section3.setLayout(null);
		section2.setLayout(null);
		section1.setLayout(null);
		setLayout(groupLayout);
	}

	private void setMinMaxValues() {
		Component[] components = getComponents();
		long minMax = value.getMinValue();
		long step = value.getRelativeMaxValue()/5;
		for(Component c:components){
			if(c instanceof Section)
				((Section)c).setMinMaxValue(minMax, minMax+=step);
		}
	}

	public static Value getValue() {
		return value;
	}

	public static void setMinMaxValue(String minValueStr, String maxValueStr) {
		logger.trace("setMaxValue({}, {})", minValueStr, maxValueStr);
		value.setMinMax(minValueStr, maxValueStr);
	}

	@Override
	public void update(Observable o, Object obj) {
		if(obj instanceof Status){
			Status s = (Status) obj;
			if(s == Status.RANGE_SET){
				setMinMaxValues();
				logger.trace("update(Observable {}, Object {})", o, obj);
			}
		}
	}
}
