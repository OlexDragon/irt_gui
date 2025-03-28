package irt.tools.panel.subpanel;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Optional;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.DefaultController;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.setter.DeviceDebagSetter;
import irt.data.DeviceType;
import irt.data.ThreadWorker;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketID;
import irt.data.packet.interfaces.Packet;

public class DebugPanel extends JPanel{
	private static final long serialVersionUID = 6314140030152046415L;

	private final static Logger logger = LogManager.getLogger();

	private LinkHeader linkHeader;

	public DebugPanel(final Optional<DeviceType> deviceType) {
		
		JButton btnClearStatistic = new JButton("Clear Statistics");
		btnClearStatistic.addActionListener(new ActionListener() {
			int count;
			public void actionPerformed(ActionEvent e) {
				try{

				count = 3;
//				guiControllerAbstract.doDump(linkHeader);

				startThread(
						new DefaultController(
								deviceType,
								"Clear Statistics",
								new DeviceDebagSetter(
										linkHeader,
										0,
										0,
										PacketID.CLEAR_STATISTICS,
										PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE,
										0
								),
								Style.CHECK_ALWAYS){

									@Override
									public void onPacketReceived(Packet packet) {

										new ThreadWorker(()->{

											if(getPacketWork().isAddressEquals(packet) &&
													PacketID.CLEAR_STATISTICS.match(packet.getHeader().getPacketId())){

												if(packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE){
													stop();
//													guiControllerAbstract.doDump(linkHeader, "***** Statistics is cleared. *****");
													JOptionPane.showMessageDialog(null, "Statistics is cleared.");
												}else{
													if(--count<=0){
														stop();
//														guiControllerAbstract.doDump(linkHeader, "***** tatistics can not be cleaned. *****");
														JOptionPane.showMessageDialog(null, "Statistics can not be cleaned.");
													}
												}
									}
										}, "DebugPanel.onPacketReceived()");
									}					
						}
				);
				}catch(Exception ex){
					logger.catching(ex);
				}
			}

			private void startThread(Runnable runnable) {
				new ThreadWorker(runnable, "DebugPanel.startThread");
			}
		});
		
		JButton btnOpenDump = new JButton("Open DeviceDebugType");
		btnOpenDump.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String serialNumber = System.getProperty("serialNumber");
				String pathName = System.getProperty("user.home")+
									File.separator+
									"irt"+
									File.separator+
									serialNumber+
									File.separator+
									serialNumber;

				File sourceFile = new File(pathName+".log");

				try {

					File destinationFile = File.createTempFile(serialNumber+"(", ").txt");
					destinationFile.deleteOnExit();

					if (sourceFile.exists() && sourceFile.isFile()) {

						try (	final FileInputStream fileInputStream = new FileInputStream(sourceFile);
								FileChannel source = fileInputStream.getChannel();
								FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
								FileChannel destination = fileOutputStream.getChannel()) {

							destination.transferFrom(source, 0, source.size());
						}
					}

					 Desktop.getDesktop().open(destinationFile);

				} catch (Exception ex) {
					logger.catching(ex);
				}
			}
		});
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(btnClearStatistic, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnOpenDump, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap(321, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnClearStatistic)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnOpenDump)
					.addContainerGap(237, Short.MAX_VALUE))
		);
		groupLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {btnClearStatistic, btnOpenDump});
		setLayout(groupLayout);
	}

	public void setLinkHeader(LinkHeader linkHeader) {
		this.linkHeader = linkHeader;
	}
}
