package com.dataquadinc.dto;

import java.util.Set;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class RequirementsDto 
{
	    @NotNull(message = "Job ID cannot be null")
	    private String jobId;

	    @NotNull(message = "Job Title cannot be null")
	    @Size(min = 3, max = 100, message = "Job Title must be between 3 and 100 characters")
	    private String jobTitle;

	    @NotNull(message = "Client Name cannot be null")
	    @Size(min = 3, max = 100, message = "Client Name must be between 3 and 100 characters")
	    private String clientName;

	    @NotNull(message = "Job Description cannot be null")
	    @Size(min = 10, max = 5000, message = "Job Description must be between 10 and 1000 characters")
	    private String jobDescription;

	    @NotNull(message = "Job Type cannot be null")
	    private String jobType;

	    @NotNull(message = "Location cannot be null")
	    private String location;

	    @NotNull(message = "Job Mode cannot be null")
	    private String jobMode;

	    @NotNull(message = "Experience Required cannot be null")
	    private String experienceRequired;
	    
	    private Set<String> recruiterIds; 
	    
	    private String status;
	    
	    private String remark;


}
