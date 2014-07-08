package org.ubimon.server.uos.drivers;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ubimon.server.model.Client;
import org.ubimon.server.model.Position;
import org.ubimon.server.persistence.ClientDao;
import org.ubimon.server.uos.UosUtil;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

public class PositionRegistryDriver implements UosDriver {
	public static final String TIMEOUT_KEY = "ubimon.positionregistrydriver.timout";
	public static final int DEFAULT_TIMEOUT = 5 * 60; // five minutes

	private static Logger logger = UOSLogging.getLogger();

	private ClientDao dao;

	public PositionRegistryDriver() {
		dao = new ClientDao();
	}

	// Driver services...
	/**
	 * Registers a client an unknown client into the database.
	 */
	public void checkIn(Call call, Response response, CallContext context) {
		try {
			UosUtil.validateParameters(call, getDriver());
			Position p = Position.extract(call);
			String clientName = call.getParameterString("clientName");
			String metadata = null;
			Object temp = call.getParameter("metadata");
			if (temp != null)
				metadata = temp.toString();
			UpDevice caller = context.getCallerDevice();

			List<Client> list = dao.find(clientName, caller.getName());
			if ((list != null) && (!list.isEmpty()))
				response.setError("client name or device already registered");
			else {
				Client c = new Client();
				c.setName(clientName);
				c.setDeviceName(caller.getName());
				c.setDeviceDesc(caller.toJSON().toString());
				c.setLastUpdate(Calendar.getInstance());
				c.setPosition(p);
				c.setMetadata(metadata);
				dao.insert(c);

				response.addParameter("clientId", c.getId());
			}
		} catch (IllegalArgumentException e) {
			response.setError(e.getMessage());
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "check-in fail", t);
			response.setError("failed to check-in: " + t);
		}
	}

	/**
	 * Updates a known client's data in the database.
	 */
	public void update(Call call, Response response, CallContext context) {
		try {
			UosUtil.validateParameters(call, getDriver());
			int id = UosUtil.extractInt(call, "clientId");
			Position p = Position.extract(call);
			String metadata = null;
			Object temp = call.getParameter("metadata");
			if (temp != null)
				metadata = temp.toString();
			UpDevice caller = context.getCallerDevice();

			Client c = dao.find(id);
			if (c == null)
				response.setError("client id not found");
			else {
				c.setDeviceName(caller.getName());
				c.setDeviceDesc(caller.toJSON().toString());
				c.setLastUpdate(Calendar.getInstance());
				c.setPosition(p);
				if (metadata != null)
					c.setMetadata(metadata);

				dao.update(c);
			}
		} catch (IllegalArgumentException e) {
			response.setError(e.getMessage());
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "check-in fail", t);
			response.setError("failed to update: " + t.getMessage());
		}
	}

	/**
	 * Removes a client from the database, if found by id.
	 */
	public void checkOut(Call call, Response response, CallContext context) {
		try {
			UosUtil.validateParameters(call, getDriver());
			int id = UosUtil.extractInt(call, "clientId");

			Client c = dao.find(id);
			if (c != null)
				dao.delete(c);
		} catch (IllegalArgumentException e) {
			response.setError(e.getMessage());
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "check-in fail", t);
			response.setError("failed to check-out: " + t.getMessage());
		}
	}

	/**
	 * Returns a list of all clients within the range of the given position.
	 */
	public void listNeighbours(Call call, Response response, CallContext context) {

	}

	public ClientDao getDao() {
		return dao;
	}

	// UosDriver interface...
	@Override
	public UpDriver getDriver() {
		return driver;
	}

	@Override
	public List<UpDriver> getParent() {
		return null;
	}

	@Override
	public void init(Gateway g, InitialProperties props, String instanceId) {
		UOSLogging.getLogger().info("Initialising PositionRegistryDriver.");

		int timeout = DEFAULT_TIMEOUT;
		if (props.containsKey(TIMEOUT_KEY))
			timeout = (Integer) props.getInt(TIMEOUT_KEY);

		dao.init(timeout);
	}

	@Override
	public void destroy() {
		dao.destroy();
	}

	private static final UpDriver driver;
	static {
		UpDriver d = new UpDriver("ubimon.PositionRegistryDriver");

		d.addService("checkIn")
				.addParameter("clientName", UpService.ParameterType.OPTIONAL)
				.addParameter("latitude", UpService.ParameterType.MANDATORY)
				.addParameter("longitude", UpService.ParameterType.MANDATORY)
				.addParameter("delta", UpService.ParameterType.MANDATORY)
				.addParameter("metadata", UpService.ParameterType.OPTIONAL);

		d.addService("update")
				.addParameter("clientId", UpService.ParameterType.MANDATORY)
				.addParameter("latitude", UpService.ParameterType.MANDATORY)
				.addParameter("longitude", UpService.ParameterType.MANDATORY)
				.addParameter("delta", UpService.ParameterType.MANDATORY)
				.addParameter("metadata", UpService.ParameterType.OPTIONAL);

		d.addService("checkOut").addParameter("clientId",
				UpService.ParameterType.MANDATORY);

		d.addService("listNeighbours")
				.addParameter("latitude", UpService.ParameterType.MANDATORY)
				.addParameter("longitude", UpService.ParameterType.MANDATORY)
				.addParameter("delta", UpService.ParameterType.MANDATORY)
				.addParameter("range", UpService.ParameterType.OPTIONAL);

		driver = d;
	}
}
