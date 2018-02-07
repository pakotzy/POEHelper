package com.pakotzy.poehelper;

import com.melloware.jintellitype.JIntellitype;
import com.sun.glass.ui.Application;
import com.sun.glass.ui.Robot;
import com.sun.jna.platform.win32.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class Runner {
	@Autowired
	private SettingsProvider settings;

	private Robot robot;
	private WindowSwitchHook hook;
	private Thread whTh;

	public Runner(){
		hook = new WindowSwitchHook();
		whTh = new Thread(hook);
		whTh.start();
	}

	public void run(int id) {
		if (robot == null) {
			robot = Application.GetApplication().createRobot();
		}
		Event event = settings.getEvent(id);
		System.out.println("Running: " + event.getAction());
		switch (event.getType()) {
			case "binder":
				binderRun(event);
				break;
			case "writer":
				writerRun(event);
				break;
			default:
				System.out.println("Sorry, not supported yet!");
		}
	}

	private void binderRun(Event event) {
		String[] keys = event.getAction().split("");
		for (String key : keys) {
			robot.keyPress(key.charAt(0));
			robot.keyRelease(key.charAt(0));
		}
	}

	private void writerRun(Event event) {

	}

	@PreDestroy
	public void cleanUp() {
		hook.stop();
		if (robot != null) {
			robot.destroy();
		}
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
			String windowClass = new String("wshWindow");
			WinDef.HMODULE hInst = kernel32.GetModuleHandle("");
			WinUser.WNDCLASSEX wClass = new WinUser.WNDCLASSEX();
			wClass.hInstance = hInst;
			wClass.lpfnWndProc = WindowSwitchHook.this;
			wClass.lpszClassName = windowClass;
			user32.RegisterClassEx(wClass);
			hwnd = user32.CreateWindowEx(0, windowClass, "Dummy message catcher", 0, 0, 0, 0, 0, null, null, hInst, null);

			if (!isHooked) {
				wsHook = user32.SetWinEventHook(3,3, kernel32.GetModuleHandle(null), wsHookP, 0, 0, 0);
				isHooked = true;
			}

			WinUser.MSG msg = new WinUser.MSG();
			while (user32.GetMessage(msg, hwnd, 0, 0) != 0) {}
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
				JIntellitype.getInstance().registerHotKey(i, settings.getEvent(i).getHotKey());
			}
		}

		private void unregisterHotkeys() {
			for (int i = 0; i < settings.getEventsSize(); i++) {
				JIntellitype.getInstance().unregisterHotKey(i);
			}
		}
	}
}
