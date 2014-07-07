package org.ubimon.server.persistence;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Client implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id; // unique database id
	private String name; // informed client name
	private String deviceName; // caller UpDevice name
	private String deviceDesc; // caller UpDevice JSON description
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar lastUpdate; // last time this client was connected
	private Double latitute; // last informed client's latitute
	private Double longitude; // last informed client's longitude
	private Double delta; // last informed client's position accuracy
	private String metadata; // client metadata string, app dependent

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceDesc() {
		return deviceDesc;
	}

	public void setDeviceDesc(String deviceDesc) {
		this.deviceDesc = deviceDesc;
	}

	public Calendar getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Calendar lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Double getLatitute() {
		return latitute;
	}

	public void setLatitute(Double latitute) {
		this.latitute = latitute;
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

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
}
