package irt.tools.panel.head;

import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class IrtPanel extends MainPanel {

	private final static Logger logger = (Logger) LogManager.getLogger();
//
//	public enum Style {
//		WITH_IMAGE,
//		WITHOUT_IMAGE
//	}
//
//	public IrtPanel(JFrame target) {
//		this(target, (ImageIcon)null, "IRT Technologies", new Font("Tahoma", Font.BOLD, 16), Style.WITH_IMAGE);
//	}
//
//	public IrtPanel(JFrame target, String text, Font font) {
//		this(target, (ImageIcon)null, text, font,  Style.WITHOUT_IMAGE);
//	}

	public static Map<String, Integer> fontStyle = getFontStyles();

	protected static HashMap<String, Integer> getFontStyles() {
		HashMap<String, Integer> hashMap = new HashMap<>();
		hashMap.put("BOLD", Font.BOLD);
		hashMap.put("BOLD|ITALIC", Font.BOLD|Font.ITALIC);
		hashMap.put("PLAIN", Font.PLAIN);
		return hashMap;
	}

	public static Properties properties = getProperties();
	public static String companyIndex = properties.getProperty("used_company_index");

	protected static Properties getProperties() {
		Properties p = new Properties();
		try {
			InputStream resourceAsStream = IrtGui.class.getResourceAsStream("irt.properties");
			p.load(resourceAsStream);
		} catch (IOException e) {
			logger.catching(e);
		}
		return p;
	}

	public IrtPanel(JFrame target) {
		super(target, Integer.parseInt(properties.getProperty("IrtPanel_width_"+companyIndex)));
		setCorner(30);
		setGradient(false);
		setSize(1, 46);
		setArcStart(14);
		setArcWidth(45);
		setBackground(new Color(0x3B, 0x4A, 0x8B, 100));


		String text = properties.getProperty("company_name_"+companyIndex);

		Font font = new Font(properties.getProperty("font_name_"+companyIndex),
								fontStyle.get(properties.get("font_style_"+companyIndex)),
								Integer.parseInt(properties.get("font_size_"+companyIndex).toString()));

		ImageIcon imageIcon = new ImageIcon(
				IrtGui.class.getResource(
						properties.get("company_logo_"+companyIndex).toString()));

		String[] split = properties.get("logo_bounds_"+companyIndex).toString().split(",");

		JLabel lblIrtTechnologies = new ImageLabel(imageIcon, text);
		lblIrtTechnologies.setBounds(Integer.parseInt(split[0]),
									Integer.parseInt(split[1]),
									Integer.parseInt(split[2]),
									Integer.parseInt(split[3]));
		lblIrtTechnologies.setHorizontalAlignment(SwingConstants.RIGHT);
		lblIrtTechnologies.setForeground(new Color(176, 224, 230));
		lblIrtTechnologies.setFont(font);
		add(lblIrtTechnologies);

		JLabel lblText = new JLabel(text);
		lblText.setBounds(68, 13, 166, 20);
		lblText.setHorizontalAlignment(SwingConstants.LEFT);
		lblText.setForeground(new Color(0x3B, 0x4A, 0x8B));
		lblText.setFont(font);
		add(lblText);
		
		JLabel lblShadow = new JLabel(text);
		lblShadow.setHorizontalAlignment(SwingConstants.LEFT);
		lblShadow.setForeground(Color.WHITE);
		lblShadow.setFont(font);
		lblShadow.setBounds(lblText.getX()-1, lblText.getY()-1, 166, 20);
		add(lblShadow);
	}
}
