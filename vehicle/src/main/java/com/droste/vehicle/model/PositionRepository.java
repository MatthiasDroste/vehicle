package com.droste.vehicle.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long>{
	//findbyID already in superclass
	Position findByTimestampAndSession(long timestamp, Session session);
}
	