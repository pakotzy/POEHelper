package com.pakotzy.poehelper.event;

import javax.validation.constraints.Max;
import java.util.ArrayList;

public class CrafterEvent extends Event {
	@Max(6)
	private Integer links;
	@Max(6)
	private Integer sockets;
	@Max(6)
	private Integer r;
	@Max(6)
	private Integer g;
	@Max(6)
	private Integer b;

	private ArrayList<String> mods;

	public CrafterEvent() {
		links = 0;
		sockets = 0;
		r = 0;
		g = 0;
		b = 0;
		mods = new ArrayList<>();
	}

	public Integer getLinks() {
		return links;
	}

	public void setLinks(Integer links) {
		this.links = links;
	}

	public Integer getR() {
		return r;
	}

	public void setR(Integer r) {
		this.r = r;
	}

	public Integer getG() {
		return g;
	}

	public void setG(Integer g) {
		this.g = g;
	}

	public Integer getB() {
		return b;
	}

	public void setB(Integer b) {
		this.b = b;
	}

	public Integer getColorsSum() {
		return r + g + b;
	}

	public Integer getSockets() {
		return sockets;
	}

	public void setSockets(Integer sockets) {
		this.sockets = sockets;
	}

	public ArrayList<String> getMods() {
		return mods;
	}

	public void setMods(ArrayList<String> mods) {
		this.mods = mods;
	}

	public String getMod(int id) {
		return mods.get(id);
	}

	public void setMod(int id, String element) {
		mods.set(id, element);
	}

	public int addMod(String element) {
		mods.add(element);
		return mods.size() - 1;
	}
}
