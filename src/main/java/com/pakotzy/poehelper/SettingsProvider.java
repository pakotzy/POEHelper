package com.pakotzy.poehelper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties
public class SettingsProvider {
	private final Path configPath = Paths.get(System.getProperty("user.home") + "/Documents/POEHelper/settings.yml");

	private List<Event> events = new ArrayList<>();

	public List<Event> getEvents() {
		return events;
	}

	public Event getEvent(int eventId) {
		return events.get(eventId);
	}

	public int getEventsSize() {
		return events.size();
	}

	public SettingsProvider(List<Event> events) {}

	@PostConstruct
	public void loadCustomConfig() {
		if (Files.exists(configPath)) {
			try (BufferedReader reader = Files.newBufferedReader(configPath)) {
				Yaml yaml = new Yaml();
				events = yaml.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
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
			writer.append(yaml.dumpAs(events, Tag.SEQ, DumperOptions.FlowStyle.BLOCK));
		}
	}
}
