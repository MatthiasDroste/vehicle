package com.droste.vehicle.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class Vehicle {

	@Id
	private String vin;

	@OneToMany(mappedBy = "vehicle", fetch = FetchType.EAGER)//, cascade=CascadeType.ALL)
	@OrderBy("timestamp DESC")
	private List<Session> sessions;
	
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

	public Session getSession(String sessionId) {
		for (Session session : getSessions()) {
			if (session.getSessionId() == sessionId) {
				return session;
			}
		}
		return new Session(sessionId);

	}

	/** needed for JPA only */
	@SuppressWarnings("unused")
	private Vehicle() {
	}
	@Override
	public String toString() {
		return "Vehicle [vin=" + vin + ", sessions=" + sessions + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sessions == null) ? 0 : sessions.hashCode());
		result = prime * result + ((vin == null) ? 0 : vin.hashCode());
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
		Vehicle other = (Vehicle) obj;
		if (sessions == null) {
			if (other.sessions != null)
				return false;
		} else if (!sessions.equals(other.sessions))
			return false;
		if (vin == null) {
			if (other.vin != null)
				return false;
		} else if (!vin.equals(other.vin))
			return false;
		return true;
	}

}