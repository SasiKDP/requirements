
package com.dataquadinc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ignore null fields in JSON response
@Data
@NoArgsConstructor  // Default constructor for JSON serialization
@AllArgsConstructor // Constructor with all fields
public class BdmInterviewDTO {
    private String candidateId;
    private String fullName;
    private String email;
    private String contactNumber;
    private String qualification;
    private String skills;
    private String interviewStatus;
    private String interviewLevel;
    private String interviewDateTime;
}
