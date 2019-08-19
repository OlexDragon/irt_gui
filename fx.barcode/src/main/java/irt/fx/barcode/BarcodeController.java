package irt.fx.barcode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class BarcodeController extends AnchorPane{
	private final static Logger logger		 = LogManager.getLogger();
	private final static Preferences prefs	 = Preferences.userNodeForPackage(BarcodeController.class);

    private Timer timer;

    @FXML private ChoiceBox<BarcodeFormat> chbBarcodeFormat;
    @FXML private TextField tfWidth;
    @FXML private TextField tfHeight;
	@FXML private TextField tfToConvert;
    @FXML private ImageView imageView;
    @FXML private Button btnSave;
    @FXML private Slider zoomSlider;

    @FXML void initialize() {
    	
//		imageView.fitWidthProperty().bind(widthProperty());
//		imageView.fitHeightProperty().bind(heightProperty());

		ObservableList<BarcodeFormat> items = FXCollections.observableArrayList(BarcodeFormat.values());
		chbBarcodeFormat.setItems(items);
		final SingleSelectionModel<BarcodeFormat> selectionModel = chbBarcodeFormat.getSelectionModel();
		final String key = "format";
		selectionModel.select(prefs.getInt(key, 11));
		ReadOnlyIntegerProperty indexProperties = selectionModel.selectedIndexProperty();
		indexProperties.addListener((o,ov,nv)->createBarcode());
		indexProperties.addListener((o,ov,nv)->prefs.putInt(key, nv.intValue()));

		final ChangeListener<? super String> listener = (o,ov,nv)->createBarcode();

		final StringProperty textProperty = tfToConvert.textProperty();
		textProperty.addListener(listener);
		textProperty.addListener((o,ov,nv)->btnSave.setDisable(nv.isEmpty()));

		tfWidth.textProperty().addListener(listener);
		tfWidth.setText(prefs.get("width", "200"));
		tfWidth.focusedProperty().addListener(
				(o,ov,nv)->
				Optional.of(tfWidth.getText().replaceAll("\\D", ""))
				.filter(text->!text.isEmpty())
				.map(Integer::parseInt)
				.ifPresent(width->prefs.putInt("width", width)));

		tfHeight.textProperty().addListener(listener);
		tfHeight.setText(prefs.get("height", "200"));
		tfHeight.focusedProperty().addListener(
				(o,ov,nv)->
				Optional.of(tfWidth.getText().replaceAll("\\D", ""))
				.filter(text->!text.isEmpty())
				.map(Integer::parseInt)
				.ifPresent(height->prefs.putInt("height", height)));

		zoomSlider.setMin(10);
		zoomSlider.setMax(500);
		zoomSlider.setValue(prefs.getInt("height", 200));
		zoomSlider.valueProperty().addListener(
				(o,ov,nv)->{
					final String newValue = Integer.toString(nv.intValue());
					tfHeight.setText(newValue);
					tfWidth.setText(newValue); });
    }

    @FXML void onSave() {

    	try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Barcode");
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("PNG", "*.png"),
					new FileChooser.ExtensionFilter("CSV", "*.csv"));

			Optional.ofNullable(prefs.get("path", null)).map(path->Paths.get(path)).map(Path::toFile).ifPresent(fileChooser::setInitialDirectory);
			fileChooser.setInitialFileName(tfToConvert.getText().trim());

			Optional.ofNullable(
					fileChooser
					.showSaveDialog(btnSave.getScene().getWindow()))
			.ifPresent(
					file->{
						prefs.put("path", file.getParentFile().getAbsolutePath());

						if(file.getName().toLowerCase().endsWith(".png"))
							saveImage(file);
						else if(file.getName().toLowerCase().endsWith(".csv"))
							saveCsv(file);
					});
		} catch (Exception e) {
			logger.catching(e);
		}
    	
    }

	private void saveCsv(File file) {
		try {

			getBitMatrix()
			.ifPresent(
					bitMatrix->{
						final int column = bitMatrix.getWidth();
						final int rows = bitMatrix.getHeight();

						final String csv = IntStream.range(0, rows)

								.mapToObj(
										roeIndex->
										IntStream.range(0, column)
										.filter(columnIndex->bitMatrix.get(columnIndex, roeIndex))
										.mapToObj(Integer::toString)
										.collect(Collectors.joining(",")))
								.filter(line->!line.isEmpty())
								.collect(Collectors.joining("\n"));

						try {
							Files.write(file.toPath(), csv.getBytes());
						} catch (IOException e) {
							logger.catching(e);
						}
					});

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void saveImage(File file) {
		try {

			getBitMatrix()
			.ifPresent(
					bitMatrix->{
						try {

							MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file.toPath());

						} catch (IOException e) {
							logger.catching(e);
						}
					});

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public void createBarcode() {
		Optional.ofNullable(timer).ifPresent(t->t.cancel());
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {

				try {
					getBitMatrix()
					.ifPresent(
							bitMatrix->{
							
								try {
									ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
									MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
//									BufferedImage bufferedImage = new BufferedImage(Integer.parseInt(width), Integer.parseInt(height), BufferedImage.TYPE_INT_RGB);
									InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
									imageView.setImage(SwingFXUtils.toFXImage(ImageIO.read(inputStream), null));

								} catch (IllegalArgumentException e) {

									logger.catching(e);
									showAlert(e, null);
									btnSave.setDisable(true);

								} catch (ArrayIndexOutOfBoundsException e) {
									logger.catching(e);
									showAlert(e, "Only numbers are allowed");
									btnSave.setDisable(true);

								} catch (Exception e) {
									logger.catching(e);
									btnSave.setDisable(true);
								}
							});
				} catch (IllegalArgumentException e) {

					logger.catching(e);
					showAlert(e, null);
					btnSave.setDisable(true);

				} catch (ArrayIndexOutOfBoundsException e) {
					logger.catching(e);
					showAlert(e, "Only numbers are allowed");
					btnSave.setDisable(true);

				} catch (Exception e) {
					logger.catching(e);
					btnSave.setDisable(true);
				}
			}

			public void showAlert(Exception e, String contentText) {
				Platform.runLater(
						()->{
							final Alert alert = new Alert(AlertType.ERROR);
							alert.initOwner(tfHeight.getScene().getWindow());
							alert.setTitle(Optional.ofNullable(contentText).orElse(e.getClass().getSimpleName()));
							alert.setContentText(e.getLocalizedMessage());
							alert.showAndWait();
						});
			}
		}, 500);
	}

	public Optional<BitMatrix> getBitMatrix() throws WriterException {

		final String width = tfWidth.getText().replaceAll("\\D", "");
		final String height = tfHeight.getText().replaceAll("\\D", "");
		final SingleSelectionModel<BarcodeFormat> selectionModel = chbBarcodeFormat.getSelectionModel();
		final int selectedIndex = selectionModel.getSelectedIndex();
		final String text = tfToConvert.getText();

		if(width.isEmpty() || height.isEmpty() || selectedIndex<0 || tfToConvert.getText().trim().isEmpty())
			return Optional.empty();

		final Map<EncodeHintType, String> hints = new HashMap<>();
		hints.put(EncodeHintType.MARGIN, "0");

		final BarcodeFormat barcodeFormat = selectionModel.getSelectedItem();
		final Writer codeWriter = new MultiFormatWriter();
		final BitMatrix bitMatrix = codeWriter.encode(text, barcodeFormat, Integer.parseInt(width), Integer.parseInt(height), hints);
		return Optional.of(bitMatrix);
	}
}
