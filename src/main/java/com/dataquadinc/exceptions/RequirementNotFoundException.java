package com.dataquadinc.exceptions;

public class RequirementNotFoundException extends RuntimeException {

	public RequirementNotFoundException() {
		super();
	}

	public RequirementNotFoundException(String message) {
		super(message);
	}

}
