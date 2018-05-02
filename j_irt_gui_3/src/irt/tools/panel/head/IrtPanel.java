package irt.tools.panel.head;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;

@SuppressWarnings("serial")
public class IrtPanel extends MainPanel {

	private final static Logger logger = LogManager.getLogger();

	private static Map<String, Integer> fontStyle;

	public static ImageIcon logoIcon;

	protected static HashMap<String, Integer> getFontStyles() {
		HashMap<String, Integer> hashMap = new HashMap<>();
		hashMap.put("BOLD", Font.BOLD);
		hashMap.put("BOLD|ITALIC", Font.BOLD|Font.ITALIC);
		hashMap.put("PLAIN", Font.PLAIN);
		return hashMap;
	}

	public static Integer parseFontStyle(String fontStyleStr) {

		if(fontStyle==null)
			fontStyle=getFontStyles();

		return fontStyleStr!=null ? fontStyle.get(fontStyleStr) : 0;
	}

	public static Properties PROPERTIES;

	static{
		try(InputStream resourceAsStream = IrtGui.class.getResourceAsStream("irt.properties");) {
			PROPERTIES = new Properties();

			PROPERTIES.load(resourceAsStream);

			File f = new File(System.getProperty("user.dir"), "gui.properties");
			if(f.exists() && !f.isDirectory()){
				try (FileInputStream inStream = new FileInputStream(f);) {
					PROPERTIES.load(inStream);
					PROPERTIES.put("lastModified", "" + f.lastModified());
				}
			}

			String logoPath = PROPERTIES.getProperty("company_logo");
			URL resource = IrtGui.class.getResource(logoPath);
			if(resource!=null)
				logoIcon = new ImageIcon(resource);
			else
				logoIcon = new ImageIcon(logoPath);
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private static int getIrtWidth() {
		String property = PROPERTIES.getProperty("IrtPanel_width");
		return property!=null ? Integer.parseInt(property) : 235;
	}

	public IrtPanel(JFrame target) {
		super(target, getIrtWidth());
		setCorner(30);
		setGradient(false);
		setSize(1, 46);
		setArcStart(14);
		setArcWidth(45);
		setBackground(new Color(0x3B, 0x4A, 0x8B, 100));

		String companyName = PROPERTIES.getProperty("company_name");

		final JLabel lblIrtTechnologies = new ImageLabel(logoIcon, companyName);
		lblIrtTechnologies.setHorizontalAlignment(SwingConstants.RIGHT);
		lblIrtTechnologies.setForeground(new Color(176, 224, 230));
		add(lblIrtTechnologies);

		final JLabel lblText = new JLabel(companyName);
		lblText.setBounds(68, 13, 166, 20);
		lblText.setHorizontalAlignment(SwingConstants.LEFT);
		lblText.setForeground(new Color(0x3B, 0x4A, 0x8B));
		add(lblText);
		
		final JLabel lblShadow = new JLabel(companyName);
		lblShadow.setHorizontalAlignment(SwingConstants.LEFT);
		lblShadow.setForeground(Color.WHITE);
		lblShadow.setBounds(lblText.getX()-1, lblText.getY()-1, 166, 20);
		add(lblShadow);

		new SwingWorker<Rectangle, Void>() {
			@Override
			protected Rectangle doInBackground() throws Exception {
				try {
					String[] split = PROPERTIES.get("logo_bounds").toString().split(",");
					return new Rectangle(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
				} catch (Exception e) {
					logger.catching(e);
					return null;
				}
			}

			@Override
			protected void done() {
				try {
					lblIrtTechnologies.setBounds(get());
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();

		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				try {
					return new Font(PROPERTIES.getProperty("font_name"), parseFontStyle( PROPERTIES.getProperty("font_style")),
							Integer.parseInt(PROPERTIES.get("font_size").toString()));
				} catch (Exception e) {
					logger.catching(e);
					return null;
				}
			}

			@Override
			protected void done() {
				try {

					Font font = get();
					lblIrtTechnologies.setFont(font);
					lblText.setFont(font);
					lblShadow.setFont(font);

				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();

	}
}
