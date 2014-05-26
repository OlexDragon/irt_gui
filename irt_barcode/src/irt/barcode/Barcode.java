package irt.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Barcode {

	private String msg;
	private int width;
	private int height;

	public Barcode(String msg, int width, int height){
		this.msg = msg;
		this.width = width;
		this.height =height;
	}

	public Icon getIcon() throws WriterException {
		return new ImageIcon(getBufferedImage());
	}

	public BufferedImage getBufferedImage() throws WriterException{

        // Create the ByteMatrix for the QR-Code that encodes the given String
       Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
       hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
       QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix byteMatrix = qrCodeWriter.encode(msg, BarcodeFormat.QR_CODE, width, height, hintMap);

       // Make the BufferedImage that are to hold the QRCode
       int matrixWidth = byteMatrix.getWidth();
       int matrixHeight = byteMatrix.getHeight();

       BufferedImage bufferedImage = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_RGB);
       bufferedImage.createGraphics();

       Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
       graphics.setColor(Color.WHITE);
       graphics.fillRect(0, 0, matrixWidth, matrixHeight);
       // Paint and save the image using the ByteMatrix
       graphics.setColor(Color.BLACK);
 
       for (int i = 0; i < matrixWidth; i++) {
    	   for (int j = 0; j < matrixHeight; j++) {
    		   if (byteMatrix.get(i, j)) {
    			   graphics.fillRect(i, j, 1, 1);
    		   }
    	   }
       }

		return bufferedImage;
		
	}
}
