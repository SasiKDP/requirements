package com.dataquadinc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ignore null fields in JSON response
@Data
@NoArgsConstructor  // Default constructor for JSON serialization
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


    public BdmInterviewDTO(String candidateId, String fullName, String email, String contactNumber,
                           String qualification, String skills, String interviewStatus,
                           String interviewLevel, String interviewDateTime) {
        this.candidateId = candidateId;
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.qualification = qualification;
        this.skills = skills;
        this.interviewStatus = interviewStatus;
        this.interviewLevel = interviewLevel;
        this.interviewDateTime = interviewDateTime;
    }


    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getInterviewStatus() {
        return interviewStatus;
    }

    public void setInterviewStatus(String interviewStatus) {
        this.interviewStatus = interviewStatus;
    }

    public String getInterviewLevel() {
        return interviewLevel;
    }

    public void setInterviewLevel(String interviewLevel) {
        this.interviewLevel = interviewLevel;
    }

    public String getInterviewDateTime() {
        return interviewDateTime;
    }

    public void setInterviewDateTime(String interviewDateTime) {
        this.interviewDateTime = interviewDateTime;
    }
}
