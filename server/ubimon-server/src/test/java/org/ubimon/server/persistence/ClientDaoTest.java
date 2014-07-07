package org.ubimon.server.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientDaoTest {
	private ClientDao dao;

	@Before
	public void setup() {
		dao = new ClientDao();
	}

	@After
	public void teardown() {
		dao.clear();
	}

	@Test
	public void shouldReturnASavedClient() {
		Client client = new Client();
		dao.save(client);
		List<Client> list = dao.list();
		assertNotNull(list);
		Client ret = list.get(0);
		assertEquals(client, ret);
	}

	@Test
	public void shouldReturnNothingAfterClear() {
		Client client = new Client();
		dao.save(client);
		dao.clear();
		List<Client> list = dao.list();
		assertTrue((list == null) || list.isEmpty());
	}
}
