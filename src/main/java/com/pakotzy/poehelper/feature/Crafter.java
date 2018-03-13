package com.pakotzy.poehelper.feature;

import com.pakotzy.poehelper.Runner;
import com.pakotzy.poehelper.Utils;
import com.pakotzy.poehelper.event.CrafterEvent;
import com.sun.glass.ui.Robot;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crafter extends Feature {
	/*
	 * Number of links
	 * (?:\s|^)[RGB](?:[-][RGB]){1}(?=\s|$)
	 *
	 * Number of sockets
	 * ^[RGB](?:[ -][RGB]){5}[ $]
	 *
	 * Needed colors
	 *^(?=(?:.*?R){1})(?=(?:.*?G){1})(?=(?:.*?B){2}).{1,11}$
	 * */

	@Override
	public TitledPane draw() {
		TitledPane titledPane = new TitledPane();
		titledPane.setId("crafterTitledPane");
		titledPane.setAnimated(false);
		titledPane.setExpanded(false);
		titledPane.setText("Crafter");

		VBox vBox = new VBox();
		vBox.setId("vBox");

		CrafterEvent event;
		for (int i = 0; i < getEvents().size(); i++) {
			event = (CrafterEvent) getEvent(i);
			if (event.isEnabled())
				bind(getFeatureId(), i);
			vBox.getChildren().add(build(event, i));
		}

		Button addButton = new Button("Add");
		addButton.setId("addButton");
		addButton.setOnMouseClicked(event1 -> {
			CrafterEvent nEvent = new CrafterEvent();
			int id = addEvent(nEvent);
			vBox.getChildren().add(vBox.getChildren().size() - 1, build(nEvent, id));
			Platform.runLater(() -> vBox.getScene().getWindow().sizeToScene());
		});

		vBox.getChildren().add(addButton);

		titledPane.setContent(vBox);
		return titledPane;
	}

	private TitledPane build(CrafterEvent event, int id) {
		TitledPane titledPane = new TitledPane();
		titledPane.setId("tritledPane" + id);
		titledPane.setAnimated(false);
		titledPane.setExpanded(false);
		titledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
			Utils.executeOnEventThread(() -> titledPane.getScene().getWindow().sizeToScene());
		});

//		Build header
		StringBuilder style = new StringBuilder();
		style.append(event.getSockets() > 0 ? event.getSockets() + "s; " : "");
		style.append(event.getLinks() > 0 ? event.getLinks() + "l; " : "");
		style.append(event.getR() > 0 ? event.getR() + "R; " : "");
		style.append(event.getG() > 0 ? event.getG() + "G; " : "");
		style.append(event.getB() > 0 ? event.getB() + "B; " : "");
		style.append(event.getMods().size() > 0 ? event.getMods().size() + "M" : "");
		titledPane.setText(style.toString());

//		Main controller
		VBox vBox = new VBox();
		vBox.setId("vBox" + id);

//		Controls
		HBox controlsHBox = new HBox();
		controlsHBox.setId("controlsHBox" + id);

		TextField keyField = new TextField(event.getHotKey());
		keyField.setId("keyField" + id);
		keyField.setOnKeyPressed(event1 -> {
			String combination = Utils.getShortcutString(event1);

			if (event1.getCode().getName().equals("Backspace"))
				combination = "";

			((TextField) event1.getSource()).setText(combination);
		});
		keyField.setEditable(false);
		keyField.textProperty().addListener((observable, oldValue, newValue) -> {
			event.setHotKey(newValue);
		});

		CheckBox enabledBox = new CheckBox();
		enabledBox.setId("enabledBox" + id);
		enabledBox.setIndeterminate(false);
		enabledBox.setSelected(event.isEnabled());
		enabledBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			event.setEnabled(newValue);
			if (newValue) {
				bind(getFeatureId(), id);
			} else {
				unbind(getFeatureId(), id);
			}
		});

		Button removeButton = new Button("X");
		removeButton.setId("removeButton" + id);
		removeButton.setOnMouseClicked(event1 -> {
			getEvents().remove(event);
			unbind(getFeatureId(), id);
			VBox parent = (VBox) titledPane.getParent();
			parent.getChildren().remove(titledPane);
			Platform.runLater(() -> parent.getScene().getWindow().sizeToScene());
		});

		controlsHBox.getChildren().addAll(keyField, enabledBox, removeButton);

//		Sockets + Links
		HBox socketsHBox = new HBox();
		socketsHBox.setId("socketsHBox" + id);

		TextField socketsFiled = new TextField(event.getSockets() == 0 ? "" : event.getSockets().toString());
		socketsFiled.setId("socketsField" + id);
		socketsFiled.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.matches("^[0-6]?$")) {
				socketsFiled.setText(newValue);
				try {
					event.setSockets(Integer.parseInt(newValue));
				} catch (NumberFormatException e) {
					event.setSockets(0);
				}
			} else {
				socketsFiled.setText(oldValue);
			}
		});

		TextField linksField = new TextField(event.getLinks() == 0 ? "" : event.getLinks().toString());
		linksField.setId("linksField" + id);
		linksField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.matches("^[0-6]?$")) {
				linksField.setText(newValue);
				try {
					event.setLinks(Integer.parseInt(newValue));
				} catch (NumberFormatException e) {
					event.setLinks(0);
				}
			} else {
				linksField.setText(oldValue);
			}
		});

		socketsHBox.getChildren().addAll(socketsFiled, linksField);

//		Colors
		HBox colorsHBox = new HBox();
		colorsHBox.setId("colorsHBox" + id);

		TextField rField = new TextField(event.getR() == 0 ? "" : event.getR().toString());
		rField.setId("rField" + id);
		rField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.matches("^[0-6]?$")) {
				rField.setText(newValue);
				try {
					event.setR(Integer.parseInt(newValue));

					if (event.getColorsSum() > 6) {
						rField.setText(oldValue);
						event.setR(Integer.parseInt(oldValue));
					}
				} catch (NumberFormatException e) {
					event.setR(0);
				}
			} else {
				rField.setText(oldValue);
			}
		});
		TextField gField = new TextField(event.getG() == 0 ? "" : event.getG().toString());
		gField.setId("gField" + id);
		gField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.matches("^[0-6]?$")) {
				gField.setText(newValue);
				try {
					event.setG(Integer.parseInt(newValue));

					if (event.getColorsSum() > 6) {
						gField.setText(oldValue);
						event.setG(Integer.parseInt(oldValue));
					}
				} catch (NumberFormatException e) {
					event.setG(0);
				}
			} else {
				gField.setText(oldValue);
			}
		});
		TextField bField = new TextField(event.getB() == 0 ? "" : event.getB().toString());
		bField.setId("bField" + id);
		bField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.matches("^[0-6]?$")) {
				bField.setText(newValue);
				try {
					event.setB(Integer.parseInt(newValue));

					if (event.getColorsSum() > 6) {
						bField.setText(oldValue);
						event.setB(Integer.parseInt(oldValue));
					}
				} catch (NumberFormatException e) {
					event.setB(0);
				}
			} else {
				bField.setText(oldValue);
			}
		});
		colorsHBox.getChildren().addAll(rField, gField, bField);

		vBox.getChildren().addAll(controlsHBox, socketsHBox, colorsHBox);

//		Mods
		for (int i = 0; i < event.getMods().size(); i++) {
			HBox modHBox = buildMod(event, i);
			modHBox.setId("modHBox" + id);
			vBox.getChildren().add(buildMod(event, i));
		}

//		Add
		Button addButton = new Button("Add modifier");
		addButton.setId("addButton" + id);
		addButton.setOnMouseClicked(event1 -> {
			int nModId = event.addMod("");
			vBox.getChildren().add(vBox.getChildren().size() - 1, buildMod(event, nModId));
			Platform.runLater(() -> vBox.getScene().getWindow().sizeToScene());
		});

		vBox.getChildren().add(addButton);

		titledPane.setContent(vBox);
		return titledPane;
	}

	private HBox buildMod(CrafterEvent event, int i) {
		HBox modHBox = new HBox();

		TextField modField = new TextField(event.getMod(i));
		modField.setId("modField" + i);
		modField.textProperty().addListener((observable, oldValue, newValue) -> event.setMod(i, newValue));

		Button removeButton = new Button("X");
		removeButton.setId("removeModButton" + i);
		removeButton.setOnMouseClicked(event1 -> {
			VBox parent = (VBox) modHBox.getParent();
			parent.getChildren().remove(modHBox);
			event.getMods().remove(i);
			Platform.runLater(() -> parent.getScene().getWindow().sizeToScene());
		});

		modHBox.getChildren().addAll(modField, removeButton);
		return modHBox;
	}

	@Override
	public void run(Integer id) {
		CrafterEvent event = (CrafterEvent) getEvent(id);

//		Compile patterns
		ArrayList<Pattern> patterns = new ArrayList<>();
//		Find mods
//		if ()
//		Find line with sockets
		patterns.add(Pattern.compile("(?:Sockets: )((?:[RGB][ -]?){0,6})", Pattern.MULTILINE));
//		Find sockets
		if (event.getSockets() > 0)
			patterns.add(Pattern.compile(String.format("(^[RGB](?:[ -][RGB]){%d,}[ $])", event.getSockets() - 1)));
//		Find links
		if (event.getLinks() > 0) {
			patterns.add(Pattern.compile(String.format("((?:\\s|^)[RGB](?:[-][RGB]){%d,}(?=\\s|$))",
					event.getLinks() - 1)));
		}
//		Find colors
		if (event.getColorsSum() > 0 && event.getColorsSum() <= 6)
			patterns.add(Pattern.compile(String.format("(^(?=(?:.*?R){%d,})(?=(?:.*?G){%d,})(?=(?:.*?B){%d,}).{1," +
							"11}$)",
					event.getR(), event.getG(), event.getB())));

		Utils.executeOnEventThread(() -> Utils.writeToClipboard("Starting writer"));
		Utils.executeOnEventThread(() -> Runner.ROBOT.get().keyPress(KeyEvent.VK_SHIFT));

		while (true) {
			String oldItem = Utils.executeOnEventThread(Utils::readFromClipboard);

			String item = oldItem;
			while (item.equals(oldItem)) {
				item = Utils.executeOnEventThread(() -> {
					Runner.ROBOT.get().keyPress(KeyEvent.VK_CONTROL);
					Runner.ROBOT.get().keyPress(KeyEvent.VK_C);
					Runner.ROBOT.get().keyRelease(KeyEvent.VK_C);
					Runner.ROBOT.get().keyRelease(KeyEvent.VK_CONTROL);

					return Utils.readFromClipboard();
				});

				try {
					TimeUnit.MILLISECONDS.sleep(10);
					// If shift is not pressed, stop
					short shiftState = Utils.getKeyState(0x10);
					if (shiftState == 1 || shiftState == 0) {
						throw new InterruptedException("Shift is not pressed");
					}
				} catch (InterruptedException e) {
					System.out.println("Interrupting");
					return;
				}
			}

			if (matchAll(item, patterns)) {
				System.out.println("Complete");
				break;
			}

			Utils.executeOnEventThread(() -> {
				Runner.ROBOT.get().mousePress(Robot.MOUSE_LEFT_BTN);
				Runner.ROBOT.get().mouseRelease(Robot.MOUSE_LEFT_BTN);
			});
		}

		Utils.executeOnEventThread(() -> Runner.ROBOT.get().keyRelease(KeyEvent.VK_SHIFT));
	}

	private boolean matchAll(String item, ArrayList<Pattern> patterns) {
		boolean result = false;
		ArrayList<String> matches = new ArrayList<>();
		matches.add(item);

		for (Pattern p : patterns) {
			ArrayList<String> tempMatches = new ArrayList<>();
			for (String line : matches) {
				for (String sMatch : matchSingle(line, p)) {
					tempMatches.add(sMatch);
				}
			}
			if (tempMatches.size() > 0) {
				result = true;
				matches = tempMatches;
			} else {
				result = false;
			}
		}
		return result;
	}

	private ArrayList<String> matchSingle(String line, Pattern pattern) {
		ArrayList<String> result = new ArrayList<>();
		Matcher m = pattern.matcher(line);

		while (m.find()) {
			result.add(m.group());
		}

		return result;
	}

	@Override
	public void stop(Integer id) {
		Utils.executeOnEventThread(() -> Runner.ROBOT.get().keyRelease(KeyEvent.VK_SHIFT));
	}
}
