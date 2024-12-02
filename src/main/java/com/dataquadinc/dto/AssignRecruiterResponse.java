package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssignRecruiterResponse
{
	private String jobId;
	private String recruiterId;
	private String message;
	

}
