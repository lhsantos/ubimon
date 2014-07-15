package org.ubimon.server;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.ubimon.server.model.Position;
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
		props.addApplication(UbimonServer.class);

		UOS uos = new UOS();
		uos.start(props);

		(new KeepPositionThread(uos, createStation ? "ubimon,station" : null)).start();
	}

	private static class KeepPositionThread extends Thread {
		private UOS uos;
		private Position myPos;
		private String metadata;
		private Integer posRegId;

		public KeepPositionThread(UOS uos, String metadata) {
			this.uos = uos;

			this.myPos = new Position(-15.804479, -47.868465, 50); // CNMP

			this.metadata = metadata;
		}

		@Override
		public void run() {
			try {
				checkIn();
				while (true) {
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
			if (metadata != null) {
				System.out.println("included metadata");
				call.addParameter("metadata", metadata);
			}

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

	@Override
	public void init(OntologyDeploy knowledgeBase, InitialProperties properties, String appId) {
	}

	@Override
	public void start(Gateway gateway, OntologyStart ontology) {
		try {
		System.out.println(gateway.getCurrentDevice().toJSON().toString());
		}
		catch (Throwable t){}
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public void tearDown(OntologyUndeploy ontology) throws Exception {
	}
}
