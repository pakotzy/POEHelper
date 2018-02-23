package com.pakotzy.poehelper;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

@FXMLController
public class MainController implements HotkeyListener {
	// Root
	@FXML
	private VBox root;

	private SettingsProvider settings;
	private Runner runner;

	public MainController(SettingsProvider settings, Runner runner) {
		this.settings = settings;
		this.runner = runner;
	}

	// Populate UI fields with current values from the save file and register hot key handlers
	public void initialize() {
		JIntellitype.getInstance().addHotKeyListener(this);

		for (int i = 0; i < settings.getSettings().size(); i++) {
			settings.getFeature(i).setFeatureId(i);
			TitledPane pane = settings.getFeature(i).draw();
			pane.expandedProperty().addListener(observable -> Platform.runLater(() -> root.getScene().getWindow()
					.sizeToScene()));
			root.getChildren().add(pane);
		}

		Platform.runLater(() -> root.getScene().getWindow().sizeToScene());
	}

	@Override
	public void onHotKey(int hkIdentifier) {
		runner.run(hkIdentifier);
	}
}
