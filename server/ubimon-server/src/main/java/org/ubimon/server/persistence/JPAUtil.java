package org.ubimon.server.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class JPAUtil {
	private JPAUtil() {
	}

	private static final EntityManagerFactory factory = createFactory();

	private static EntityManagerFactory createFactory() {
		return Persistence.createEntityManagerFactory("ubimon");
	}

	public static EntityManager getEntityManager() {
		return factory.createEntityManager();
	}
}
