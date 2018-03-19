package com.droste.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VehicleNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8565714684398760798L;

	public VehicleNotFoundException(String vehicleId) {
		super("Could not find vehicle '" + vehicleId + "'.");
	}
}
