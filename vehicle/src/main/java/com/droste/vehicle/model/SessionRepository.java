package com.droste.vehicle.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String>{
	
	/** get all sessions of a vehicle in correct ordering */
	List<Session> findAllByVehicleOrderBySessionIdAsc(Vehicle vehicle);
	
}
	