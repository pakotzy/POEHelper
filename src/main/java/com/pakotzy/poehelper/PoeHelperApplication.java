package com.pakotzy.poehelper;

import com.melloware.jintellitype.JIntellitype;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PreDestroy;
import java.io.IOException;

@SpringBootApplication
public class PoeHelperApplication extends AbstractJavaFxApplicationSupport {
	public static void main(String[] args) {
		// Check environment
		if (loadDll())
			launchApp(PoeHelperApplication.class, MainView.class, args);
	}

	private static boolean loadDll() {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource resource;

		if (System.getProperty("sun.arch.data.model").equals("x32")) {
			resource = resourceLoader.getResource("classpath:JIntellitype.dll");
		} else {
			resource = resourceLoader.getResource("classpath:JIntellitype64.dll");
		}

		try {
			JIntellitype.setLibraryInputStream(resource.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return JIntellitype.isJIntellitypeSupported() && !JIntellitype.checkInstanceAlreadyRunning("POEHelper");

	}

	@PreDestroy
	public void cleanUp() {
		JIntellitype.getInstance().cleanUp();
	}
}
