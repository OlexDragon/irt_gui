package irt.tools.fx.update.profile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.ThreadWorker;
import irt.tools.fx.update.profile.ProfileTables.Table;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;

public class EditTablesMessageFx extends Alert {
	private final static Logger logger = LogManager.getLogger();

	private static final ButtonData NEXT_OR_SAVE = ButtonData.APPLY;
	private static final ButtonData IGNORE = ButtonData.OK_DONE;

	private static Action action;

	public EditTablesMessageFx(final Profile profile, final List<Table> tablesWithError) {
		super(AlertType.ERROR);

		setTitle("The profile \"" + profile.getFileName() + "\" has " + tablesWithError.size() + " Errors.");
		setHeaderText("CORRECT, IGNORE profile errors, or CANCEL the update.");
		setResizable(true);
		initModality(Modality.APPLICATION_MODAL);

		TextArea textArea = new TextArea();
		getDialogPane().setContent(textArea);

		ButtonType buttonTypeSave = new ButtonType("Next", NEXT_OR_SAVE);
		ButtonType buttonTypeIgnore = new ButtonType("Ignore", IGNORE);

		getButtonTypes().setAll(buttonTypeSave, buttonTypeIgnore, ButtonType.CANCEL);

		final Button saveButton = (Button) getDialogPane().lookupButton(buttonTypeSave);
		saveButton.setDisable(true);

		Timer timer = new Timer(true);
		AtomicReference<TableChecker> task = new AtomicReference<>();
		resultProperty().set(ButtonType.NO);

		final ChangeListener<? super String> textChangeListener = (o,ov,nv)->{
			saveButton.setDisable(true);
			setAlertType(AlertType.WARNING);
			final TableChecker t = new TableChecker(nv, saveButton);
			Optional.ofNullable(task.getAndSet(t)).ifPresent(TimerTask::cancel);
			timer.schedule(t, 3000);
		};

		textArea.textProperty().addListener(textChangeListener);

		// Show alert message
		ThreadWorker.runThread(
				()->{

					Map<String, String> map = new HashMap<>();
					final Table lastTable = tablesWithError.get(tablesWithError.size()-1);

					tablesWithError
					.forEach(
							table->{

								logger.debug("\n{}", table);

								// Return after clicking the cancel button
								if(resultProperty().get() == ButtonType.CANCEL)
									return;

								// Reset alert message
								Platform.runLater(
										()->{
											saveButton.setDisable(true);
											setAlertType(AlertType.ERROR);
											resultProperty().set(ButtonType.CANCEL);
										});

								try {

									// Get table string from the profile
									final String t = profile.getTable(table.getKey()).getKey();

									// Show alert
									FutureTask<Optional<ButtonType>> ft = new FutureTask<>(

											()->{

												// If this is the last or only one table, change the text of the Next button to Save.
												if(table == lastTable)
													saveButton.setText("Save");

												// Show table in the TextArea
												final StringProperty textProperty = textArea.textProperty();
												textProperty.removeListener(textChangeListener);
												textArea.setText(t);
												textProperty.addListener(textChangeListener);

												return showAndWait();
											});

									Platform.runLater(ft);

									// Response to user action
									try {

										ft.get().map(ButtonType::getButtonData)
										.ifPresent(
												bd->{

													logger.debug(bd);

													// Cancel button
													if(bd==ButtonData.CANCEL_CLOSE) {
														map.clear();
														map.put(table.getKey(), null);
														return;

													// Ignore button
													}else if(bd==IGNORE)
														map.put(table.getKey(), "");

													// Next/Save button
													else if(bd==NEXT_OR_SAVE)
														map.put(table.getKey(), textArea.getText());
												});

									} catch (InterruptedException | ExecutionException e) { logger.catching(Level.DEBUG, e); }

								} catch (IOException e) {
									logger.catching(e);
								}
							});

					// Do nothing if canceled
					if(map.isEmpty() || map.entrySet().parallelStream().filter(es->es.getValue()==null).findAny().isPresent()) {
						action = Action.CANCEL;
						return;
					}
					
					try {

						// Update and Save profile
						profile.updateAndSave(map);

						action = Action.CONTINUE;

					} catch (IOException e) {
						logger.catching(e);
						action = Action.CANCEL;
					}

				}, "EditTableMessageFx");
	}

	public class TableChecker extends TimerTask{

		private String text;
		private Node buttonToEnable;

		public TableChecker(String text, Node buttonToEnable) {
			this.text = text;
			this.buttonToEnable = buttonToEnable;
		}

		@Override
		public void run() {

			synchronized (EditTablesMessageFx.class) {

				ProfileTables.clear();
				try (Scanner scanner = new Scanner(text)) {

					while (scanner.hasNextLine()) {
						final String trim = scanner.nextLine();
						ProfileTables.add(trim);
					}

					final boolean noError = ProfileTables.getTablesWithError().isEmpty();
					buttonToEnable.setDisable(!noError);
					Platform.runLater(()->setAlertType(noError ? AlertType.CONFIRMATION : AlertType.ERROR));
				}
			}
		}
	}

	public static Callable<Action> getMessageTask(Profile profile, List<Table> tablesWithError) {

		action = null;
		Platform.runLater(()->new EditTablesMessageFx(profile, tablesWithError));

		return ()->{
			while(action == null) {
				Thread.sleep(1000);
			}
			return action;
		};
	}

	public enum Action{
		CANCEL,
		CONTINUE
	}
}
