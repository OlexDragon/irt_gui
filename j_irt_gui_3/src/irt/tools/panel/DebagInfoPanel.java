package irt.tools.panel;

import irt.controller.DeviceDebugController;
import irt.controller.DumpControllers;
import irt.controller.GuiController;
import irt.controller.serial_port.value.getter.Getter;
import irt.data.PacketWork;
import irt.data.RundomNumber;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial")
public class DebagInfoPanel extends JPanel {

	private DeviceDebugController deviceDebagInfoController;
	private LinkHeader linkHeader;
	private JTextArea textArea;
	private JComboBox<String> cbCommand;
	private JComboBox<Integer> cbParameter;
	private JPanel owner;

	public DebagInfoPanel(LinkHeader linkHeader, JPanel panel) {
		this.linkHeader = linkHeader;

		if(panel!=null)
			owner = panel;

			setLayout(new BorderLayout(0, 0));

		textArea = new JTextArea();
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				int modifiers = mouseEvent.getModifiers();
				int mask = KeyEvent.VK_CONTROL|KeyEvent.VK_SHIFT;
				if((modifiers&mask)==mask){
					Preferences prefs = GuiController.getPrefs();
					int prefsValue = prefs.getInt(DumpControllers.DUMP_WAIT,10);
					String output = JOptionPane.showInputDialog(owner, "Put The Time in minutes ("+prefsValue+" min)");
					if(output!=null){
						output = output.replaceAll("\\D", "");
						if(!output.isEmpty() && output.length()<10){
							int waitTime = Integer.parseInt(output);
							if(prefsValue!=waitTime)
								prefs.putInt(DumpControllers.DUMP_WAIT, waitTime);
						}else{
							JOptionPane.showMessageDialog(owner, "Wrong input.");
						}
					}
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(textArea);
		add(scrollPane, BorderLayout.CENTER);

		panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		cbCommand = new JComboBox<String>();
		cbCommand.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				if(itemEvent.getStateChange()==ItemEvent.SELECTED)
					cbCommand.setToolTipText(cbCommand.getSelectedItem().toString());
			}
		});
		cbCommand.addItem("device information: parts, firmware and etc.");
		cbCommand.addItem("dump of registers for specified device index ");
		panel.add(cbCommand, BorderLayout.CENTER);
		
		cbParameter = new JComboBox<Integer>();
		cbParameter.setMaximumRowCount(9);
		cbParameter.setPreferredSize(new Dimension(50, 20));
		cbParameter.setEditable(true);
		for(int i=0; i<7; i++)
			cbParameter.addItem(i);
		cbParameter.addItem(10);
		cbParameter.addItem(100);
		panel.add(cbParameter, BorderLayout.EAST);

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {

				deviceDebagInfoController = new DeviceDebugController("Info Controller",
																new Getter(
																		DebagInfoPanel.this.linkHeader,
																		Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG,
																		(byte) (cbCommand.getSelectedIndex()+1),
																		PacketWork.PACKET_ID_DEVICE_DEBAG_DEVICE_INFO),
																cbCommand,
																cbParameter,
																DebagInfoPanel.this.textArea);

				deviceDebagInfoController.setWaitTime(10000);//10 sec

				Thread t = new Thread(deviceDebagInfoController, "DebagInfoPanel.Info Controller-"+new RundomNumber());
				int priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.setDaemon(true);
				t.start();
			}
			public void ancestorMoved(AncestorEvent arg0) {
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				deviceDebagInfoController.stop();
			}
		});
	}
}
