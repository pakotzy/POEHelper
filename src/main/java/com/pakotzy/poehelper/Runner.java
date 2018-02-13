package com.pakotzy.poehelper;

import com.melloware.jintellitype.JIntellitype;
import com.sun.glass.ui.Application;
import com.sun.glass.ui.Robot;
import com.sun.jna.platform.win32.*;
import javafx.application.Platform;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
public class Runner {
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final WindowSwitchHook hook = new WindowSwitchHook();

	private SettingsProvider settings;

	private Robot robot;
	private Thread whTh;
	private Map<Integer, Future<?>> tasks = new HashMap<>();

	public Runner(SettingsProvider settings) {
		this.settings = settings;
		whTh = new Thread(hook);
		whTh.start();
	}

	public void run(int id) {
		if (robot == null) {
			Platform.runLater(() -> robot = Application.GetApplication().createRobot());
		}

		Platform.runLater(() -> robot.keyRelease(KeyEvent.VK_SHIFT));

		Event event = settings.getEvent(id);

//		Check if task is already running, if so interrupt
		Future<?> future = tasks.get(id);
		if (future != null && future.cancel(true)) {
			return;
		}

		switch (event.getType()) {
			case "binder":
				future = executor.submit(() -> binderRun(event));
				break;
			case "writer":
				writerRun(event);
				break;
			case "custom":
				customRun(event);
				break;
			default:
				System.out.println("Sorry, not supported yet!");
		}
		tasks.put(id, future);
	}

	private void binderRun(Event event) {
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
			Platform.runLater(() -> {
				for (char key : keys) {
					robot.keyPress(Character.toUpperCase(key));
					robot.keyRelease(Character.toUpperCase(key));
				}
			});
			try {
				TimeUnit.SECONDS.sleep(delay);
			} catch (InterruptedException e) {
				break;
			}
		} while (delay != 0);
	}

	private void writerRun(Event event) {
		Platform.runLater(() -> {
			Utils.writeToClipboard(event.getAction());

			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);

			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);

			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
		});

//		char[] keys = event.getAction().toCharArray();
//
//		Platform.runLater(() -> {
//			robot.keyPress(KeyEvent.VK_ENTER);
//			robot.keyRelease(KeyEvent.VK_ENTER);
//
//			for (char key : keys) {
//				Character buffer = Utils.isSpecial(key);
//				if (Character.isUpperCase(key) || buffer != null) {
//					if (buffer != null) {
//						key = buffer;
//					}
//					robot.keyPress(KeyEvent.VK_SHIFT);
//				}
//
//				robot.keyPress(KeyEvent.getExtendedKeyCodeForChar(key));
//				robot.keyRelease(KeyEvent.getExtendedKeyCodeForChar(key));
//
//				if (Character.isUpperCase(key) || buffer != null) {
//					robot.keyRelease(KeyEvent.VK_SHIFT);
//				}
//			}
//
//			robot.keyPress(KeyEvent.VK_ENTER);
//			robot.keyRelease(KeyEvent.VK_ENTER);
//		});
	}

	private void customRun(Event event) {
	}

	@PreDestroy
	public void cleanUp() {
		hook.stop();
		if (robot != null) {
			robot.destroy();
		}
		executor.shutdownNow();
		whTh.interrupt();
		whTh = null;
	}

	private class WindowSwitchHook implements WinUser.WindowProc, Runnable {

		private final User32 user32 = User32.INSTANCE;
		private final Kernel32 kernel32 = Kernel32.INSTANCE;

		private WinNT.HANDLE wsHook;
		private WinDef.HWND hwnd;

		private boolean isHooked = false;
		private boolean wStatus = false;

		private final WinUser.WinEventProc wsHookP = (handle, event, hwnd, aLong, aLong1, dword1, dword2) -> {
			String name = Utils.getWindowName(hwnd);
			if (name.equals("Path of Exile")) {
				if (!wStatus) {
					registerHotkeys();
					wStatus = true;
				}
			} else {
				if (wStatus) {
					unregisterHotkeys();
					wStatus = false;
				}
			}
		};

		@Override
		public void run() {
			String windowClass = "wshWindow";
			WinDef.HMODULE hInst = kernel32.GetModuleHandle("");
			WinUser.WNDCLASSEX wClass = new WinUser.WNDCLASSEX();
			wClass.hInstance = hInst;
			wClass.lpfnWndProc = WindowSwitchHook.this;
			wClass.lpszClassName = windowClass;
			user32.RegisterClassEx(wClass);
			hwnd = user32.CreateWindowEx(0, windowClass, "Dummy message catcher", 0, 0, 0, 0, 0, null, null, hInst,
					null);

			if (!isHooked) {
				wsHook = user32.SetWinEventHook(3, 3, kernel32.GetModuleHandle(null), wsHookP, 0, 0, 0);
				isHooked = true;
			}

			WinUser.MSG msg = new WinUser.MSG();
			while (user32.GetMessage(msg, hwnd, 0, 0) != 0) ;
			user32.UnregisterClass(windowClass, hInst);
			user32.DestroyWindow(hwnd);
		}

		public WinDef.LRESULT callback(WinDef.HWND hwnd, int uMsg, WinDef.WPARAM wParam, WinDef.LPARAM lParam) {
			switch (uMsg) {
				case WinUser.WM_DESTROY: {
					User32.INSTANCE.PostQuitMessage(0);
					return new WinDef.LRESULT(0);
				}
				default:
					return User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
			}
		}

		public void stop() {
			if (isHooked) {
				user32.UnhookWinEvent(wsHook);
				isHooked = false;
			}
			user32.SendMessage(hwnd, WinUser.WM_DESTROY, null, null);
		}

		private void registerHotkeys() {
			for (int i = 0; i < settings.getEventsSize(); i++) {
				if (settings.getEvent(i).getEnabled())
					JIntellitype.getInstance().registerHotKey(i, settings.getEvent(i).getHotKey());
			}
		}

		private void unregisterHotkeys() {
			for (int i = 0; i < settings.getEventsSize(); i++) {
				if (settings.getEvent(i).getEnabled())
					JIntellitype.getInstance().unregisterHotKey(i);
			}
		}
	}
}
