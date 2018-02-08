package com.pakotzy.poehelper;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
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
	private Map<String, TextField> nodes = new HashMap<>();

	// Root
	@FXML
	private Parent root;

	// Binder
	@FXML
	private TitledPane binderTitledPane;
	@FXML
	private VBox binderVBox;

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
			nodes.put(keyField.getId(), keyField);

			TextField actionField = new TextField(event.getAction());
			actionField.setId(event.getType() + i + "ActionField");
			nodes.put(actionField.getId(), actionField);

			hBox.getChildren().addAll(keyField, actionField);
			vBox.getChildren().add(hBox);
		}

		binderTitledPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
			Platform.runLater(() -> root.getScene().getWindow().sizeToScene());
		});
	}

	public void onKeyPressed(KeyEvent keyEvent) {
		String combination = Utils.getShortcutString(keyEvent);
		((TextField) keyEvent.getSource()).setText(combination);
	}

	public void onSaveClick() {
		Event event;
		TextField key;
		TextField action;
		for (int i = 0; i < settings.getEventsSize(); i++) {
			event = settings.getEvent(i);
			key = nodes.get(event.getType() + i + "KeyField");
			action = nodes.get(event.getType() + i + "ActionField");

			event.setHotKey(key.getText());
			event.setAction(action.getText().toUpperCase());
		}
	}

	@Override
	public void onHotKey(int hkIdentifier) {
		runner.run(hkIdentifier);
	}
}
