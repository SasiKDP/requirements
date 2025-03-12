package com.dataquadinc.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecruiterRequirementsDto {

	private String jobId;
	private String jobTitle;
	private String clientName;
	private String jobDescription;
//	private byte[] jobDescriptionBlob;  // Add the jobDescriptionBlob field as byte array
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

	// No-argument constructor
	public RecruiterRequirementsDto() {
	}

	// Parameterized constructor (already provided)
	public RecruiterRequirementsDto(String jobId, String jobTitle, String clientName, String jobDescription, byte[] jobDescriptionBlob, String jobType, String location, String jobMode, String experienceRequired, String noticePeriod, String relevantExperience, String qualification, String salaryPackage, int noOfPositions, LocalDateTime requirementAddedTimeStamp, String status, String assignedBy) {
		this.jobId = jobId;
		this.jobTitle = jobTitle;
		this.clientName = clientName;
		this.jobDescription = jobDescription;
		this.jobType = jobType;
		this.location = location;
		this.jobMode = jobMode;
		this.experienceRequired = experienceRequired;
		this.noticePeriod = noticePeriod;
		this.relevantExperience = relevantExperience;
		this.qualification = qualification;
		this.salaryPackage = salaryPackage;
		this.noOfPositions = noOfPositions;
		this.requirementAddedTimeStamp = requirementAddedTimeStamp;
		this.status = status;
		this.assignedBy = assignedBy;
	}
}
