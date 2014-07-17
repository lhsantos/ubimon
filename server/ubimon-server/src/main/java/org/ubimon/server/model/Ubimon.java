package org.ubimon.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.unbiquitous.json.JSONArray;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

@Entity
public class Ubimon implements Serializable {
	private static final long serialVersionUID = -2425419548326770649L;

	public enum Type
	{
		FIRE,
		WATER,
		ELETRIC,
		FLYING,
		GRASS
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false)
	private String gameId;
	@Column(nullable = false)
	private String prototype;
	@ManyToOne
	private Player owner;
	@Column(nullable = false)
	private String trainer;
	private String name;
	@Column(nullable = false)
	private Integer level;
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> moves;
	private Integer life;
	@Column(nullable = false)
	private Integer maxLife;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getPrototype() {
		return prototype;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public void setPrototype(String prototype) {
		this.prototype = prototype;
	}

	public String getTrainer() {
		return trainer;
	}

	public void setTrainer(String trainer) {
		this.trainer = trainer;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public List<String> getMoves() {
		return moves;
	}

	public void setMoves(List<String> moves) {
		this.moves = moves;
	}

	public Integer getLife() {
		return life;
	}

	public void setLife(Integer life) {
		this.life = life;
	}

	public Integer getMaxLife() {
		return maxLife;
	}

	public void setMaxLife(Integer maxLife) {
		this.maxLife = maxLife;
	}

	public static String serialize(Ubimon ubimon) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", ubimon.gameId);
		json.put("prototype", ubimon.prototype);
		json.put("trainer", ubimon.trainer);
		json.put("name", ubimon.name);
		json.put("level", ubimon.level);
		json.put("moves", ubimon.moves);
		json.put("life", ubimon.life);
		json.put("maxLife", ubimon.maxLife);

		return json.toString();
	}

	public static Ubimon FromJSON(JSONObject json) throws Exception {
		Ubimon u = new Ubimon();

		u.setGameId(json.getString("id"));

		String proto = json.getString("prototype");
		if (UbimonData.byName(proto) == null)
			throw new Exception("Invalid ubimon JSON object.");
		u.setPrototype(proto);

		u.setTrainer(json.getString("trainer"));
		u.setName(json.getString("name"));
		u.setLevel(json.getInt("level"));

		List<String> moves = new ArrayList<String>();
		JSONArray movesObj = json.getJSONArray("moves");
		for (int i = 0; i < movesObj.length(); ++i) {
			moves.add(movesObj.getString(i));
		}
		u.setMoves(moves);
		
		u.setLife(json.getInt("life"));
		u.setMaxLife(json.getInt("maxLife"));

		return u;
	}
}
