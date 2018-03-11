package com.pakotzy.poehelper;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Utils {
	private static final int MAX_TITLE_LENGTH = 20;
	private static final int RELEASED = 0x0002; // Keyboard button released event flag
	private static final int SCANCODE = 0x0008; // Keyboard button code provided py scancode flag

	private static final User32 user32 = User32.INSTANCE;
	private static final Map<Character, Integer> spKeys = new HashMap<>();

	static {
		spKeys.put('+', 0x4E); // Num+
		spKeys.put('-', 0x4A); // Num-
	}

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
		if (keyEvent.isControlDown()) result.add("Control");
		if (keyEvent.isShiftDown()) result.add("Shift");

		String keyCode = keyEvent.getCode().getName();

		switch (keyCode) {
			case "Alt":
			case "Control":
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

	public static int[] parseUId(String uId) {
		String[] cId = uId.split(":");
		int[] result = new int[2];
		result[0] = Integer.parseInt(cId[0]);
		result[1] = Integer.parseInt(cId[1]);
		return result;
	}

	public static void writeToClipboard(String text) {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		content.putString(text);
		clipboard.setContent(content);
		//		TODO Make this stupid Clipboard to not block Robot events, so i can restore Clipboard contents
	}

	public static String readFromClipboard() {
		return Clipboard.getSystemClipboard().getString();
	}

	public static <T> T executeOnEventThread(Supplier<T> fn) {
		AtomicReference<T> result = new AtomicReference<>();
		Platform.runLater(() -> result.set(fn.get()));
		while (result.compareAndSet(null, null)) ;
		return result.get();
	}

	public static void executeOnEventThread(Runnable fn) {
		Platform.runLater(fn);
	}

	public static void lowLevelKeyboardDown(Character c) {
		WinUser.INPUT input = new WinUser.INPUT();
		input.type = new WinUser.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
		input.input.setType("ki");
		if (spKeys.containsKey(c)) {
			input.input.ki.wScan = new WinUser.WORD(spKeys.get(c));
			input.input.ki.dwFlags = new WinDef.DWORD(SCANCODE);
			WinDef.DWORD inserted = user32.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1),
					input.size());
			//			System.out.printf((inserted.intValue() == 1 ? "\nlowLevelKeyboardDown - '%c'" :
			// "\nlowLevelKeyboardDown " +
			//					"-" +
			//					" " +
			//					"'%c' " +
			//					"unsuccessful"), c);
		} else {
			System.out.println("No such special key");
		}
	}

	public static void lowLevelKeyboardUp(Character c) {
		WinUser.INPUT input = new WinUser.INPUT();
		input.type = new WinUser.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
		input.input.setType("ki");
		if (spKeys.containsKey(c)) {
			input.input.ki.wScan = new WinUser.WORD(spKeys.get(c));
			input.input.ki.dwFlags = new WinDef.DWORD(SCANCODE | RELEASED);
			WinDef.DWORD inserted = user32.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1),
					input.size());
			//			System.out.printf((inserted.intValue() == 1 ? "\nlowLevelKeyboardUp - '%c'" :
			// "\nlowLevelKeyboardUp - " +
			//					"'%c'" +
			//					" " +
			//					"unsuccessful"), c);
		} else {
			System.out.println("No such special key");
		}
	}

	public static short getKeyState(int vKeyCode) {
		return user32.GetAsyncKeyState(vKeyCode);
	}
}
