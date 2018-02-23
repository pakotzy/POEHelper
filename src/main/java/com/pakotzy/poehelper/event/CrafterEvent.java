package com.pakotzy.poehelper.event;

//@Validated
public class CrafterEvent extends Event {
	//	@Max(6)
	private int links;
	//	@Max(6)
	private int sockets;
	//	@Size(min = 1, max = 11)
//	@Pattern(regexp = "^(([RGB])([ -][RGB])*)$")
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
