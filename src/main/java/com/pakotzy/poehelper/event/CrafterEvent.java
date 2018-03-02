package com.pakotzy.poehelper.event;

import javax.validation.constraints.Max;

public class CrafterEvent extends Event {
	@Max(6)
	private int links;
	@Max(6)
	private int sockets;
	@Max(6)
	private int r;
	@Max(6)
	private int g;
	@Max(6)
	private int b;

	public int getLinks() {
		return links;
	}

	public void setLinks(int links) {
		this.links = links;
	}

	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = r;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public int getColorsSum() {
		return r + g + b;
	}

	public int getSockets() {
		return sockets;
	}

	public void setSockets(int sockets) {
		this.sockets = sockets;
	}
}
