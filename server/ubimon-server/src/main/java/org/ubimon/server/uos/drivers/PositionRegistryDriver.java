package org.ubimon.server.uos.drivers;

import java.util.List;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.UosEventDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

public class PositionRegistryDriver implements UosEventDriver {

	@Override
	public UpDriver getDriver() {
		return null;
	}

	@Override
	public List<UpDriver> getParent() {
		return null;
	}

	@Override
	public void init(Gateway gateway, InitialProperties properties,
			String instanceId) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void registerListener(Call serviceCall, Response serviceResponse,
			CallContext messageContext) {
	}

	@Override
	public void unregisterListener(Call serviceCall, Response serviceResponse,
			CallContext messageContext) {
	}
}
