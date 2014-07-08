package org.ubimon.server.uos.drivers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ubimon.server.model.Client;
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

		DriverManager dm = uos.getFactory().get(AdaptabilityEngine.class)
				.driverManager();
		dm.deployDriver(driver.getDriver(), driver);
		dm.initDrivers(uos.getGateway(), props);

		driver.getDao().clear();
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
		call.addParameter("clientName", "client1").addParameter("latitude", 42)
				.addParameter("longitude", 43).addParameter("delta", 44)
				.addParameter("metadata", "cool client");
		driver.checkIn(call, r, context);

		assertTrue((r.getError() == null) || r.getError().isEmpty());

		Integer id = (Integer) r.getResponseData("clientId");
		assertNotNull(id);

		Client c = driver.getDao().find(id);
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
		call.addParameter("clientName", "client1").addParameter("latitude", 42)
				.addParameter("longitude", 43).addParameter("delta", 44)
				.addParameter("metadata", "cool client");
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
		call.addParameter("clientName", "client1").addParameter("latitude", 42)
				.addParameter("longitude", 43).addParameter("delta", 44)
				.addParameter("metadata", "cool client");
		driver.checkIn(call, r, context);
		Integer id = (Integer) r.getResponseData("clientId");

		call = new Call("ubimon.PositionRegistryDriver", "update");
		call.addParameter("clientId", id).addParameter("latitude", 4242)
				.addParameter("longitude", 4243).addParameter("delta", 4244)
				.addParameter("metadata", "hot client");
		driver.update(call, r, context);

		assertTrue((r.getError() == null) || r.getError().isEmpty());
		Client c = driver.getDao().find(id);
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
		call.addParameter("clientId", -1).addParameter("latitude", 4242)
				.addParameter("longitude", 4243).addParameter("delta", 4244)
				.addParameter("metadata", "hot client");
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
		call.addParameter("clientName", "client1").addParameter("latitude", 42)
				.addParameter("longitude", 43).addParameter("delta", 44)
				.addParameter("metadata", "cool client");
		driver.checkIn(call, r, context);
		Integer id = (Integer) r.getResponseData("clientId");

		call = new Call("ubimon.PositionRegistryDriver", "checkOut");
		call.addParameter("clientId", id);
		driver.checkOut(call, r, context);

		assertTrue((r.getError() == null) || r.getError().isEmpty());
		Client c = driver.getDao().find(id);
		assertNull(c);
	}

	@Test
	public void unregistersOnTimeout() throws Exception {
		System.out.println("\n\n\n\n------------------------------------");
		System.out.println(driver.getDao().list());
		Gateway g = uos.getGateway();
		UpDevice currentDevice = g.getCurrentDevice();
		CallContext context = new CallContext();
		context.setCallerDevice(currentDevice);
		Response r = new Response();

		Call call = new Call("ubimon.PositionRegistryDriver", "checkIn");
		call.addParameter("clientName", "client1").addParameter("latitude", 42)
				.addParameter("longitude", 43).addParameter("delta", 44)
				.addParameter("metadata", "cool client");
		driver.checkIn(call, r, context);
		Integer id = (Integer) r.getResponseData("clientId");

		Thread.sleep((TIMEOUT + 1) * 1000);
		Client c = driver.getDao().find(id);
		assertNull(c);
	}
}
