package org.ubimon.server.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * A position in the world.
 * 
 * @author Luciano Santos
 */
@Embeddable
public class Position {
	@Column(nullable = false)
	private Double latitude;
	@Column(nullable = false)
	private Double longitude;
	@Column(nullable = false)
	private Double delta;

	public Position() {
	}

	public Position(double latitude, double longitude, double delta) {
		setLatitude(latitude);
		setLongitude(longitude);
		setDelta(delta);
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

	public boolean withinRange(Position other, double range) {
		double d = haversine(latitude, longitude, other.latitude, other.longitude);
		return (d - delta - other.delta) < range;
	}

	/**
	 * Uses Haversine algorithm to calculate the distance between two given
	 * coordinates.
	 * 
	 * @param lt1
	 *            First coordinate's latitude.
	 * @param lg1
	 *            First coordinate's longitude.
	 * @param lt2
	 *            Second coordinate's latitude.
	 * @param lg2
	 *            Second coordinate's longitude.
	 * 
	 * @return The distance, in meters.
	 */
	private static double haversine(double lt1, double lg1, double lt2, double lg2) {
		double dlong = (lg2 - lg1) * DEGREES_TO_RAD;
		double dlat = (lt2 - lt1) * DEGREES_TO_RAD;
		double a = Math.pow(Math.sin(dlat / 2D), 2D)
				+ Math.cos(lt1 * DEGREES_TO_RAD)
				* Math.cos(lt2 * DEGREES_TO_RAD)
				* Math.pow(Math.sin(dlong / 2D), 2D);
		double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
		double d = EARTH_RADIUS_M * c;

		return d;
	}

	private static final double EARTH_RADIUS_M = 6378137.0D;
	private static final double DEGREES_TO_RAD = (Math.PI / 180D);
}
