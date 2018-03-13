package com.pakotzy.poehelper;

import com.pakotzy.poehelper.event.Event;
import com.pakotzy.poehelper.feature.Feature;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@ConfigurationProperties
@Validated
public class SettingsProvider {
	private final Path configPath = Paths.get(System.getProperty("user.home") + "/Documents/POEHelper/settings.yml");

	@Valid
	private List<Feature> settings;

	public List<Feature> getSettings() {
		return settings;
	}

	public void setSettings(List<Feature> settings) {
		this.settings = settings;
	}

	public Feature getFeature(int i) {
		return settings.get(i);
	}

	public Event getEvent(int gId) {
		int[] cId = Utils.parseUId(PoeHelperApplication.eventHeap.get(gId));
		return getEvent(cId[0], cId[1]);
	}

	public Event getEvent(int fId, int eId) {
		return settings.get(fId).getEvent(eId);
	}

	@PostConstruct
	public void loadCustomConfig() throws IOException {
		if (Files.exists(configPath)) {
			try (BufferedReader reader = Files.newBufferedReader(configPath)) {
				Yaml yaml = new Yaml();
				settings = yaml.load(reader);
			}
		}
	}

	//	Save Events to local storage
	@PreDestroy
	public void saveSettings() throws IOException {
		if (Files.notExists(configPath)) {
			Files.createDirectories(configPath.getParent());
			Files.createFile(configPath);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
			Yaml yaml = new Yaml();
			writer.append(yaml.dumpAs(settings, Tag.SEQ, DumperOptions.FlowStyle.BLOCK));
		}
	}
}
