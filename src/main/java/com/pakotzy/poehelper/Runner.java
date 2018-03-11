package com.pakotzy.poehelper;

import com.melloware.jintellitype.JIntellitype;
import com.pakotzy.poehelper.event.Event;
import com.sun.glass.ui.Application;
import com.sun.glass.ui.Robot;
import com.sun.jna.platform.win32.*;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class Runner {
	public static final AtomicReference<Robot> ROBOT = new AtomicReference<>();

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final Map<Integer, Future<?>> tasks = new HashMap<>();
	private final WindowSwitchHook hook = new WindowSwitchHook();

	private SettingsProvider settings;
	private Thread whTh;

	public Runner(SettingsProvider settings) {
		whTh = new Thread(hook);
		whTh.start();
		this.settings = settings;
	}

	public void run(int id) {
		if (ROBOT.get() == null) {
			Utils.executeOnEventThread(() -> ROBOT.set(Application.GetApplication().createRobot()));
		}

		try {
			Class keyCode = Class.forName("java.awt.event.KeyEvent");
			Event event = settings.getEvent(id);
			String[] mods = event.getHotKey().split("\\+");
			for (String mod : mods) {
				Field field = keyCode.getDeclaredField("VK_" + mod.toUpperCase());
				Integer code = (Integer) field.get(keyCode);
				Utils.executeOnEventThread(() -> ROBOT.get().keyRelease(code));
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Future<?> future = tasks.get(id);

		int[] cId = Utils.parseUId(PoeHelperApplication.eventHeap.get(id));
		//		System.out.printf("Run - %d = %d -> %d\n", id, cId[0], cId[1]);

		//	Check if task is already running, if so interrupt and call stop method
		if (future != null && future.cancel(true)) {
			future = executor.submit(() -> settings.getFeature(cId[0]).stop(cId[1]));
		} else {
			future = executor.submit(() -> settings.getFeature(cId[0]).run(cId[1]));
		}
		tasks.put(id, future);
	}

	@PreDestroy
	public void cleanUp() {
		hook.stop();
		if (ROBOT != null) {
			ROBOT.get().destroy();
		}
		executor.shutdownNow();
		whTh.interrupt();
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
			for (int i = 0; i < PoeHelperApplication.eventHeap.size(); i++) {
				JIntellitype.getInstance().registerHotKey(i, settings.getEvent(i).getHotKey());
			}
		}

		private void unregisterHotkeys() {
			for (int i = 0; i < PoeHelperApplication.eventHeap.size(); i++) {
				JIntellitype.getInstance().unregisterHotKey(i);
			}
		}
	}
}
