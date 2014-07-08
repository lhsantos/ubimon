package org.ubimon.server.model;

import javax.persistence.Embeddable;

import org.ubimon.server.uos.UosUtil;
import org.unbiquitous.uos.core.messageEngine.messages.Call;

/**
 * A position in the world.
 * 
 * @author Luciano Santos
 */
@Embeddable
public class Position {
	private Double latitude;
	private Double longitude;
	private Double delta;

	public Position() {
	}

	public Position(double latitute, double logitude, double delta) {
		setLatitude(latitude);
		setLongitude(longitude);
		setDelta(delta);
	}

	/**
	 * Extracts position data from a uOS service call's parameters.
	 * 
	 * @param call
	 * @return
	 */
	public static Position extract(Call call) {
		Position p = new Position();
		p.setLatitude(UosUtil.extractDouble(call, "latitude"));
		p.setLongitude(UosUtil.extractDouble(call, "longitude"));
		p.setDelta(UosUtil.extractDouble(call, "delta"));
		return p;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getDelta() {
		return delta;
	}

	public void setDelta(Double delta) {
		this.delta = delta;
	}
}
