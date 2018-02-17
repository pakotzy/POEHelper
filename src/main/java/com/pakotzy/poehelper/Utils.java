package com.pakotzy.poehelper;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.scene.Node;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	private static final int MAX_TITLE_LENGTH = 20;
	private static final User32 user32 = User32.INSTANCE;

	//	Locate fields in theObject by key and type using Reflection API
	public static Node findField(Object theObject, String key, String type) {

		Class theClass = theObject.getClass();
		Field field = null;
		try {
			field = theClass.getDeclaredField(key + type);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		field.setAccessible(true);
		Node result = null;
		try {
			result = (VBox) field.get(theObject);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return result;
	}

	//	Convert shortcut modifiers to human readable String format
	public static String getShortcutString(KeyEvent keyEvent) {
		List<String> result = new ArrayList<>();

		if (keyEvent.isAltDown()) result.add("Alt");
		if (keyEvent.isControlDown()) result.add("Ctrl");
		if (keyEvent.isShiftDown()) result.add("Shift");

		String keyCode = keyEvent.getCode().getName();

		switch (keyCode) {
			case "Alt":
			case "Ctrl":
			case "Shift":
				break;
			default:
				result.add(keyCode);
		}

		return String.join("+", result);
	}

	public static String getWindowName(WinDef.HWND hwnd) {
		char[] buffer = new char[MAX_TITLE_LENGTH];
		user32.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);
		return Native.toString(buffer);
	}

	public static String writeToClipboard(String text) {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		String result = clipboard.getString();
		ClipboardContent content = new ClipboardContent();
		content.putString(text);
		clipboard.setContent(content);

//		TODO Make this stupid Clipboard to not block Robot events, so i can restore Clipboard contents

		return result;
	}
}
