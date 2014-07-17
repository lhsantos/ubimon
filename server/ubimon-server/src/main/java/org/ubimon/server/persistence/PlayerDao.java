package org.ubimon.server.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.ubimon.server.model.Player;

public class PlayerDao {
	private EntityManager em = null;
	private UbimonDao ubimonDao;

	public PlayerDao(UbimonDao ubimonDao) {
		this.ubimonDao = ubimonDao;
	}

	public void init() {
		this.em = JPAUtil.getEntityManager();
	}

	public List<Player> list() {
		em.getTransaction().begin();
		List<Player> ret = em.createQuery("select p from Player p", Player.class).getResultList();
		em.getTransaction().commit();

		return ret;
	}

	public Player find(Integer id) {
		em.getTransaction().begin();
		Player p = null;
		try {
			p = em.createQuery("select p from Player p where p.id = :id", Player.class)
					.setParameter("id", id)
					.getSingleResult();
			p.setUbimons(ubimonDao.list(p));
		}
		catch (NoResultException e) {
		}
		em.getTransaction().commit();

		return p;
	}

	public Player find(String gameId) {
		em.getTransaction().begin();
		Player p = null;
		try {
			p = em.createQuery("select p from Player p where p.gameId = :id", Player.class)
					.setParameter("id", gameId)
					.getSingleResult();
			p.setUbimons(ubimonDao.list(p));
		}
		catch (NoResultException e) {
		}
		em.getTransaction().commit();

		return p;
	}

	public void insert(Player p) {
		em.getTransaction().begin();
		em.persist(p);
		em.getTransaction().commit();
	}
}
