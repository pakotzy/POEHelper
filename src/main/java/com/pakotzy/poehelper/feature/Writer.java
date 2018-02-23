package com.pakotzy.poehelper.feature;

import com.pakotzy.poehelper.Runner;
import com.pakotzy.poehelper.Utils;
import com.pakotzy.poehelper.event.GenericEvent;
import javafx.application.Platform;
import javafx.scene.control.TitledPane;

import java.awt.event.KeyEvent;

public class Writer extends Binder {
	@Override
	public TitledPane draw() {
		TitledPane result = super.draw();
		result.setId("writerTitledPane");
		return result;
	}

	@Override
	public void run(Integer id) {
		GenericEvent event = (GenericEvent) getEvent(id);

		Platform.runLater(() -> {
			Utils.writeToClipboard(event.getAction());

			Runner.ROBOT.get().keyPress(KeyEvent.VK_ENTER);
			Runner.ROBOT.get().keyRelease(KeyEvent.VK_ENTER);

			Runner.ROBOT.get().keyPress(KeyEvent.VK_CONTROL);
			Runner.ROBOT.get().keyPress(KeyEvent.VK_V);
			Runner.ROBOT.get().keyRelease(KeyEvent.VK_V);
			Runner.ROBOT.get().keyRelease(KeyEvent.VK_CONTROL);

			Runner.ROBOT.get().keyPress(KeyEvent.VK_ENTER);
			Runner.ROBOT.get().keyRelease(KeyEvent.VK_ENTER);
		});
	}

	@Override
	public String toString() {
		return "Writer {\n" +
				getEvents()
				+ "\n}";
	}
}
