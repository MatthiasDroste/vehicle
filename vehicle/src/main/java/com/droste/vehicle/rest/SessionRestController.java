package com.droste.vehicle.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.droste.vehicle.exception.SessionNotFoundException;
import com.droste.vehicle.exception.VehicleNotFoundException;
import com.droste.vehicle.model.Session;
import com.droste.vehicle.model.SessionRepository;
import com.droste.vehicle.model.Vehicle;
import com.droste.vehicle.model.VehicleRepository;

/**
 * The session web-service shall then provide APIs to:
 * <ul>
 * <li>get all sessions of a vehicle in correct ordering</li>
 * <li>get a single session as an ordered list of the received positions by
 * timestamp</li>
 * </ul>
 */
@RestController
public class SessionRestController {
	private final SessionRepository sessionRepository;
	private final VehicleRepository vehicleRepository;

	@Autowired
	public SessionRestController(SessionRepository sessionRepository, VehicleRepository vehicleRepository) {
		super();
		this.sessionRepository = sessionRepository;
		this.vehicleRepository = vehicleRepository;
	}

	/** get all sessions of a vehicle in correct ordering */
	@GetMapping("/vehicle/{vehicleId}/session")
	public List<Session> getAllSessions(@PathVariable String vehicleId) {
		Vehicle vehicle = findVehicleOrThrowNotFoundException(vehicleId);
		return this.sessionRepository.findAllByVehicleOrderBySessionIdAsc(vehicle);
	}

	/**
	 * Get a single session as an ordered list of the received positions by
	 * timestamp <br>
	 * TODO a bit dirty: consistent but inconvenient would be
	 * /vehicle/{vehicleId}/session...
	 */
	@GetMapping("/vehicle/session/{sessionId}")
	public Session getPositionsOfSession(@PathVariable String sessionId) {
		Session session = this.sessionRepository.findOne(sessionId);
		if (session == null) throw new SessionNotFoundException(sessionId);
		return session;
	}
	
	//TODO copied from vehicle..
	private Vehicle findVehicleOrThrowNotFoundException(String vehicleId) {
		Vehicle vehicle = this.vehicleRepository.findOne(vehicleId);
		if (vehicle == null) throw new VehicleNotFoundException(vehicleId);
		return vehicle;
	}
	
	
}
