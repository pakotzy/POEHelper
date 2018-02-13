package com.pakotzy.poehelper;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@FXMLController
public class MainController implements HotkeyListener {
	@Autowired
	private SettingsProvider settings;
	@Autowired
	private Runner runner;
	private Map<String, Control> nodes = new HashMap<>();

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
			nodes.put(keyField.getId(), keyField);

			TextField actionField = new TextField(event.getAction());
			actionField.setId(event.getType() + i + "ActionField");
			actionField.textProperty().addListener(actionChangeListener(event));
			nodes.put(actionField.getId(), actionField);

			CheckBox enabledBox = new CheckBox();
			actionField.setId(event.getType() + i + "EnabledBox");
			enabledBox.setIndeterminate(false);
			enabledBox.setSelected(event.getEnabled());
			enabledBox.selectedProperty().addListener(enabledChangeListener(event));
			nodes.put(enabledBox.getId(), enabledBox);

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

	public void onSaveClick() {
		Event event;
		TextField key;
		TextField action;
		CheckBox enabled;
		for (int i = 0; i < settings.getEventsSize(); i++) {
			event = settings.getEvent(i);
			key = (TextField) nodes.get(event.getType() + i + "KeyField");
			action = (TextField) nodes.get(event.getType() + i + "ActionField");
			enabled = (CheckBox) nodes.get(event.getType() + i + "EnabledBox");

			event.setHotKey(key.getText());
			event.setAction(action.getText());
			event.setEnabled(enabled.isSelected());
		}
	}

	@Override
	public void onHotKey(int hkIdentifier) {
		runner.run(hkIdentifier);
	}
}
