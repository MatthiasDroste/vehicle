package com.droste.vehicle.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Session {

	@Id
	private String sessionId;

	private long timestamp = 0L;

	@JsonIgnore
	@ManyToOne
	private Vehicle vehicle;

	@OneToMany(mappedBy = "session")
	@OrderBy("timestamp DESC")
	private List<Position> positions = new ArrayList<>();

	public Session(String sessionId, long timestamp, Vehicle vehicle) {
		super();
		this.sessionId = sessionId;
		this.timestamp = timestamp;
		this.vehicle = vehicle;
	}

	public Session(String sessionId, Vehicle vehicle) {
		super();
		this.sessionId = sessionId;
		this.vehicle = vehicle;
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
		if (this.timestamp < input.getTimestamp())
			this.timestamp = input.getTimestamp();
	}

	/** needed for JPA only */
	@SuppressWarnings("unused")
	private Session() {
	}

}
