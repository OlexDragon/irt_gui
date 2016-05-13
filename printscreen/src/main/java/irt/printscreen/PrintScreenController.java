package irt.printscreen;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseMotionListener;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class PrintScreenController{

	private static final Logger logger = LogManager.getLogger();
	private static final Preferences prefs = Preferences.userRoot().node(PrintScreenController.class.getSimpleName());

	private final ExecutorService executor = Executors.newFixedThreadPool(5, new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			int priority = t.getPriority();
			if(priority>Thread.MIN_PRIORITY)
				t.setPriority(--priority);
			t.setDaemon(true);
			return t;
		}

	});

	@FXML private BorderPane borderPane;
	@FXML private CheckBox checkBoxCtrl;
	@FXML private CheckBox checkBoxShift;
	@FXML private CheckBox checkBoxAlt;
	private final  Group group = new Group();
	private final Rectangle rectangle = new Rectangle();

	private boolean shift;
	private boolean control;
	private boolean alt;
	private Stage stage;
	private volatile boolean set;
	private int x1, x2, y1, y2;
	private volatile boolean busy;

    @FXML public void initialize() {
    	rectangle.setFill(Color.color(0, 0, 0, 0.1));
		group.getChildren().add(rectangle);
		try {
			GlobalScreen.registerNativeHook();

			GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
				
				public synchronized void nativeKeyPressed(NativeKeyEvent e) {
					set(e, true);
				}
				public synchronized void nativeKeyReleased(NativeKeyEvent e) {
					set(e, false);
				}
				private void set(NativeKeyEvent e, boolean setTo) {
					switch(e.getKeyCode()){
					case NativeKeyEvent.VC_SHIFT_L:
					case NativeKeyEvent.VC_SHIFT_R:
						shift = setTo;
						break;
					case NativeKeyEvent.VC_CONTROL_L:
					case NativeKeyEvent.VC_CONTROL_R:
						control = setTo;
						break;
					case NativeKeyEvent.VC_ALT_L:
					case NativeKeyEvent.VC_ALT_R:
						alt = setTo;
						break;
					case NativeKeyEvent.VC_ESCAPE:
						Platform.runLater(()->{
							
							stage.setFullScreen(false);
							stage.setWidth(100);
							stage.setHeight(100);
							stage.setIconified(true);
							set = false;
							borderPane.getChildren().remove(group);
						});
					}
					set = checkBoxCtrl.isSelected()==control && checkBoxShift.isSelected()==shift && checkBoxAlt.isSelected()==alt;
					boolean setFillScreen = set && !busy;
					if(stage.isFullScreen()!=setFillScreen){

						Platform.runLater(()->{
							final ObservableList<Node> children = borderPane.getChildren();
							children
							.parallelStream()
							.forEach(ch->ch.setVisible(!setTo));
							stage.setFullScreen(setFillScreen);
							if(setFillScreen){
								if(!children.contains(group))
									children.add(group);
								borderPane.setStyle("-fx-background-color: rgba(0, 100, 100, 0.1)");
							}else{
								children.remove(group);
								borderPane.setStyle("-fx-background-color: white");
							}
						});
					}
				}
				public void nativeKeyTyped(NativeKeyEvent e) {}
			});
		} catch (NativeHookException e1) {
			logger.catching(e1);
		}

		GlobalScreen.addNativeMouseListener(new NativeMouseListener() {
			
			@Override
			public void nativeMousePressed(NativeMouseEvent e) {
				if(set){
					x1 = e.getX();
					y1 = e.getY();
					rectangle.setX(x1);
					rectangle.setY(y1);
					rectangle.setHeight(0);
					rectangle.setWidth(0);
				}
			}

			@Override
			public void nativeMouseReleased(NativeMouseEvent e) {
				if(set){
					set = false;
					x2 = e.getX();
					y2 = e.getY();
					if(x1!=x2 && y1!=y2){
						if(x1>x2){
							int tmp = x1;
							x1 = x2;
							x2 = tmp;
						}
						if(y1>y2){
							int tmp = y1;
							y1 = y2;
							y2 = tmp;
						}
						java.awt.Rectangle screenRect = new java.awt.Rectangle(x1, y1, x2-x1, y2-y1);
						doPrintScreen(screenRect);
					}
				}
			}

			@Override
			public void nativeMouseClicked(NativeMouseEvent e) {}
		});

		GlobalScreen.addNativeMouseMotionListener(new NativeMouseMotionListener() {
			
			@Override
			public void nativeMouseMoved(NativeMouseEvent e) {
			}
			
			@Override
			public void nativeMouseDragged(NativeMouseEvent e) {
				if(set){
					int x = x1;
					int y = y1;
					int eX = e.getX();
					int eY = e.getY();
					if(x>eX){
						int tmp = x;
						x = eX;
						eX = tmp;
					}
					if(y>eY){
						int tmp = y;
						y = eY;
						eY = tmp;
					}

					rectangle.setX(x);
					rectangle.setY(y);
					rectangle.setWidth(eX - x);
					rectangle.setHeight(eY - y);
				}
			}
		});
    }

    @FXML private void onActionFullScreen(){
    	java.awt.Rectangle screenRect = new java.awt.Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		doPrintScreen(screenRect);
    }

	private double x, y;
    @FXML void onMousePressed(MouseEvent event) {
		x = stage.getX() - event.getScreenX();
		y = stage.getY() - event.getScreenY();

	}

    @FXML
    void onMouseDragged(MouseEvent event) {
    	stage.setX(event.getScreenX() + x);
        stage.setY(event.getScreenY() + y);
    }

    @FXML
    void onMouseReleased(MouseEvent event) {
       	prefs.putDouble("psx", event.getScreenX());
       	prefs.putDouble("psy", event.getScreenY());
    }
	
	private void doPrintScreen(java.awt.Rectangle screenRect) {
		if(busy)
			return;

		busy = true;
		Platform.runLater(()->{
			stage.setFullScreen(false);
			stage.setIconified(true);

			borderPane.setStyle("-fx-background-color: white");
			final ObservableList<Node> children = borderPane.getChildren();
			children.remove(group);
			children
			.parallelStream()
			.forEach(ch->ch.setVisible(true));
		});

		executor.execute(()->{

			try{

				Thread.sleep(1000);
				Robot robot = new Robot();
				BufferedImage capture = robot.createScreenCapture(screenRect);
				Platform.runLater(()->stage.setIconified(false));

				showAlert(capture);


			}catch(Exception ex){
				logger.catching(ex);
			}


		});
	}

	private void showAlert(BufferedImage capture) {

		WritableImage image = SwingFXUtils.toFXImage(capture, null);
		final ImageView imageView = new ImageView(image);
		
		if(image.getWidth()>image.getHeight())
			imageView.setFitWidth(400);
		else
			imageView.setFitHeight(300);

		imageView.setPreserveRatio(true);
		imageView.setSmooth(true);
		imageView.setCache(true);

		final ButtonType buttonTypeSave = new ButtonType("Save");
		final ButtonType buttonTypePrint = new ButtonType("Print");
		final ButtonType buttonTypeEMail = new ButtonType("E-Mail");

		Platform.runLater(()->{
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.initOwner(borderPane.getScene().getWindow());
			alert.setTitle("PrintScreen");
			alert.setHeaderText("Choose your option.");

			alert.getDialogPane().setContent(imageView);

			alert.getButtonTypes().setAll(buttonTypeSave, buttonTypePrint, buttonTypeEMail, new ButtonType("Cancel", ButtonData.CANCEL_CLOSE));

			alert.showAndWait()
			.ifPresent(b->{
				executor.execute(()->{

					if(b==buttonTypeSave)
						save(capture);
					else if (b==buttonTypePrint)
						print(image);
					else if(b==buttonTypeEMail)
						eMail(capture);
					else
						busy = false;
				});
			});
		});
	}

	private void save(BufferedImage capture) {

		final Path path = Paths.get(prefs.get("path", System.getProperty("user.dir")));
		final String ext = prefs.get("ext", "JPG");

		List<ExtensionFilter> efs = new ArrayList<>();
		efs.add(new ExtensionFilter("JPG", "*.jpg"));
		efs.add(new ExtensionFilter("PNG", "*.png"));
		efs.add(new ExtensionFilter("BMP", "*.bmp"));
		efs.add(new ExtensionFilter("GIF", "*.gif"));

		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.setInitialDirectory(path.toFile());
        fileChooser.setInitialFileName(Long.toString(System.currentTimeMillis()));
        fileChooser.getExtensionFilters().addAll(efs);
        fileChooser.setSelectedExtensionFilter(efs.parallelStream().filter(ef->ef.getDescription().equals(ext)).findAny().get());

        Optional
        .ofNullable(fileChooser.showSaveDialog(stage))
        .ifPresent(f->{
        	prefs.put("path", f.getParentFile().getAbsolutePath());
        	final ExtensionFilter sef = fileChooser.getSelectedExtensionFilter();
			prefs.put("ext", sef.getDescription());

			try {
				ImageIO.write(capture, sef.getDescription(), f);
			} catch (Exception e) {
				busy = false;
				logger.catching(e);
			}
        });
		busy = false;
	}

	private void print(WritableImage image) {
		Optional
		.ofNullable(PrinterJob.createPrinterJob())
		.ifPresent(j->{
			if(j.showPrintDialog(borderPane.getScene().getWindow())){

				ImageView iv = new ImageView(image);
				if(j.printPage(iv))
					j.endJob();
			}
		});
		busy = false;
	}

	private void eMail(BufferedImage capture) {
		try {
			Path mailtoPath = getMailto();
//
//			String str = prefs.get("outlook", null);
//			if(str==null){
//				File f = new FileChooser().showOpenDialog(stage);
//				if(f==null)
//					return;
//				str = f.getAbsolutePath();
//				prefs.put("outlook", str);
//			}

			Path attachmentPath = saveImage(capture);
			openNewMail(mailtoPath, attachmentPath);

		} catch (Exception e) {
			busy = false;
			logger.catching(e);
		}
		busy = false;
	}

	private void openNewMail(Path mailtoPath, Path attachmentPath) throws IOException {
		final ProcessBuilder processBuilder = new ProcessBuilder(mailtoPath.toString(), "/C", "ipm.note", "/a", attachmentPath.toString());
//		logger.error("{}", processBuilder.command());
		final Process process = processBuilder.start();

       	String line;
        InputStream inputStream = process.getInputStream();
		InputStreamReader is = new InputStreamReader(inputStream);
        try(BufferedReader br = new BufferedReader(is)){
        	while ((line = br.readLine()) != null) 
        		logger.debug(line);
        }

		inputStream = process.getErrorStream();
		is = new InputStreamReader(inputStream);
        try(BufferedReader br = new BufferedReader(is)){
        	while ((line = br.readLine()) != null) 
        		logger.error(line);
        }
	}

	private Path saveImage(BufferedImage capture) throws IOException {
		Path attachmentPath = Paths.get(System.getProperty("user.home"), "Desktop", "PrintScreen", System.currentTimeMillis() + ".jpg");
		final File file = attachmentPath.toFile();
		final File folder = file.getParentFile();
		if(!folder.exists() || folder.isFile())
			folder.mkdirs();
		ImageIO.write(capture, "JPG", file);
		return attachmentPath;
	}
	private Path getMailto() throws IOException {
		Process process = new ProcessBuilder("cmd.exe", "/C", "ftype", "mailto").start();
		InputStreamReader is = new InputStreamReader(process.getInputStream());
        BufferedReader br = new BufferedReader(is);
        String line;
        Map<String, Path> properties = new HashMap<>();
        while ((line = br.readLine()) != null) {
			final String[] split = line.split("=");
			if(split.length>1){
				final String[] tmp = split[1].split("\"");
				if(tmp.length>3)
					properties.put(split[0].toLowerCase(), Paths.get(tmp[1]));
			}
        }
		return properties.get("mailto");
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}
}
