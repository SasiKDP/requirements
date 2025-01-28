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

//	@ExceptionHandler(RequirementNotFoundException.class)
//	public ResponseEntity<ErrorResponse> handleRequirementNotFound(RequirementNotFoundException ex) {
//		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.OK.value(), ex.getMessage(),
//				LocalDateTime.now());
//		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
//	}
@ExceptionHandler(RequirementNotFoundException.class)
public ResponseEntity<ResponseBean> handleRequirementNotFound(RequirementNotFoundException ex) {
	// Create the custom response bean
	ResponseBean response = new ResponseBean(
			false,
			ex.getMessage(),
			ex.getClass().getSimpleName(),
			null
	);
	// Return the ResponseEntity with 404 status (Not Found)
	return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
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

}
