package com.pakotzy.poehelper.event;

import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CrafterEvent extends Event {
	@Max(6)
	private int links;
	@Max(6)
	private int sockets;
	@Size(max = 11)
	@Pattern(regexp = "^(([RGB])([ -][RGB])*)$")
	private String colors;

	public int getLinks() {
		return links;
	}

	public void setLinks(int links) {
		this.links = links;
	}

	public int getSockets() {
		return sockets;
	}

	public void setSockets(int sockets) {
		this.sockets = sockets;
	}

	public String getColors() {
		return colors;
	}

	public void setColors(String colors) {
		this.colors = colors;
	}

	@Override
	public String toString() {
		return "CrafterEvent{" +
				"hotKey=" + getHotKey() +
				", links=" + links +
				", sockets=" + sockets +
				", colors='" + colors +
				", enabled=" + isEnabled() + '\'' +
				'}';
	}
}
