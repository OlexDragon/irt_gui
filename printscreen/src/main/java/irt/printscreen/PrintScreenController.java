package irt.printscreen;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class PrintScreenController{

	private static final Logger logger = LogManager.getLogger();

	private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

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
						});
					}
					set = checkBoxCtrl.isSelected()==control && checkBoxShift.isSelected()==shift && checkBoxAlt.isSelected()==alt;
					if(stage.isFullScreen()!=set){
//						Platform.runLater(()->stage.setIconified(!set));
						Platform.runLater(()->{
							borderPane
							.getChildren()
							.parallelStream()
							.forEach(ch->ch.setVisible(!setTo));
							stage.setFullScreen(set);
							borderPane.setStyle(set ? "-fx-background-color: rgba(0, 100, 100, 0.1)" : "-fx-background-color: white");
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
					Platform.runLater(()->borderPane.getChildren().add(group));
				}
			}

			@Override
			public void nativeMouseReleased(NativeMouseEvent e) {
				if(set){
					Platform.runLater(()->borderPane.getChildren().remove(group));
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
	
	private void doPrintScreen(java.awt.Rectangle screenRect) {
		Platform.runLater(()->{
			stage.setFullScreen(false);
			stage.setIconified(true);
		});

		executor.execute(()->{
			try {

				Thread.sleep(1000);
				Robot robot = new Robot();
				BufferedImage capture = robot.createScreenCapture(screenRect);
				Path path = Paths.get(System.getProperty("user.home"), "Desktop", "PrintScreen", System.currentTimeMillis() + ".jpg");
				final File file = path.toFile();
				final File folder = file.getParentFile();
				if(!folder.exists() || folder.isFile())
					folder.mkdirs();
				ImageIO.write(capture, "jpg", file);

				Platform.runLater(()->stage.setIconified(false));

			} catch (Exception e) {
				logger.catching(e);
			}
		});
	}

    public void setStage(Stage stage) {
		this.stage = stage;
	}
}
