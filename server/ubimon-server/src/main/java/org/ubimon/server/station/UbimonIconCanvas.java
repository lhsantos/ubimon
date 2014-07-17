package org.ubimon.server.station;

public class UbimonIconCanvas {
	public int x, y, width, height;
	public UbimonIcon left;
	public UbimonIcon right;
	public UbimonIcon up;
	public UbimonIcon down;

	public UbimonIconCanvas() {
		this(0, 0, 0, 0);
	}

	public UbimonIconCanvas(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		left = right = up = down = null;
	}

	public int area() {
		return width * height;
	}

	public boolean fits(int width, int height) {
		return (width <= this.width) && (height <= this.height);
	}
}
