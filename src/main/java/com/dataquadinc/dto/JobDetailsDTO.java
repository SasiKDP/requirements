package com.dataquadinc.dto;



import java.time.LocalDateTime;

public interface JobDetailsDTO {
    String getJobId();
    String getJobTitle();
    String getClientName();
    String getAssignedBy();
    String getStatus();
    Integer getNoOfPositions();
    String getQualification();
    String getJobType();
    String getJobMode();
    LocalDateTime getPostedDate(); // Change from Timestamp to LocalDateTime
}


