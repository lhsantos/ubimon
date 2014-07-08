package org.ubimon.server;

import java.util.ResourceBundle;

import org.ubimon.server.uos.drivers.PositionRegistryDriver;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UosApplication;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyUndeploy;

public class UbimonServer implements UosApplication {
	private static final String CONFIG_BUNDLE_FILE = "ubimon-server";

	/**
	 * Entry point for this server.
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		InitialProperties props = new InitialProperties(ResourceBundle.getBundle(CONFIG_BUNDLE_FILE));
		props.addDriver(PositionRegistryDriver.class);
		props.addApplication(UbimonServer.class);

		UOS uos = new UOS();
		uos.start(props);

		Thread.sleep(5000);
		uos.stop();
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
