package org.ubimon.server.persistence;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ubimon.server.model.Client;

public class ClientDao {
	private EntityManager em = null;
	private int timeout;

	public ClientDao() {
	}

	public void init(int timeout) {
		this.em = JPAUtil.getEntityManager();
		this.timeout = timeout;
	}

	public void insert(Client c) {
		begin();
		em.persist(c);
		end();
	}

	public void update(Client c) {
		begin();
		em.merge(c);
		end();
	}

	public List<Client> list() {
		begin();
		List<Client> ret = em.createQuery("select c from Client c",
				Client.class).getResultList();
		end();

		return ret;
	}

	public Client find(Integer id) {
		begin();
		Client c = null;
		try {
			c = em.createQuery("select c from Client c where c.id = :id",
					Client.class).setParameter("id", id).getSingleResult();
		} catch (NoResultException e) {
		}
		end();

		return c;
	}

	public List<Client> find(String clientName, String deviceName) {
		begin();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Client> criteria = builder.createQuery(Client.class);
		Root<Client> root = criteria.from(Client.class);
		Predicate conjunction = builder.conjunction();
		if (clientName != null) {
			conjunction = builder
					.and(conjunction, builder.equal(root.<Client> get("name"),
							clientName.trim().toLowerCase()));
		}
		if (deviceName != null) {
			conjunction = builder.and(conjunction, builder.equal(root
					.<Client> get("deviceName"), deviceName.trim()
					.toLowerCase()));
		}
		criteria.where(conjunction);
		List<Client> ret = em.createQuery(criteria).getResultList();
		end();

		return ret;
	}

	public void delete(Client c) {
		begin();
		em.remove(c);
		end();
	}

	public void clear() {
		begin();
		em.createQuery("delete from Client").executeUpdate();
		end();
	}

	public void destroy() {
		em.close();
	}

	private void begin() {
		processTimeout();
		em.getTransaction().begin();
	}

	private void end() {
		em.getTransaction().commit();
	}

	private void processTimeout() {
		em.getTransaction().begin();

		Calendar time = Calendar.getInstance();
		time.add(Calendar.SECOND, -timeout);
		em.createQuery("delete from Client c where c.lastUpdate < :time")
				.setParameter("time", time, TemporalType.TIMESTAMP)
				.executeUpdate();

		em.getTransaction().commit();
	}
}
