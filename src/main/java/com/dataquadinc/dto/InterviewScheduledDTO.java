package com.dataquadinc.dto;

import java.time.LocalDateTime;

public interface InterviewScheduledDTO {
    String getCandidateId();
    String getFullName();
    String getCandidateEmailId();
    String getContactNumber();
    String getQualification();
    String getSkills();
    String getInterviewStatus();
    String getInterviewLevel();
    LocalDateTime getInterviewDateTime();
    String getJobId();
    String getJobTitle();
    String getClientName(); // Add this field
}
