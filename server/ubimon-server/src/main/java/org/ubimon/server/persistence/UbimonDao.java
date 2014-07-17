package org.ubimon.server.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.ubimon.server.model.Player;
import org.ubimon.server.model.Ubimon;

public class UbimonDao {
	private EntityManager em = null;

	public UbimonDao() {
	}

	public void init() {
		this.em = JPAUtil.getEntityManager();
	}

	public List<Ubimon> list() {
		em.getTransaction().begin();
		List<Ubimon> ret = em.createQuery("select u from Ubimon u", Ubimon.class).getResultList();
		em.getTransaction().commit();

		return ret;
	}

	public List<Ubimon> list(Player p) {
		em.getTransaction().begin();
		List<Ubimon> ret =
				em.createQuery("select u from Ubimon u where u.owner = :p", Ubimon.class)
						.setParameter("p", p).getResultList();
		em.getTransaction().commit();

		return ret;
	}

	public Ubimon find(Integer id) {
		em.getTransaction().begin();
		Ubimon u = null;
		try {
			u = em.createQuery("select u from Ubimon u where u.id = :id", Ubimon.class)
					.setParameter("id", id)
					.getSingleResult();
		}
		catch (NoResultException e) {
		}
		em.getTransaction().commit();

		return u;
	}

	public Ubimon find(String gameId) {
		em.getTransaction().begin();
		Ubimon u = null;
		try {
			u = em.createQuery("select u from Ubimon u where u.gameId = :id", Ubimon.class)
					.setParameter("id", gameId)
					.getSingleResult();
		}
		catch (NoResultException e) {
		}
		em.getTransaction().commit();

		return u;
	}

	public void insert(Ubimon u) {
		em.getTransaction().begin();
		em.persist(u);
		em.getTransaction().commit();
	}

	public void delete(Ubimon u) {
		em.getTransaction().begin();
		em.createQuery("delete from Ubimon u where u.id = :id")
				.setParameter("id", u.getId())
				.executeUpdate();
		em.getTransaction().commit();
	}
}
