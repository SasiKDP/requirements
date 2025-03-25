

package com.dataquadinc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ignore null fields in JSON response
@Data
@NoArgsConstructor  // Default constructor for JSON serialization
@AllArgsConstructor // Constructor with all fields
public class BdmSubmissionDTO {
    private String candidateId;
    private String fullName;
    private String candidateEmailId;
    private String contactNumber;
    private String qualification;
    private String skills;
    private String overallFeedback;
    private String jobId;
    private String jobTitle;
    private String clientName;
}
