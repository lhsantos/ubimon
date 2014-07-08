package org.ubimon.server.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ubimon.server.model.Client;
import org.ubimon.server.model.Position;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

public class ClientDaoTest {
	private ClientDao dao;

	@Before
	public void setup() {
		dao = new ClientDao();
		dao.init(100);
	}

	@After
	public void teardown() {
		dao.clear();
		dao.destroy();
	}

	@Test
	public void shouldReturnASavedClient() throws JSONException {
		Client c = dummyClient("device");
		dao.insert(c);
		assertEquals(dao.find(c.getId()), c);
	}

	@Test
	public void shouldNotReturnAnUnknownClient() throws JSONException {
		Client c = dummyClient("device");
		dao.insert(c);
		assertNull(dao.find(c.getId() + 1));
	}

	@Test
	public void shouldReturnASavedDeviceNameOrClientName() throws JSONException {
		Client c1 = dummyClient("device1");
		Client c2 = dummyClient("device2");
		c2.setName("client2");
		dao.insert(c1);
		dao.insert(c2);

		List<Client> result;

		result = dao.find(null, "device1");
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.get(0), c1);

		result = dao.find(null, "device2");
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.get(0), c2);

		result = dao.find("client2", "device2");
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.get(0), c2);

		result = dao.find("client2", null);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.get(0), c2);
	}

	@Test
	public void shouldReturnSavedClients() throws JSONException {
		Client c1 = dummyClient("device1");
		Client c2 = dummyClient("device2");
		dao.insert(c1);
		dao.insert(c2);
		List<Client> list = dao.list();
		assertNotNull(list);
		assertEquals(list.size(), 2);
		assertTrue(list.contains(c1));
		assertTrue(list.contains(c2));
	}

	@Test
	public void shouldReturnNothingAfterClear() throws JSONException {
		Client client = dummyClient("device");
		dao.insert(client);
		dao.clear();
		List<Client> list = dao.list();
		assertTrue((list == null) || list.isEmpty());
	}

	private static Client dummyClient(String deviceName) throws JSONException {
		Client client = new Client();
		UpDevice device = new UpDevice(deviceName);
		client.setDeviceName(device.getName());
		client.setDeviceDesc(device.toJSON().toString());
		client.setLastUpdate(Calendar.getInstance());
		client.setPosition(new Position(0, 0, 0));
		return client;
	}
}
