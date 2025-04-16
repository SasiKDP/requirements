package com.dataquadinc.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.dataquadinc.dto.ResponseBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(RequirementAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleRequirementAlreadyExists(RequirementAlreadyExistsException ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(),
				LocalDateTime.now());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
		// Return a meaningful response with a 400 Bad Request status
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(RequirementNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleRequirementNotFound(RequirementNotFoundException ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(),
				LocalDateTime.now());
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(NoJobsAssignedToRecruiterException.class)
	public ResponseEntity<ErrorResponse> handleNoJobsAssignedToRecruiter(NoJobsAssignedToRecruiterException ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(),
				LocalDateTime.now());
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
		// Log the stack trace for debugging
		logger.error("An unexpected error occurred: ", ex);

		// Create a detailed error response
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"An unexpected error occurred: " + ex.getMessage(), LocalDateTime.now());
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ClientAlreadyExistsException.class)
	public ResponseEntity<ResponseBean> handleClientAlreadyExists(ClientAlreadyExistsException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ResponseBean.errorResponse("Client already exists", ex.getMessage()));
	}

	@ExceptionHandler(DateRangeValidationException.class)
	public ResponseEntity<Map<String, String>> handleDateRangeValidationException(DateRangeValidationException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
}
