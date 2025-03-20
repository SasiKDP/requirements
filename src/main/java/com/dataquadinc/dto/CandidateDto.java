package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CandidateDto {
    private String candidateId;
    private String candidateName;
    private String recruiterId;  // ✅ Added recruiterId explicitly
    private String email;
    private String interviewStatus;
    private String contactNumber;
    private String qualification;
    private String skills;
    private String overallFeedback;

    // ✅ Constructor with all fields except recruiterId
    public CandidateDto(String candidateId, String candidateName, String email, String interviewStatus,
                        String contactNumber, String qualification, String skills, String overallFeedback) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.email = email;
        this.interviewStatus = interviewStatus;
        this.contactNumber = contactNumber;
        this.qualification = qualification;
        this.skills = skills;
        this.overallFeedback = overallFeedback;
    }

    // ✅ Constructor with all fields including recruiterId
    public CandidateDto(String candidateId, String candidateName, String recruiterId, String email,
                        String interviewStatus, String contactNumber, String qualification,
                        String skills, String overallFeedback) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.recruiterId = recruiterId;
        this.email = email;
        this.interviewStatus = interviewStatus;
        this.contactNumber = contactNumber;
        this.qualification = qualification;
        this.skills = skills;
        this.overallFeedback = overallFeedback;
    }
}
