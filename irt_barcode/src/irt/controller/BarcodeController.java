package irt.controller;

import irt.barcode.Barcode;
import irt.data.JFileChooserWork;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

public class BarcodeController {

	private JLabel lblBarcode;
	private JTextArea textArea;
	private JButton btnCreatBarcode;
	private JButton btnSave;

	private Barcode barcode;
	private JPanel owner;

	public BarcodeController(JPanel owner, JTextArea textArea, JLabel lblBarcode, JButton btnCreatBarcode, JButton btnSave){

		this.owner = owner;
		this.textArea = textArea;
		this.lblBarcode = lblBarcode;
		this.btnCreatBarcode =btnCreatBarcode;
		this.btnCreatBarcode.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent arg0) { show(); } });
		this.btnSave = btnSave;
		this.btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Preferences prefs = Preferences.userRoot().node("IrtBarcode");

				String path = prefs.get("path", "");
				@SuppressWarnings("serial")
				JFileChooser fileChooser = new JFileChooser(){
				    @Override
				    public void approveSelection(){
				        File f = getSelectedFile();
				        if(f.exists() && getDialogType() == SAVE_DIALOG){
				            switch(JOptionPane.showConfirmDialog(this,"The File '"+f.getName()+"' already exists.\nDo you want to replace it?", "Existing file", JOptionPane.YES_NO_OPTION)){
				                case JOptionPane.YES_OPTION:
				                    super.approveSelection();
				            }
				        }
				    }
				};

				fileChooser.setSelectedFile(new File(path+File.separator+BarcodeController.this.textArea.getText().replaceAll("\\s", " ").split(" ")[0]));
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.addPropertyChangeListener(JFileChooserWork.PROPERTY_CHANGE_LISTENER_FILE_FILTER_CHANGED);
				fileChooser.addChoosableFileFilter(JFileChooserWork.PNG);
				fileChooser.addChoosableFileFilter(JFileChooserWork.JPG);

				if (fileChooser.showSaveDialog(BarcodeController.this.owner) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					FileFilter fileFilter = fileChooser.getFileFilter();
					String p = file.getParentFile().getPath();

					if(!p.equals(path))
						prefs.put("path", p);

					try {
						ImageIO.write(barcode.getBufferedImage(), fileFilter.toString().substring(1), file);
					} catch (IOException | com.google.zxing.WriterException e1) {
						JOptionPane.showMessageDialog(null, e1.getLocalizedMessage());
					}
				}
			}
		});
	}

	protected void show() {
		String text = BarcodeController.this.textArea.getText();
		if(text==null || text.isEmpty())
			JOptionPane.showMessageDialog(null, "Type the Text.");
		else{
			barcode = new Barcode(text, BarcodeController.this.lblBarcode.getWidth(), BarcodeController.this.lblBarcode.getHeight());
			try {
				Icon icon = barcode.getIcon();
				BarcodeController.this.lblBarcode.setIcon(icon);
			} catch (com.google.zxing.WriterException e) {
				JOptionPane.showMessageDialog(owner, e.getLocalizedMessage());
				e.printStackTrace();
			}
			btnSave.setEnabled(true);
		}
	}
}
