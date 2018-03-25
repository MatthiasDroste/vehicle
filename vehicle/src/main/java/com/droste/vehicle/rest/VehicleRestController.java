package com.droste.vehicle.rest;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.droste.vehicle.exception.DuplicatePositionException;
import com.droste.vehicle.exception.VehicleNotFoundException;
import com.droste.vehicle.model.Position;
import com.droste.vehicle.model.PositionRepository;
import com.droste.vehicle.model.Session;
import com.droste.vehicle.model.Vehicle;
import com.droste.vehicle.model.VehicleRepository;

/**
 * The web-service shall then provide APIs to: <br>
 * <ul>
 * <li>Get the last position of a certain vehicle</li>
 * <li>Get a specific vehicle</li>
 * <li>add a new vehicle/session/position</li>
 * </ul>
 */
@RestController
public class VehicleRestController {
	private final VehicleRepository vehicleRepository;
	private final PositionRepository positionRepository;

	@Autowired
	public VehicleRestController(VehicleRepository vehicleRepository, PositionRepository positionRepository) {
		super();
		this.vehicleRepository = vehicleRepository;
		this.positionRepository = positionRepository;
	}

	/**
	 * Get the last position of a certain vehicle
	 */
	@GetMapping("/vehicle/{vehicleId}/position/latest")
	public Position getLastPosition(@PathVariable String vehicleId) {
		// relational storage suboptimal here. Could be optimized for this one use case
		// by storing the latest pos directly in the vehicle.
		Vehicle vehicle = findVehicleOrThrowNotFoundException(vehicleId);
		List<Session> sessions = vehicle.getSessions();
		Session latestSession = sessions.stream().min((s1, s2) -> Long.compare(s1.getTimestamp(), s2.getTimestamp()))
				.get();
		return latestSession.getPositions().get(0);
	}

	/** get a specific vehicle */
	@GetMapping("/vehicle/{vehicleId}")
	public Vehicle getVehicle(@PathVariable String vehicleId) {
		return findVehicleOrThrowNotFoundException(vehicleId);
	}

	/**
	 * add a new position <br>
	 * new vehicles or sessions are created on the fly
	 */
	@PostMapping("/vehicle/{vehicleId}/session/{sessionId}/position")
	public ResponseEntity<?> add(@PathVariable String vehicleId, @PathVariable String sessionId,
			@RequestBody Position position) {
		Vehicle vehicle = findOrCreateVehicleAndSession(vehicleId, sessionId);
		Session session = vehicle.getSession(sessionId);
		session.addPosition(position);
		this.vehicleRepository.save(vehicle);

		position.setSession(session);
		try {
			this.positionRepository.save(position);
		} catch (DataIntegrityViolationException ex) {
			throw new DuplicatePositionException(position);
		}

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(position.getTimestamp()).toUri();

		return ResponseEntity.created(location).build();
	}

	private Vehicle findOrCreateVehicleAndSession(String vehicleId, String sessionId) {
		Vehicle vehicle = this.vehicleRepository.findOne(vehicleId);
		if (vehicle == null) {
			vehicle = new Vehicle(vehicleId, sessionId);
		} else if (vehicle.getSession(sessionId) == null) {
			vehicle.addSession(new Session(sessionId, vehicle));
		}
		return vehicle;
	}

	private Vehicle findVehicleOrThrowNotFoundException(String vehicleId) {
		Vehicle vehicle = this.vehicleRepository.findOne(vehicleId);
		if (vehicle == null)
			throw new VehicleNotFoundException(vehicleId);
		return vehicle;
	}
}
