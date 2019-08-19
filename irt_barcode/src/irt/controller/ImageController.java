package irt.controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import irt.tools.MakerResizable;

public class ImageController {

//	private BufferedImage image;
//	private int imageWidht;
//	private int imageHeight;

	public ImageController(final JLabel lblSmallImage, final JPanel imagePanel) {

		new MakerResizable(imagePanel, 20);

//		try {
//			image = ImageIO.read(IrtBarcode.class.getResource("/irt/irt_gui/images/logo.gif"));
//		} catch (IOException e) {
//			JOptionPane.showMessageDialog(imagePanel, e.getLocalizedMessage());
//		}

		lblSmallImage.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if(lblSmallImage.getBackground().equals(Color.WHITE)){
					lblSmallImage.setBackground(new Color(25, 25, 112));
					removeImageLabel(mouseEvent);
				}else{
					lblSmallImage.setBackground(Color.WHITE);
//					addImage();
				}
			}

//			private void addImage() {
//
//				Dimension panelSize = imagePanel.getSize();
//				int imageSize = (int) Math.min(panelSize.getHeight(), panelSize.getWidth());
//				if(imageHeight==0 || imageWidht==0)
//					imageHeight = imageWidht = imageSize-6;
//
//				JPanel panel = new JPanel();
//				panel.setBounds(501, 71, 70, 73);
//				panel.setLayout(new BorderLayout(0, 0));
//				panel.setBounds( 3, 3, imageWidht, imageHeight);
//				panel.setBorder(new LineBorder(Color.WHITE, 1));
//
//				JLabel label = new JLabel();
//				label.setMinimumSize(new Dimension(10,10));
//				panel.add(label, image);
//
//				imagePanel.add(panel);
//				imagePanel.repaint();
//				new MakerResizable(panel, 10);
//
//				panel.addMouseListener(new MouseListener() {
//					
//					@Override public void mousePressed(MouseEvent arg0) { }
//					@Override public void mouseClicked(MouseEvent arg0) { }
//					@Override public void mouseReleased(MouseEvent arg0) { }
//
//					@Override
//					public void mouseExited(MouseEvent mouseEvent) {
//						JPanel panel = (JPanel) mouseEvent.getSource();
//						panel.setBorder(new LineBorder(Color.WHITE, 1));
//					}
//					
//					@Override
//					public void mouseEntered(MouseEvent mouseEvent) {
//						JPanel panel = (JPanel) mouseEvent.getSource();
//						panel.setBorder(new LineBorder(Color.BLACK, 1));
//					}
//				});
//
//				panel.addComponentListener(new ComponentListener() {
//					
//					@Override public void componentMoved(ComponentEvent arg0) { }
//					@Override public void componentHidden(ComponentEvent arg0) { }
//					@Override public void componentShown(ComponentEvent arg0) { }
//					@Override
//					public void componentResized(ComponentEvent componentEvent) {
//
//						JPanel panel = (JPanel) componentEvent.getSource();
//
//						imageHeight = panel.getHeight();
//						imageWidht = panel.getWidth();
//
////						putImage(panel, image);
//					}
//				});
//			}

			private void removeImageLabel(MouseEvent mouseEvent) {

				Component component = (Component) mouseEvent.getSource();
				imagePanel.remove(component);
				imagePanel.repaint();
			}
		});
	}

	public static void putImage(JLabel label, BufferedImage image){

		image = Scalr.resize(image, Mode.AUTOMATIC, label.getWidth(), label.getHeight());
		label.setIcon(new ImageIcon(image));
	}
}
