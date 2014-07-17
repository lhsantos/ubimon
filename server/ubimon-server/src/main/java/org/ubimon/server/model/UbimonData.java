package org.ubimon.server.model;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.ubimon.server.ResourceDatabase;

public class UbimonData {
	public static final Dimension refResolution = new Dimension(683, 384);
	public static final float refFontSize = 6;

	private String name;
	private BufferedImage sprite;
	private Ubimon.Type[] types;

	private UbimonData(String name, String icon, Ubimon.Type[] types) {
		this.name = name;
		this.sprite = ResourceDatabase.loadImage(icon);
		this.types = types;
	}

	public static UbimonData byName(String name) {
		return database.get(name);
	}

	public String getName() {
		return name;
	}

	public BufferedImage getSprite() {
		return sprite;
	}

	public Ubimon.Type[] getTypes() {
		return types;
	}

	private static final Map<String, UbimonData> database;
	static {
		database = new HashMap<String, UbimonData>();
		UbimonData data;
		data = new UbimonData("pikachu", "eletric.png", new Ubimon.Type[] { Ubimon.Type.ELETRIC });
		database.put(data.getName(), data);
		data = new UbimonData("vulpix", "fire.png", new Ubimon.Type[] { Ubimon.Type.FIRE });
		database.put(data.getName(), data);
		data = new UbimonData("pidgey", "flying.png", new Ubimon.Type[] { Ubimon.Type.FLYING });
		database.put(data.getName(), data);
		data = new UbimonData("bellsprout", "grass.png", new Ubimon.Type[] { Ubimon.Type.GRASS });
		database.put(data.getName(), data);
		data = new UbimonData("poliwag", "water.png", new Ubimon.Type[] { Ubimon.Type.WATER });
		database.put(data.getName(), data);
	}
}
