
package com.dataquadinc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ignore null fields in JSON response
@Data
@NoArgsConstructor  // Default constructor for serialization
@AllArgsConstructor // Constructor with all fields
public class BdmDetailsDto {
    private String employeeId;
    private String userName;
    private String roles;
    private String email;
    private String designation;
    private String joiningDate;
    private String gender;
    private String dob;
    private String phoneNumber;
    private String personalEmail;
    private String status;
    private String clientName;
}
