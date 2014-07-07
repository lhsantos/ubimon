package org.ubimon.server.persistence;

import java.util.List;

import javax.persistence.EntityManager;

public class ClientDao {
	private EntityManager em;

	public ClientDao() {
		this.em = JPAUtil.getEntityManager();
	}

	public void save(Client c) {
		em.getTransaction().begin();
		em.persist(c);
		em.getTransaction().commit();
	}

	public List<Client> list() {
		return em.createQuery("select c from Client c", Client.class)
				.getResultList();
	}

	public void clear() {
		em.getTransaction().begin();
		em.createQuery("delete from Client").executeUpdate();
		em.getTransaction().commit();
	}
}
