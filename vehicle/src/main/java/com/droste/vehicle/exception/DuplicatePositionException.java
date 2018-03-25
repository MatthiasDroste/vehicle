package com.droste.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.droste.vehicle.model.Position;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePositionException extends RuntimeException {
	
	private static final long serialVersionUID = -2750804736723842753L;

	public DuplicatePositionException(Position position) {
		super("Entry already exists in database with timestamp: " + position.getTimestamp() + " and sessionId: "
				+ position.getSession().getSessionId());
	}


}
