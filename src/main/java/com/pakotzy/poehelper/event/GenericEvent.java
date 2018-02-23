package com.pakotzy.poehelper.event;

public class GenericEvent extends Event {
	private String action;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return "GenericEvent{" +
				"action='" + action + '\'' +
				", hotKey='" + getHotKey() + '\'' +
				", enabled=" + isEnabled() +
				'}';
	}
}
