package org.ubimon.server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.ubimon.server.model.Position;
import org.ubimon.server.model.Ubimon;
import org.ubimon.server.station.StationWindow;
import org.ubimon.server.uos.drivers.PositionRegistryDriver;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.applicationManager.UosApplication;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyUndeploy;
import org.unbiquitous.uos.network.socket.radar.MulticastRadar;

public class UbimonServer implements UosApplication {
	private static UOS uos;
	private static boolean running;
	private static StationWindow station;

	/**
	 * Entry point for this server.
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		// Processes command line arguments.
		Set<String> argsMap = new HashSet<String>();
		for (String arg : args)
			argsMap.add(arg);
		boolean createStation = argsMap.contains("--station");

		// Prepares and starts middleware.
		UOSLogging.getLogger().setLevel(Level.ALL);

		InitialProperties props = new MulticastRadar.Properties();
		props.setDeviceName("ubimon-server");
		props.addDriver(PositionRegistryDriver.class);
		props.addApplication(UbimonServer.class, "ubimon");

		uos = new UOS();
		uos.start(props);
		running = true;

		(new KeepPositionThread(createStation ? "ubimon,station" : null)).start();

		if (createStation)
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					station = new StationWindow("laico");

					station.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							running = false;
							uos.stop();
						}
					});

					station.setVisible(true);
					station.setExtendedState(station.getExtendedState() | StationWindow.MAXIMIZED_BOTH);
					station.setDefaultCloseOperation(StationWindow.EXIT_ON_CLOSE);
				}
			});
	}

	private static class KeepPositionThread extends Thread {
		private Position myPos;
		private String metadata;
		private Integer posRegId;

		public KeepPositionThread(String metadata) {
			this.myPos = new Position(-15.804479, -47.868465, 50); // CNMP
			//this.myPos = new Position(-15.833204, -47.981353, 50); // Casa
			this.metadata = metadata;
		}

		@Override
		public void run() {
			try {
				checkIn();
				while (running) {
					// Sleeps for one minute.
					Thread.sleep(60 * 1000);
					updatePos();
				}
			}
			catch (Throwable e) {
				UOSLogging.getLogger().severe("Error while handling position. " + e.toString());
			}
		}

		private void checkIn() throws ServiceCallException {
			Call call = new Call(PositionRegistryDriver.DRIVER_NAME, "checkIn");
			call.addParameter("clientName", "laico");
			call.addParameter("latitude", myPos.getLatitude());
			call.addParameter("longitude", myPos.getLongitude());
			call.addParameter("delta", myPos.getDelta());
			if (metadata != null)
				call.addParameter("metadata", metadata);

			Gateway gateway = uos.getGateway();
			Response r = gateway.callService(gateway.getCurrentDevice(), call);
			posRegId = Integer.parseInt(r.getResponseData("clientId").toString());
		}

		private void updatePos() throws ServiceCallException {
			Call call = new Call(PositionRegistryDriver.DRIVER_NAME, "update");
			call.addParameter("clientId", posRegId);
			call.addParameter("latitude", myPos.getLatitude());
			call.addParameter("longitude", myPos.getLongitude());
			call.addParameter("delta", myPos.getDelta());

			Gateway gateway = uos.getGateway();
			gateway.callService(gateway.getCurrentDevice(), call);
		}
	}

	// // UOS interface ////
	@Override
	public void init(OntologyDeploy knowledgeBase, InitialProperties properties, String appId) {
	}

	@Override
	public void start(Gateway gateway, OntologyStart ontology) {
		try {
			System.out.println(gateway.getCurrentDevice().toJSON().toString());
		}
		catch (Throwable t) {
		}
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public void tearDown(OntologyUndeploy ontology) throws Exception {
	}

	public Map<String, Object> enter(Map<String, Object> parameters) {
		Map<String, Object> response = new HashMap<String, Object>();
		if (station == null)
			response.put("error", "Station is not available!");
		else {
			Object playerId = parameters.get("playerId");
			if (playerId == null)
				response.put("error", "No player id provided.");
			else {
				try {
					station.enter(playerId.toString());
				}
				catch (Exception e) {
					response.put("error", e.getMessage());
				}
			}
		}

		return response;
	}

	public Map<String, Object> leave(Map<String, Object> parameters) {
		Map<String, Object> response = new HashMap<String, Object>();
		if (station != null)
			station.leave();
		return response;
	}

	public Map<String, Object> cursorToLeft(Map<String, Object> parameters) {
		Map<String, Object> response = new HashMap<String, Object>();
		if (station == null)
			response.put("error", "Station is not available!");
		else {
			Object playerId = parameters.get("playerId");
			if (playerId == null)
				response.put("error", "No player id provided.");
			else {
				try {
					station.cursorToLeft(playerId.toString());
				}
				catch (Exception e) {
					response.put("error", e.getMessage());
				}
			}
		}
		return response;
	}

	public Map<String, Object> cursorToRight(Map<String, Object> parameters) {
		Map<String, Object> response = new HashMap<String, Object>();
		if (station == null)
			response.put("error", "Station is not available!");
		else {
			Object playerId = parameters.get("playerId");
			if (playerId == null)
				response.put("error", "No player id provided.");
			else {
				try {
					station.cursorToRight(playerId.toString());
				}
				catch (Exception e) {
					response.put("error", e.getMessage());
				}
			}
		}
		return response;
	}

	public Map<String, Object> cursorToUp(Map<String, Object> parameters) {
		Map<String, Object> response = new HashMap<String, Object>();
		if (station == null)
			response.put("error", "Station is not available!");
		else {
			Object playerId = parameters.get("playerId");
			if (playerId == null)
				response.put("error", "No player id provided.");
			else {
				try {
					station.cursorToUp(playerId.toString());
				}
				catch (Exception e) {
					response.put("error", e.getMessage());
				}
			}
		}
		return response;
	}

	public Map<String, Object> cursorToDown(Map<String, Object> parameters) {
		Map<String, Object> response = new HashMap<String, Object>();
		if (station == null)
			response.put("error", "Station is not available!");
		else {
			Object playerId = parameters.get("playerId");
			if (playerId == null)
				response.put("error", "No player id provided.");
			else {
				try {
					station.cursorToDown(playerId.toString());
				}
				catch (Exception e) {
					response.put("error", e.getMessage());
				}
			}
		}
		return response;
	}

	public Map<String, Object> peek(Map<String, Object> parameters) {
		Map<String, Object> response = new HashMap<String, Object>();
		if (station == null)
			response.put("error", "Station is not available!");
		else {
			Object playerId = parameters.get("playerId");
			if (playerId == null)
				response.put("error", "No player id provided.");
			else {
				try {
					Ubimon ubimon = station.peek(playerId.toString());
					if (ubimon != null)
						response.put("ubimon", Ubimon.serialize(ubimon));
				}
				catch (Exception e) {
					response.put("error", e.getMessage());
				}
			}
		}
		return response;
	}

	public Map<String, Object> removeSelected(Map<String, Object> parameters) {
		Map<String, Object> response = new HashMap<String, Object>();
		if (station == null)
			response.put("error", "Station is not available!");
		else {
			Object playerId = parameters.get("playerId");
			if (playerId == null)
				response.put("error", "No player id provided.");
			else {
				try {
					station.removeSelected(playerId.toString());
				}
				catch (Exception e) {
					response.put("error", e.getMessage());
				}
			}
		}
		return response;
	}

	public Map<String, Object> store(Map<String, Object> parameters) {
		Map<String, Object> response = new HashMap<String, Object>();
		if (station == null)
			response.put("error", "Station is not available!");
		else {
			Object playerId = parameters.get("playerId");
			Object ubimon = parameters.get("ubimon");
			if (playerId == null)
				response.put("error", "No player id provided.");
			else if (ubimon == null)
				response.put("error", "No ubimon provided.");
			else {
				try {
					station.store(playerId.toString(), ubimon.toString());
				}
				catch (Exception e) {
					response.put("error", e.getMessage());
				}
			}
		}
		return response;
	}
}
