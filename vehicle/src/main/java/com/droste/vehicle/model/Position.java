package com.droste.vehicle.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Position {

	@Id //TODO wrong, use combined key with sessionid (extra class) or introduce ID.
	private long timestamp;

	@JsonIgnore
	@ManyToOne(cascade = CascadeType.ALL)
	private Session session;

	private double latitude;
	private double longitude;
	/** cardinal points */
	private int heading;

	public Position(long timestamp, double latitude, double longitude, int heading, Session session) {
		super();
		this.timestamp = timestamp;
		this.latitude = latitude;
		this.longitude = longitude;
		this.heading = heading;
		this.session = session;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public int getHeading() {
		return heading;
	}

	public void setHeading(int heading) {
		this.heading = heading;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	/** private constructor for JPA */
	@SuppressWarnings("unused")
	private Position() {
	}

	@Override
	public String toString() {
		return "Position [timestamp=" + timestamp + ", latitude=" + latitude + ", longitude=" + longitude + ", heading="
				+ heading + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + heading;
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (heading != other.heading)
			return false;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

}