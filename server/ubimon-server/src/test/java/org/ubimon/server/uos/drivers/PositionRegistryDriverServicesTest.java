package org.ubimon.server.uos.drivers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ubimon.server.model.Client;
import org.ubimon.server.model.Position;
import org.unbiquitous.json.JSONArray;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.adaptabitilyEngine.AdaptabilityEngine;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

public class PositionRegistryDriverServicesTest {
	private static final int TIMEOUT = 1;

	private PositionRegistryDriver driver;
	private UpDevice currentDevice;
	private UOS uos;

	@Before
	public void setup() throws Exception {
		InitialProperties props = new InitialProperties();
		props.put(PositionRegistryDriver.TIMEOUT_KEY, TIMEOUT);
		uos = new UOS();
		uos.start(props);

		currentDevice = uos.getGateway().getCurrentDevice();
		currentDevice = UpDevice.fromJSON(currentDevice.toJSON());

		driver = new PositionRegistryDriver();

		DriverManager dm = uos.getFactory().get(AdaptabilityEngine.class).driverManager();
		dm.deployDriver(driver.getDriver(), driver);
		dm.initDrivers(uos.getGateway(), props);

		driver.getClientDao().clear();
	}

	@After
	public void tearDown() throws Exception {
		uos.stop();
	}

	@Test
	public void registersClientOnCheckIn() throws Exception {
		Gateway g = uos.getGateway();
		UpDevice currentDevice = g.getCurrentDevice();
		CallContext context = new CallContext();
		context.setCallerDevice(currentDevice);
		Response r = new Response();

		Call call = new Call("ubimon.PositionRegistryDriver", "checkIn");
		call.addParameter("clientName", "client1").addParameter("latitude", 42).addParameter("longitude", 43)
				.addParameter("delta", 44).addParameter("metadata", "cool client");
		driver.checkIn(call, r, context);

		assertTrue((r.getError() == null) || r.getError().isEmpty());

		Integer id = (Integer) r.getResponseData("clientId");
		assertNotNull(id);

		Client c = driver.getClientDao().find(id);
		assertEquals(c.getName(), "client1");
		assertEquals(c.getDeviceName(), currentDevice.getName());
		assertEquals(c.getDeviceDesc(), currentDevice.toJSON().toString());
		assertEquals(c.getPosition().getLatitude(), new Double(42));
		assertEquals(c.getPosition().getLongitude(), new Double(43));
		assertEquals(c.getPosition().getDelta(), new Double(44));
		assertEquals(c.getMetadata(), "cool client");
	}

	@Test
	public void doesntRegisterKnownClients() throws Exception {
		Gateway g = uos.getGateway();
		UpDevice currentDevice = g.getCurrentDevice();
		CallContext context = new CallContext();
		context.setCallerDevice(currentDevice);
		Response r = new Response();

		Call call = new Call("ubimon.PositionRegistryDriver", "checkIn");
		call.addParameter("clientName", "client1").addParameter("latitude", 42).addParameter("longitude", 43)
				.addParameter("delta", 44).addParameter("metadata", "cool client");
		driver.checkIn(call, r, context);
		driver.checkIn(call, r, context);

		assertNotNull(r.getError());
		assertFalse(r.getError().isEmpty());
		assertTrue(r.getError().toLowerCase().contains("already registered"));
	}

	@Test
	public void updatesClientData() throws Exception {
		Gateway g = uos.getGateway();
		UpDevice currentDevice = g.getCurrentDevice();
		CallContext context = new CallContext();
		context.setCallerDevice(currentDevice);
		Response r = new Response();

		Call call = new Call("ubimon.PositionRegistryDriver", "checkIn");
		call.addParameter("clientName", "client1").addParameter("latitude", 42).addParameter("longitude", 43)
				.addParameter("delta", 44).addParameter("metadata", "cool client");
		driver.checkIn(call, r, context);
		Integer id = (Integer) r.getResponseData("clientId");

		call = new Call("ubimon.PositionRegistryDriver", "update");
		call.addParameter("clientId", id).addParameter("latitude", 4242).addParameter("longitude", 4243)
				.addParameter("delta", 4244).addParameter("metadata", "hot client");
		driver.update(call, r, context);

		assertTrue((r.getError() == null) || r.getError().isEmpty());
		Client c = driver.getClientDao().find(id);
		assertEquals(c.getName(), "client1");
		assertEquals(c.getDeviceName(), currentDevice.getName());
		assertEquals(c.getDeviceDesc(), currentDevice.toJSON().toString());
		assertEquals(c.getPosition().getLatitude(), new Double(4242));
		assertEquals(c.getPosition().getLongitude(), new Double(4243));
		assertEquals(c.getPosition().getDelta(), new Double(4244));
		assertEquals(c.getMetadata(), "hot client");
	}

	@Test
	public void doesntUpdateUnknownClient() throws Exception {
		Gateway g = uos.getGateway();
		UpDevice currentDevice = g.getCurrentDevice();
		CallContext context = new CallContext();
		context.setCallerDevice(currentDevice);
		Response r = new Response();

		Call call = new Call("ubimon.PositionRegistryDriver", "update");
		call.addParameter("clientId", -1).addParameter("latitude", 4242).addParameter("longitude", 4243)
				.addParameter("delta", 4244).addParameter("metadata", "hot client");
		driver.update(call, r, context);

		assertNotNull(r.getError());
		assertFalse(r.getError().isEmpty());
		assertTrue(r.getError().toLowerCase().contains("not found"));
	}

	@Test
	public void unregistersOnCheckOut() throws Exception {
		Gateway g = uos.getGateway();
		UpDevice currentDevice = g.getCurrentDevice();
		CallContext context = new CallContext();
		context.setCallerDevice(currentDevice);
		Response r = new Response();

		Call call = new Call("ubimon.PositionRegistryDriver", "checkIn");
		call.addParameter("clientName", "client1").addParameter("latitude", 42).addParameter("longitude", 43)
				.addParameter("delta", 44).addParameter("metadata", "cool client");
		driver.checkIn(call, r, context);
		Integer id = (Integer) r.getResponseData("clientId");

		call = new Call("ubimon.PositionRegistryDriver", "checkOut");
		call.addParameter("clientId", id);
		driver.checkOut(call, r, context);

		assertTrue((r.getError() == null) || r.getError().isEmpty());
		Client c = driver.getClientDao().find(id);
		assertNull(c);
	}

	@Test
	public void unregistersOnTimeout() throws Exception {
		Gateway g = uos.getGateway();
		UpDevice currentDevice = g.getCurrentDevice();
		CallContext context = new CallContext();
		context.setCallerDevice(currentDevice);
		Response r = new Response();

		Call call = new Call("ubimon.PositionRegistryDriver", "checkIn");
		call.addParameter("clientName", "client1").addParameter("latitude", 42).addParameter("longitude", 43)
				.addParameter("delta", 44).addParameter("metadata", "cool client");
		driver.checkIn(call, r, context);
		Integer id = (Integer) r.getResponseData("clientId");

		Thread.sleep((TIMEOUT + 1) * 1000);
		Client c = driver.getClientDao().find(id);
		assertNull(c);
	}

	@Test
	public void listsNeighboursWithinRange() throws Exception {
		final double refLatitude = -15.831499;
		final double refLongitude = -47.981958;

		// Populates clients into database
		final Position[] withinRange = new Position[] {
				new Position(-15.831553, -47.981755, 10),
				new Position(-15.831687, -47.981878, 10),
				new Position(-15.831659, -47.982157, 10),
				new Position(-15.831483, -47.982068, 10)
		};
		final Position[] outsideRange = new Position[] {
				new Position(-15.831061, -47.983122, 10),
				new Position(-15.833054, -47.982135, 10),
				new Position(-15.831805, -47.976331, 10),
				new Position(-15.830071, -47.988455, 10)
		};
		List<String> expected = new ArrayList<String>();
		for (int i = 0; i < withinRange.length; ++i) {
			Client c = new Client();
			c.setName("client" + i);
			UpDevice d = new UpDevice("device" + i);
			c.setDeviceName(d.getName());
			c.setDeviceDesc(d.toJSON().toString());
			c.setLastUpdate(Calendar.getInstance());
			c.setPosition(withinRange[i]);
			driver.getClientDao().insert(c);
			expected.add(c.getName());
		}
		for (int i = 0; i < withinRange.length; ++i) {
			Client c = new Client();
			c.setName("client" + (i + expected.size()));
			UpDevice d = new UpDevice("device" + i + expected.size());
			c.setDeviceName(d.getName());
			c.setDeviceDesc(d.toJSON().toString());
			c.setLastUpdate(Calendar.getInstance());
			c.setPosition(outsideRange[i]);
			driver.getClientDao().insert(c);
		}

		// Tests the service.
		Gateway g = uos.getGateway();
		UpDevice currentDevice = g.getCurrentDevice();
		CallContext context = new CallContext();
		context.setCallerDevice(currentDevice);
		Response r = new Response();

		Call call = new Call("ubimon.PositionRegistryDriver", "listNeighbours");
		call.addParameter("latitude", refLatitude)
				.addParameter("longitude", refLongitude)
				.addParameter("delta", 10)
				.addParameter("range", 100);
		driver.listNeighbours(call, r, context);

		assertTrue((r.getError() == null) || r.getError().isEmpty());
		Object clients = r.getResponseData("clients");
		assertNotNull(clients);
		assertTrue(clients instanceof JSONArray);
		JSONArray jsonClients = (JSONArray) clients;
		assertEquals(jsonClients.length(), expected.size());

		for (String name : expected) {
			assertNotNull(findClient(jsonClients, name));
		}
	}

	private static JSONObject findClient(JSONArray array, String clientName) throws JSONException {
		for (int i = 0; i < array.length(); ++i) {
			JSONObject obj = (JSONObject) array.get(i);
			if (obj.getString("name").equals(clientName))
				return obj;
		}
		return null;
	}
}
