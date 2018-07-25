package irt.tools.panel.subpanel.monitor;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.DefaultController;
import irt.controller.control.ControllerAbstract;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo.DeviceType;
import irt.data.MyThreadFactory;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.Payload;

@SuppressWarnings("serial")
public abstract class MonitorPanelAbstract extends JPanel implements Refresh, Monitor  {

	protected final Logger logger = LogManager.getLogger(getClass());

	protected LinkHeader linkHeader;
	protected TitledBorder titledBorder;
	protected String selectedLanguage;

	protected Optional<DeviceType> deviceType;

//	public MonitorPanelAbstract(LinkHeader linkHeader){
//		this(linkHeader, "Monitor", 214, 210);
//	}

	protected MonitorPanelAbstract(final Optional<DeviceType> deviceType, LinkHeader linkHeader, String title, int width, int height) {
		setName("MonitorPanelFx");
		this.linkHeader = linkHeader!=null ? linkHeader : new LinkHeader((byte)0, (byte)0, (short)0);
		this.deviceType = deviceType;

		selectedLanguage = Translation.getSelectedLanguage();

		setOpaque(false);

		Font font = getTitledBorderFont();

		titledBorder = new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.LEADING, TitledBorder.TOP, font, Color.WHITE);
		setBorder(titledBorder);
		if(width==0)
			width = 200;
		if(height==0)
			height = 100;
		setSize(width, height);
		setLayout(null);

		addAncestorListener(new AncestorListener() {
			private List<ControllerAbstract> defaultControllers;
			public void ancestorAdded(AncestorEvent event) {
				defaultControllers = getControllers();
				startControllers(defaultControllers);
			}
			private void startControllers( List<ControllerAbstract> controllers) {
				if(controllers!=null)
					for(ControllerAbstract ca:controllers)
						new MyThreadFactory(ca, "MonitorPanelAbstract.startControllers");
			}
			public void ancestorMoved(AncestorEvent event) { }
			public void ancestorRemoved(AncestorEvent event) {
				if(defaultControllers!=null){
					for(Iterator<ControllerAbstract> c=defaultControllers.iterator(); c.hasNext();){
						ControllerAbstract next = c.next();
						next.stop();
					}
					defaultControllers = null;
				}
			}
		});
	}

	protected abstract List<ControllerAbstract> getControllers();

	private Font getTitledBorderFont() {
		return Translation.getFont()
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.size", 18))
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.type", Font.BOLD));
	}

	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	public void refresh() {

		selectedLanguage = Translation.getSelectedLanguage();

		Font font = getTitledBorderFont();
		titledBorder.setTitleFont(font);
	}

	protected DefaultController getController(String controllerName, byte parameter, final PacketIDs packetID) {
		return null;
	}

	protected String getOperator(byte flags) {
		String operator;
		switch(flags){
		case 2:
			operator = "<";
			break;
		case 3:
			operator = ">";
			break;
		default:
			operator = "";
		}
		return operator;
	}

	protected abstract void packetRecived(List<Payload> payloads);
}
