package com.pakotzy.poehelper.feature;

import com.pakotzy.poehelper.Runner;
import com.pakotzy.poehelper.Utils;
import com.pakotzy.poehelper.event.Event;
import com.pakotzy.poehelper.event.GenericEvent;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.concurrent.TimeUnit;

public class Binder extends Feature {

	@Override
	public TitledPane draw() {
		TitledPane titledPane = new TitledPane();
		titledPane.setId("binderTitledPane");
		titledPane.setAnimated(false);
		titledPane.setExpanded(false);
		titledPane.setText("Binder");

		VBox vBox = new VBox();
		vBox.setId("vBox");

		GenericEvent event;
		for (int i = 0; i < getEvents().size(); i++) {
			event = (GenericEvent) getEvent(i);
			if (event.isEnabled())
				bind(getFeatureId(), i);
			vBox.getChildren().add(build(event, i));
		}

		Button addButton = new Button("Add");
		addButton.setId("addButton");
		addButton.setOnMouseClicked(this::onAddClick);

		vBox.getChildren().add(addButton);
		titledPane.setContent(vBox);

		return titledPane;
	}

	private HBox build(GenericEvent event, int id) {
		HBox hBox = new HBox();
		hBox.setId("hBox" + id);
		hBox.getStyleClass().add("hbox");

		TextField keyField = new TextField(event.getHotKey());
		keyField.setId("keyField" + id);
		keyField.setOnKeyPressed(this::onKeyPressed);
		keyField.setEditable(false);
		keyField.textProperty().addListener(keyChangeListener(event));

		TextField actionField = new TextField(event.getAction());
		actionField.setId("actionField" + id);
		actionField.textProperty().addListener(actionChangeListener(event));

		CheckBox enabledBox = new CheckBox();
		actionField.setId("enabledBox" + id);
		enabledBox.setIndeterminate(false);
		enabledBox.setSelected(event.isEnabled());
		enabledBox.selectedProperty().addListener(enabledChangeListener(event, id));

		Button removeButton = new Button("X");
		removeButton.setId("removeButton" + id);
		removeButton.setOnMouseClicked(onRemoveCLick(event, hBox, id));

		hBox.getChildren().addAll(keyField, actionField, enabledBox, removeButton);
		return hBox;
	}

	private ChangeListener<String> keyChangeListener(GenericEvent event) {
		return (observable, oldValue, newValue) -> event.setHotKey(newValue);
	}

	private ChangeListener<String> actionChangeListener(GenericEvent event) {
		return (observable, oldValue, newValue) -> event.setAction(newValue);
	}

	private ChangeListener<Boolean> enabledChangeListener(GenericEvent event, Integer id) {
		return (observable, oldValue, newValue) -> {
			event.setEnabled(newValue);
			if (newValue) {
				bind(getFeatureId(), id);
			} else {
				unbind(getFeatureId(), id);
			}
		};
	}

	private EventHandler<MouseEvent> onRemoveCLick(Event event, HBox hBox, Integer id) {
		return e -> {
			VBox parent = (VBox) hBox.getParent();
			parent.getChildren().remove(hBox);
			getEvents().remove(event);
			unbind(getFeatureId(), id);
			Platform.runLater(() -> parent.getScene().getWindow().sizeToScene());
		};
	}

	private void onKeyPressed(KeyEvent keyEvent) {
		String combination = Utils.getShortcutString(keyEvent);

		if (keyEvent.getCode().getName().equals("Backspace"))
			combination = "";

		((TextField) keyEvent.getSource()).setText(combination);
	}

	private void onAddClick(MouseEvent mouseEvent) {
		Button button = (Button) mouseEvent.getSource();
		VBox vBox = (VBox) button.getParent();
		GenericEvent event = new GenericEvent();
		int id = addEvent(event);
		vBox.getChildren().add(vBox.getChildren().size() - 1, build(event, id));
		Platform.runLater(() -> vBox.getScene().getWindow().sizeToScene());
	}

	@Override
	public void run(Integer id) {
		GenericEvent event = (GenericEvent) getEvent(id);

		int delay = 0;
		char[] keys;
		int pos = event.getAction().indexOf(";");
		if (pos != -1) {
			delay = Integer.parseInt(event.getAction().substring(pos + 1));
			keys = event.getAction().substring(0, pos).toCharArray();
		} else {
			keys = event.getAction().toCharArray();
		}

		do {
			if (keys[0] == '+' || keys[0] == '-') {
				Utils.lowLevelKeyboardDown(keys[0]);
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException ignored) {

				} finally {
					Utils.lowLevelKeyboardUp(keys[0]);
				}
			} else {
				Utils.executeOnEventThread(() -> {
					for (char key : keys) {
						Runner.ROBOT.get().keyPress(Character.toUpperCase(key));
						Runner.ROBOT.get().keyRelease(Character.toUpperCase(key));
					}
				});
			}

			try {
				TimeUnit.SECONDS.sleep(delay);
			} catch (InterruptedException e) {
				break;
			}
		} while (delay != 0);
	}

	@Override
	public void stop(Integer id) {
		GenericEvent event = (GenericEvent) getEvent(id);
		String action = event.getAction();

		if (action.length() == 1)
			Utils.lowLevelKeyboardUp(action.charAt(0));
	}

	@Override
	public String toString() {
		return "Binder {\n" +
				getEvents()
				+ "\n}";
	}
}
