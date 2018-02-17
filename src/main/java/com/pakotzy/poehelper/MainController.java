package com.pakotzy.poehelper;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@FXMLController
public class MainController implements HotkeyListener {
	// Root
	@FXML
	private Parent root;
	// Binder
	@FXML
	private TitledPane binderTitledPane;
	@FXML
	private VBox binderVBox;
	// Writer
	@FXML
	private TitledPane writerTitledPane;
	@FXML
	private VBox writerVBox;
	// Custom
	@FXML
	private TitledPane customTitledPane;
	@FXML
	private VBox customVBox;

	private SettingsProvider settings;
	private Runner runner;

	public MainController(SettingsProvider settings, Runner runner) {
		this.settings = settings;
		this.runner = runner;
	}

	// Populate UI fields with current values from the save file and register hot key handlers
	public void initialize() {
		JIntellitype.getInstance().addHotKeyListener(this);

		Event event;
		for (int i = 0; i < settings.getEventsSize(); i++) {
			event = settings.getEvent(i);
			VBox vBox = (VBox) Utils.findField(this, event.getType(), "VBox");
			showEvent(event, i, vBox);
		}

		binderTitledPane.expandedProperty().addListener(expandedChangeListener());
		writerTitledPane.expandedProperty().addListener(expandedChangeListener());
		customTitledPane.expandedProperty().addListener(expandedChangeListener());
	}

	private void showEvent(Event event, int i, VBox vBox) {
		HBox hBox = new HBox();
		hBox.setId(event.getType() + "HBox" + i);
		hBox.getStyleClass().add("hbox");

		TextField keyField = new TextField(event.getHotKey());
		keyField.setId(event.getType() + "KeyField" + i);
		keyField.setOnKeyPressed(this::onKeyPressed);
		keyField.setEditable(false);
		keyField.textProperty().addListener(keyChangeListener(event));

		TextField actionField = new TextField(event.getAction());
		actionField.setId(event.getType() + "ActionField" + i);
		actionField.textProperty().addListener(actionChangeListener(event));

		CheckBox enabledBox = new CheckBox();
		actionField.setId(event.getType() + "EnabledBox" + i);
		enabledBox.setIndeterminate(false);
		enabledBox.setSelected(event.getEnabled());
		enabledBox.selectedProperty().addListener(enabledChangeListener(event));

		Button removeButton = new Button("X");
		removeButton.setId(event.getType() + "RemoveButton" + i);
		removeButton.setOnMouseClicked(onRemoveCLick(event, vBox, hBox));

		hBox.getChildren().addAll(keyField, actionField, enabledBox, removeButton);
		vBox.getChildren().add(vBox.getChildren().size() - 1, hBox);
	}

	private ChangeListener<Boolean> expandedChangeListener() {
		return (obs, wasExpanded, isNowExpanded) -> Platform.runLater(this::resizeWindow);
	}

	private ChangeListener<String> keyChangeListener(Event event) {
		return (observable, oldValue, newValue) -> event.setHotKey(newValue);
	}

	private ChangeListener<String> actionChangeListener(Event event) {
		return (observable, oldValue, newValue) -> event.setAction(newValue);
	}

	private ChangeListener<Boolean> enabledChangeListener(Event event) {
		return (observable, oldValue, newValue) -> event.setEnabled(newValue);
	}

	private EventHandler<MouseEvent> onRemoveCLick(Event event, VBox vBox, HBox hBox) {
		return e -> {
			vBox.getChildren().remove(hBox);
			settings.getEvents().remove(event);
			resizeWindow();
		};
	}

	private void resizeWindow() {
		root.getScene().getWindow().sizeToScene();
	}

	private void onKeyPressed(KeyEvent keyEvent) {
		String combination = Utils.getShortcutString(keyEvent);

		if (keyEvent.getCode().getName().equals("Backspace"))
			combination = "";

		((TextField) keyEvent.getSource()).setText(combination);
	}

	public void onAddClick(MouseEvent mouseEvent) {
		Button button = (Button) mouseEvent.getSource();
		String key = button.getId().substring(0, 6);
		VBox vBox = (VBox) Utils.findField(this, key, "VBox");
		Event event = new Event();
		event.setType(key);
		int id = settings.addEvent(event);
		showEvent(event, id, vBox);
		resizeWindow();
	}

	@Override
	public void onHotKey(int hkIdentifier) {
		runner.run(hkIdentifier);
	}
}
