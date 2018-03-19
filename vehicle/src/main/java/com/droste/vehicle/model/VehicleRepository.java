package com.droste.vehicle.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {
	// no special methods necessary, default is good enough
}
