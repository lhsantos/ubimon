package org.ubimon.server;

import java.awt.Font;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.ubimon.server.station.UbimonViewer;

public final class ResourceDatabase {
	private ResourceDatabase() {
	}

	public static BufferedImage loadImage(String fileName) {
		try {
			return ImageIO.read(BufferedImage.class.getResourceAsStream("/images/" + fileName));
		}
		catch (Throwable t) {
			System.err.println("Failed to load image: " + fileName + ".");
			t.printStackTrace();
			return null;
		}
	}

	public static Font loadFont(String fileName) {
		try {
			return Font.createFont(
					Font.TRUETYPE_FONT,
					UbimonViewer.class.getResourceAsStream("/fonts/" + fileName));
		}
		catch (Throwable t) {
			System.err.println("Failed to load font: " + fileName + ".");
			t.printStackTrace();
			return null;
		}
	}
}
