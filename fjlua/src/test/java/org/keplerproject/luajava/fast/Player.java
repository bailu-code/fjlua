package org.keplerproject.luajava.fast;

/**
 * @author wl
 * @version 2021.10.29
 */
public class Player {

	private long exp;

	public void addExp(int exp) {
		this.exp += exp;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = exp;
	}

	@Override
	public String toString() {
		return "Player{" +
				"exp=" + exp +
				'}';
	}
}
