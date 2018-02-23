package com.pakotzy.poehelper.event;

public class Event {
	//	@NotNull
	private String hotKey;
	//	@NotNull
	private boolean enabled;

	public String getHotKey() {
		return hotKey;
	}

	public void setHotKey(String hotKey) {
		this.hotKey = hotKey;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "Event{" +
				"hotKey='" + hotKey + '\'' +
				", enabled=" + enabled +
				'}';
	}
}
