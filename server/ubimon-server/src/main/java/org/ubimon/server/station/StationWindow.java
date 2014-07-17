package org.ubimon.server.station;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.UUID;

import javax.swing.JFrame;

import org.ubimon.server.model.Player;
import org.ubimon.server.model.Ubimon;
import org.ubimon.server.persistence.PlayerDao;
import org.ubimon.server.persistence.UbimonDao;
import org.unbiquitous.json.JSONObject;

@SuppressWarnings("serial")
public class StationWindow extends JFrame {
	private UbimonViewer ubimonViewer;
	private PlayerDao playerDao;
	private UbimonDao ubimonDao;
	private Player player;
	private long lastInteraction;

	public StationWindow(String stationName) {
		super("Welcome to " + stationName + " station!!");

		ubimonDao = new UbimonDao();
		ubimonDao.init();

		playerDao = new PlayerDao(ubimonDao);
		playerDao.init();

		Player p = new Player();
		p.setGameId("teste");
		playerDao.insert(p);

		Ubimon u;

		for (int i = 0; i < 4; ++i) {
		u = new Ubimon();
		u.setGameId(UUID.randomUUID().toString());
		u.setPrototype("pikachu");
		u.setOwner(p);
		u.setTrainer(p.getGameId());
		u.setName("pikachu");
		u.setLevel(1);
		u.setLife(100);
		u.setMaxLife(100);
		ubimonDao.insert(u);

		u = new Ubimon();
		u.setGameId(UUID.randomUUID().toString());
		u.setPrototype("vulpix");
		u.setOwner(p);
		u.setTrainer(p.getGameId());
		u.setName("vulpix");
		u.setLevel(1);
		u.setLife(100);
		u.setMaxLife(100);
		ubimonDao.insert(u);

		u = new Ubimon();
		u.setGameId(UUID.randomUUID().toString());
		u.setPrototype("pidgey");
		u.setOwner(p);
		u.setTrainer(p.getGameId());
		u.setName("pidgey");
		u.setLevel(1);
		u.setLife(100);
		u.setMaxLife(100);
		ubimonDao.insert(u);

		u = new Ubimon();
		u.setGameId(UUID.randomUUID().toString());
		u.setPrototype("poliwag");
		u.setOwner(p);
		u.setTrainer(p.getGameId());
		u.setName("poliwag");
		u.setLevel(1);
		u.setLife(100);
		u.setMaxLife(100);
		ubimonDao.insert(u);

		u = new Ubimon();
		u.setGameId(UUID.randomUUID().toString());
		u.setPrototype("bellsprout");
		u.setOwner(p);
		u.setTrainer(p.getGameId());
		u.setName("bellsprout");
		u.setLevel(1);
		u.setLife(100);
		u.setMaxLife(100);
		ubimonDao.insert(u);
		}
		
		init();
	}

	private void init() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 1.0;

		ubimonViewer = new UbimonViewer();
		ubimonViewer.setBackground(new Color(0x0099CC));
		add(ubimonViewer, gbc);
	}

	public void enter(String playerId) throws Exception {
		if ((player != null) && ((System.currentTimeMillis() - lastInteraction) < 30000))
			throw new Exception("Someone is already using this station.");

		player = playerDao.find(playerId);
		if (player == null) {
			player = new Player();
			player.setGameId(playerId);
			playerDao.insert(player);
		}

		updateScreen();
		lastInteraction = System.currentTimeMillis();
	}

	public void leave() {
		player = null;
		updateScreen();
	}

	public void cursorToLeft(String playerId) throws Exception {
		if (!playerId.equals(player.getGameId()))
			throw new Exception("You are not the one using this station!");

		ubimonViewer.moveCursorLeft();
		lastInteraction = System.currentTimeMillis();
	}

	public void cursorToRight(String playerId) throws Exception {
		if (!playerId.equals(player.getGameId()))
			throw new Exception("You are not the one using this station!");

		ubimonViewer.moveCursorRight();
		lastInteraction = System.currentTimeMillis();
	}

	public void cursorToUp(String playerId) throws Exception {
		if (!playerId.equals(player.getGameId()))
			throw new Exception("You are not the one using this station!");

		ubimonViewer.moveCursorUp();
		lastInteraction = System.currentTimeMillis();
	}

	public void cursorToDown(String playerId) throws Exception {
		if (!playerId.equals(player.getGameId()))
			throw new Exception("You are not the one using this station!");

		ubimonViewer.moveCursorDown();
		lastInteraction = System.currentTimeMillis();
	}

	public Ubimon peek(String playerId) throws Exception {
		if (!playerId.equals(player.getGameId()))
			throw new Exception("You are not the one using this station!");

		lastInteraction = System.currentTimeMillis();
		return ubimonViewer.getSelectedUbimon();
	}

	public void removeSelected(String playerId) throws Exception {
		if (!playerId.equals(player.getGameId()))
			throw new Exception("You are not the one using this station!");

		Ubimon u = ubimonViewer.getSelectedUbimon();
		if (u != null) {
			ubimonDao.delete(u);
			player = playerDao.find(player.getId());
			updateScreen();
		}
		lastInteraction = System.currentTimeMillis();
	}

	public void store(String playerId, String ubimon) throws Exception {
		if (!playerId.equals(player.getGameId()))
			throw new Exception("You are not the one using this station!");

		Ubimon u = Ubimon.FromJSON(new JSONObject(ubimon));
		if (ubimonDao.find(u.getGameId()) != null)
			throw new Exception("Ubimon already stored here.");

		if (ubimonViewer.fit(player.getUbimons(), u) != null) {
			u.setOwner(player);
			ubimonDao.insert(u);
			player = playerDao.find(player.getId());
			updateScreen();
		}
		else
			throw new Exception("I can't fit this ubimon.");
		lastInteraction = System.currentTimeMillis();
	}

	private void updateScreen() {
		validate();

		UbimonIcon[] icons = null;
		if (player != null) {
			List<Ubimon> ubimons = player.getUbimons();
			icons = ubimonViewer.fit(ubimons, null);
		}
		ubimonViewer.setIcons(icons);

		repaint();
	}
}
