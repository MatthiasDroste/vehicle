package com.droste.vehicle.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "timestamp", "sessionId" }) })
public class Position {

	@JsonIgnore
	@Id
	@GeneratedValue
	private Long id;

	private long timestamp;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "sessionId")
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
}