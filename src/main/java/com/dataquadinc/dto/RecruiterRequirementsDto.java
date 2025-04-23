package com.dataquadinc.dto;

import java.time.Duration;
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

	private int  noOfPositions;

	private LocalDateTime requirementAddedTimeStamp;

	private String status;
	private String assignedBy;
	public String getAge() {
		if (requirementAddedTimeStamp == null) {
			return "N/A";
		}

		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.between(requirementAddedTimeStamp, now);

		long days = duration.toDays();
		long hours = duration.toHours() % 24;

		return days + " days " + hours + " hours";
	}

}
