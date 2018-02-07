package com.pakotzy.poehelper;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	private static final int MAX_TITLE_LENGTH = 20;
	private static final User32 user32 = User32.INSTANCE;

	//	Locate fields in theObject by key using Reflection API
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
		List result = new ArrayList<String>();

		if (keyEvent.isAltDown()) result.add("ALT");
		if (keyEvent.isControlDown()) result.add("CTRL");
		if (keyEvent.isShiftDown()) result.add("SHIFT");
		result.add(keyEvent.getText().toUpperCase());

		return String.join(" + ", result);
	}

	/**
	 * Check if window with given name is active
	 * @param windowName The name of the Window to look for
	 * @return true if window with given name is currently active
	 */
	public static boolean isActive(String windowName) {
		char[] buffer = new char[MAX_TITLE_LENGTH];
		WinDef.HWND hwnd = user32.GetForegroundWindow();
		user32.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);

		return Native.toString(buffer).equals(windowName);
	}

	/**
	 * Check if window with given windowName exists
	 * @param windowName
	 * @return true if window with given name exists
	 */
	public static boolean isExists(String windowName) {
		return !user32.EnumWindows((hWnd, arg1) -> {
			char[] buffer = new char[MAX_TITLE_LENGTH];
			user32.GetWindowText(hWnd, buffer, MAX_TITLE_LENGTH);

			return Native.toString(buffer).equals(windowName) ? false : true;
		}, null);
	}

	public static String getWindowName(WinDef.HWND hwnd) {
		char[] buffer = new char[MAX_TITLE_LENGTH];
		user32.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);
		return Native.toString(buffer);
	}
}
