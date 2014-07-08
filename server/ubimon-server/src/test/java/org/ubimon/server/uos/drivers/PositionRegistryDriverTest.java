package org.ubimon.server.uos.drivers;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;

public class PositionRegistryDriverTest {
	PositionRegistryDriver instance = new PositionRegistryDriver();

	@Test
	public void declaresTheInterfaceProperly() {
		assertThat(instance.getDriver().getName()).isEqualTo("ubimon.PositionRegistryDriver");
	}

	@Test
	public void declaresCheckIn() {
		assertThat(instance.getDriver().getServices()).contains(
				new UpService("checkIn")
						.addParameter("clientName", UpService.ParameterType.OPTIONAL)
						.addParameter("latitude", UpService.ParameterType.MANDATORY)
						.addParameter("longitude", UpService.ParameterType.MANDATORY)
						.addParameter("delta", UpService.ParameterType.MANDATORY)
						.addParameter("metadata", UpService.ParameterType.OPTIONAL));
	}

	@Test
	public void declaresUpdate() {
		assertThat(instance.getDriver().getServices()).contains(
				new UpService("update")
						.addParameter("clientId", UpService.ParameterType.MANDATORY)
						.addParameter("latitude", UpService.ParameterType.MANDATORY)
						.addParameter("longitude", UpService.ParameterType.MANDATORY)
						.addParameter("delta", UpService.ParameterType.MANDATORY)
						.addParameter("metadata", UpService.ParameterType.OPTIONAL));
	}

	@Test
	public void declaresCheckOut() {
		assertThat(instance.getDriver().getServices()).contains(
				new UpService("checkOut")
						.addParameter("clientId", UpService.ParameterType.MANDATORY));
	}

	@Test
	public void declaresListNeighbours() {
		assertThat(instance.getDriver().getServices())
				.contains(new UpService("listNeighbours")
						.addParameter("latitude", UpService.ParameterType.MANDATORY)
						.addParameter("longitude", UpService.ParameterType.MANDATORY)
						.addParameter("delta", UpService.ParameterType.MANDATORY)
						.addParameter("range", UpService.ParameterType.OPTIONAL));
	}
}
