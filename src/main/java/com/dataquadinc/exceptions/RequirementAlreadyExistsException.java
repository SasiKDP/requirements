package com.dataquadinc.exceptions;

public class RequirementAlreadyExistsException extends RuntimeException {

	public RequirementAlreadyExistsException() {
		super();
	}

	public RequirementAlreadyExistsException(String message) {
		super(message);
	}

}
