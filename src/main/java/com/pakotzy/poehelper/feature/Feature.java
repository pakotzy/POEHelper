package com.pakotzy.poehelper.feature;

import com.pakotzy.poehelper.PoeHelperApplication;
import com.pakotzy.poehelper.event.Event;
import javafx.scene.control.TitledPane;

import java.util.List;

public abstract class Feature {
	//	@Valid
	private List<Event> events;

	private int fId;

	public abstract TitledPane draw();

	public abstract void run(Integer id);

	public abstract void stop(Integer id);

	public void bind(int fId, int eId) {
		PoeHelperApplication.eventHeap.add(fId + ":" + eId);
	}

	public void unbind(int fId, int eId) {
		PoeHelperApplication.eventHeap.remove(fId + ":" + eId);
	}

	int getFeatureId() {
		return fId;
	}

	public void setFeatureId(int fId) {
		this.fId = fId;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public Event getEvent(int id) {
		if (id < events.size())
			return events.get(id);
		return null;
	}

	public int addEvent(Event event) {
		events.add(event);
		return events.size() - 1;
	}

	@Override
	public String toString() {
		return "Feature{\n" +
				events +
				'}';
	}
}
