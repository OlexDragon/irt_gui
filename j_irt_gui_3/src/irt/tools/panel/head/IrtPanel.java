package irt.tools.panel.head;

import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

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

	private static Map<String, Integer> fontStyle;

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

		ImageIcon imageIcon = new ImageIcon(
				IrtGui.class.getResource(
						properties.get("company_logo_"+companyIndex).toString()));


		final JLabel lblIrtTechnologies = new ImageLabel(imageIcon, text);
		lblIrtTechnologies.setHorizontalAlignment(SwingConstants.RIGHT);
		lblIrtTechnologies.setForeground(new Color(176, 224, 230));
		add(lblIrtTechnologies);

		final JLabel lblText = new JLabel(text);
		lblText.setBounds(68, 13, 166, 20);
		lblText.setHorizontalAlignment(SwingConstants.LEFT);
		lblText.setForeground(new Color(0x3B, 0x4A, 0x8B));
		add(lblText);
		
		final JLabel lblShadow = new JLabel(text);
		lblShadow.setHorizontalAlignment(SwingConstants.LEFT);
		lblShadow.setForeground(Color.WHITE);
		lblShadow.setBounds(lblText.getX()-1, lblText.getY()-1, 166, 20);
		add(lblShadow);

		new SwingWorker<Rectangle, Void>() {
			@Override
			protected Rectangle doInBackground() throws Exception {
				Thread.currentThread().setName("IrtPanel.lblIrtTechnologies.setBounds");
				String[] split = properties.get("logo_bounds_"+companyIndex).toString().split(",");
				return new Rectangle(Integer.parseInt(split[0]),
						Integer.parseInt(split[1]),
						Integer.parseInt(split[2]),
						Integer.parseInt(split[3]));
			}

			@Override
			protected void done() {
				try {
					lblIrtTechnologies.setBounds(get());
				} catch (InterruptedException | ExecutionException e) {
					logger.catching(e);
				}
			}
		}.execute();

		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				Thread.currentThread().setName("IrtPanel.setFont");
				return new Font(properties.getProperty("font_name_"+companyIndex),
						parseFontStyle((String) properties.get("font_style_"+companyIndex)),
						Integer.parseInt(properties.get("font_size_"+companyIndex).toString()));
			}

			@Override
			protected void done() {
				try {

					Font font = get();
					lblIrtTechnologies.setFont(font);
					lblText.setFont(font);
					lblShadow.setFont(font);

				} catch (InterruptedException | ExecutionException e) {
					logger.catching(e);
				}
			}
		}.execute();

	}
}
