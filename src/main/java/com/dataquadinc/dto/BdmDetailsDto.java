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

    public BdmDetailsDto(String employeeId, String userName, String roles, String email,
                         String designation, String joiningDate, String gender, String dob,
                         String phoneNumber, String personalEmail, String status, String clientName) {
        this.employeeId = employeeId;
        this.userName = userName;
        this.roles = roles;
        this.email = email;
        this.designation = designation;
        this.joiningDate = joiningDate;
        this.gender = gender;
        this.dob = dob;
        this.phoneNumber = phoneNumber;
        this.personalEmail = personalEmail;
        this.status = status;
        this.clientName = clientName;
    }

}
