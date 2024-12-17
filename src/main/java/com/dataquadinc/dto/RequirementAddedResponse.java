package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequirementAddedResponse {
	private String jobId;
	private String jobTitle;
	private String successMessage;

	// Constructor with 3 arguments
	public RequirementAddedResponse(String jobId, String jobTitle, String successMessage) {
		this.jobId = jobId;
		this.jobTitle = jobTitle;
		this.successMessage = successMessage;
	}

	// Optional: Getter and Setter methods (if you don't use @Data annotation)
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getSuccessMessage() {
		return successMessage;
	}

	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}
}
