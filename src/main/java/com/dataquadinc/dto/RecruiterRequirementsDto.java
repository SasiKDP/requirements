package com.dataquadinc.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Lombok generates the public no-args constructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecruiterRequirementsDto {

	private String jobId;
	private String jobTitle;
	private String clientName;
	private String jobDescription;
	private String jobType;
	private String location;
	private String jobMode;
	private String experienceRequired;
	private String noticePeriod;
	private String relevantExperience;
	private String qualification;
	private String salaryPackage;
	private int noOfPositions;
	private LocalDateTime requirementAddedTimeStamp;
	private String status;
	private String assignedBy;
}
