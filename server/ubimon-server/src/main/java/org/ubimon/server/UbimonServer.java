package org.ubimon.server;

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
	private static final String CONFIG_BUNDLE_FILE = "ubimon-server";

	/**
	 * Entry point for this server.
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		// InitialProperties props = new
		// InitialProperties(ResourceBundle.getBundle(CONFIG_BUNDLE_FILE));
		InitialProperties props = new MulticastRadar.Properties();
		props.setDeviceName("ubimon-server");
		props.addDriver(PositionRegistryDriver.class);
		props.addApplication(UbimonServer.class);

		UOS uos = new UOS();
		UOSLogging.getLogger().setLevel(Level.SEVERE);
		uos.start(props);

		(new KeepPositionThread(uos)).start();
	}

	private static class KeepPositionThread extends Thread {
		private UOS uos;
		private Position myPos = null;
		private Integer posRegId;

		public KeepPositionThread(UOS uos) {
			this.uos = uos;
			this.myPos = new Position(-15.758749, -47.868772, 10);
		}

		@Override
		public void run() {
			try {
				System.out.println(uos.getGateway().getCurrentDevice().toJSON().toString());
				checkIn();
				while (true) {
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
			call.addParameter("metadata", "ubimon,station");

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
	public void init(OntologyDeploy knowledgeBase, InitialProperties props, String appId) {
	}

	@Override
	public void start(Gateway gateway, OntologyStart ontology) {
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public void tearDown(OntologyUndeploy ontology) throws Exception {
	}
}
