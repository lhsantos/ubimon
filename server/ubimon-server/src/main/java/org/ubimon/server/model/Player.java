package org.ubimon.server.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Player implements Serializable {
	private static final long serialVersionUID = 8315183935676782820L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false)
	private String gameId;
	@OneToMany(mappedBy = "owner")
	private List<Ubimon> ubimons;

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

	public List<Ubimon> getUbimons() {
		return ubimons;
	}

	public void setUbimons(List<Ubimon> ubimons) {
		this.ubimons = ubimons;
	}
}
