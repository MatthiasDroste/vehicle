package com.droste.vehicle.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

	List<Position> findAllBySessionSessionId(String sessionId);
}
