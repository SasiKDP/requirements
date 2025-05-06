package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlacementDto {
    private String candidateId;
    private String candidateName;
    private String email;
    private String contactNumber;
    private String qualification;
    private String overallFeedback;
    private String recruiterName;

    public PlacementDto(String candidateId, String candidateName, String email,
                        String contactNumber, String qualification,
                        String overallFeedback, String recruiterName) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.qualification = qualification;
        this.overallFeedback = overallFeedback;
        this.recruiterName = recruiterName;
    }


    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
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

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }
}