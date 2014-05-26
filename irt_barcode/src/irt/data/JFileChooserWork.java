package irt.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class JFileChooserWork {

	public static final FileFilter JPG = new FileFilter() {
		@Override
		public String getDescription() {
			return "JPEG (*.jpg)";
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			} else {
				return f.getName().toLowerCase().endsWith(".jpg");
			}
		}
		@Override
		public String toString() {		
			return ".jpg";
		}
	};

	public static final FileFilter PNG = new FileFilter() {
		@Override
		public String getDescription() {
			return "PNG (*.png)";
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			} else {
				return f.getName().toLowerCase().endsWith(".png");
			}
		}
		@Override
		public String toString() {		
			return ".png";
		}
	};

	public static final PropertyChangeListener PROPERTY_CHANGE_LISTENER_FILE_FILTER_CHANGED = new PropertyChangeListener() {
		
		private File file;

		@Override
		public void propertyChange(PropertyChangeEvent evt) {

			JFileChooser fileChooser = (JFileChooser) evt.getSource();
			Object newValue = evt.getNewValue();
			Object oldValue = evt.getOldValue();
			String propertyName = evt.getPropertyName();

			if(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(propertyName)){
				if(newValue==null)
					file = (File)oldValue;
				else{
					File addedExtension = addExtension(fileChooser, fileChooser.getFileFilter(), (File)newValue);
					if(addedExtension!=newValue)
						fileChooser.setSelectedFile(addedExtension);
				}
			}else if(JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(propertyName)){

				if (newValue != null) {
					File f = fileChooser.getSelectedFile();

					if(f==null && file!=null)
						f = file;

					if(f!=null){
						File addedExtension = addExtension(fileChooser, (FileFilter)newValue, f);
						if(addedExtension!=f)
							fileChooser.setSelectedFile(addedExtension);
						else if(f==file)
							fileChooser.setSelectedFile(f);
					}
				}
			}
		}

		protected File addExtension(JFileChooser fileChooser, FileFilter fileFilter, File f) {

			String name = f.getName();

			boolean isDirectory = f.isDirectory();
			if (isDirectory || name == null){

				name = "Untitled"+fileFilter;
				f = new File(isDirectory ? f : f.getParentFile(), name);

			}else {
				if(!name.endsWith(fileChooser.getFileFilter().toString())){
					int index = name.lastIndexOf(".");
					if (index > 0)
						name = name.substring(0, index);
					name += fileFilter;
					f = new File(isDirectory ? f : f.getParentFile(), name);
				}
			}

			return f;
		}
	};
}
