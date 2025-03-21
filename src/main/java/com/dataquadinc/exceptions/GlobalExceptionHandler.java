package com.dataquadinc.exceptions;

import java.time.LocalDateTime;

import com.dataquadinc.dto.ResponseBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {



	@ExceptionHandler(RequirementAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleRequirementAlreadyExists(RequirementAlreadyExistsException ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.OK.value(), ex.getMessage(),
				LocalDateTime.now());
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
		// Return a meaningful response with a 400 Bad Request status
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(RequirementNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleRequirementNotFound(RequirementNotFoundException ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.OK.value(), ex.getMessage(),
				LocalDateTime.now());
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	@ExceptionHandler(NoJobsAssignedToRecruiterException.class)
	public ResponseEntity<ErrorResponse> handleNoJobsAssignedToRecruiter(NoJobsAssignedToRecruiterException ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.OK.value(), ex.getMessage(),
				LocalDateTime.now());
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"An unexpected error occurred", LocalDateTime.now());
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ClientAlreadyExistsException.class)
	public ResponseEntity<ResponseBean> handleClientAlreadyExists(ClientAlreadyExistsException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ResponseBean.errorResponse("Client already exists", ex.getMessage()));
	}
}
