package com.droste.vehicle.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Session {

	@Id
	private String sessionId;

	private long timestamp;

	@JsonIgnore
	@ManyToOne // (cascade=CascadeType.ALL)
	private Vehicle vehicle;

	@OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
	@OrderBy("timestamp DESC") // TODO or on the getPositions?
	private List<Position> positions;

	public Session(String sessionId, long timestamp, Vehicle vehicle) {
		super();
		this.sessionId = sessionId;
		this.timestamp = timestamp;
		this.vehicle = vehicle;
	}

	public Session(String sessionId) {
		super();
		this.sessionId = sessionId;
		this.positions = new ArrayList<Position>();
	}

	public List<Position> getPositions() {
		return positions;
	}

	public void setPositions(List<Position> positions) {
		this.positions = positions;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void addPosition(Position input) {
		positions.add(input);
		this.timestamp = input.getTimestamp();
	}

	/** needed for JPA only */
	@SuppressWarnings("unused")
	private Session() {
	}

}
