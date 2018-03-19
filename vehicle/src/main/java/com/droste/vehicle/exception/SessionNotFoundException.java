package com.droste.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SessionNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4182323965455414759L;

	public SessionNotFoundException(String sessionId) {
		super("Could not find session '" + sessionId + "'.");
	}
}
