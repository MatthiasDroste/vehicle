package com.droste.vehicle.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class Vehicle {

	@Id
	private String vin;

	@OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy("timestamp DESC")
	private List<Session> sessions = new ArrayList<>();

	public Vehicle(String vin, String sessionId) {
		super();
		this.vin = vin;
		sessions.add(new Session(sessionId, this));
	}

	public Vehicle(String vin) {
		super();
		this.vin = vin;
	}

	public String getId() {
		return vin;
	}

	public void setId(String id) {
		this.vin = id;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}
	
	public void addSession(Session session) {
		this.sessions.add(session);
	}
	

	public Session getSession(String sessionId) {
		for (Session session : getSessions()) {
			if (session.getSessionId().equals(sessionId)) {
				return session;
			}
		}
		return null;

	}

	/** needed for JPA only */
	@SuppressWarnings("unused")
	private Vehicle() {
	}

}