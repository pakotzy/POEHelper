package com.pakotzy.poehelper;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
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
			HBox hBox = new HBox();
			hBox.getStyleClass().add("hbox");

			TextField keyField = new TextField(event.getHotKey());
			keyField.setId(event.getType() + i + "KeyField");
			keyField.setOnKeyPressed(this::onKeyPressed);
			keyField.setEditable(false);
			keyField.textProperty().addListener(keyChangeListener(event));

			TextField actionField = new TextField(event.getAction());
			actionField.setId(event.getType() + i + "ActionField");
			actionField.textProperty().addListener(actionChangeListener(event));

			CheckBox enabledBox = new CheckBox();
			actionField.setId(event.getType() + i + "EnabledBox");
			enabledBox.setIndeterminate(false);
			enabledBox.setSelected(event.getEnabled());
			enabledBox.selectedProperty().addListener(enabledChangeListener(event));

			hBox.getChildren().addAll(keyField, actionField, enabledBox);
			vBox.getChildren().add(hBox);
		}

		binderTitledPane.expandedProperty().addListener(expandedChangeListener());
		writerTitledPane.expandedProperty().addListener(expandedChangeListener());
		customTitledPane.expandedProperty().addListener(expandedChangeListener());
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

	private ChangeListener<Boolean> expandedChangeListener() {
		return (obs, wasExpanded, isNowExpanded) -> Platform.runLater(() -> root.getScene().getWindow().sizeToScene());
	}

	public void onKeyPressed(KeyEvent keyEvent) {
		String combination = Utils.getShortcutString(keyEvent);
		((TextField) keyEvent.getSource()).setText(combination);
	}

	@Override
	public void onHotKey(int hkIdentifier) {
		runner.run(hkIdentifier);
	}
}
