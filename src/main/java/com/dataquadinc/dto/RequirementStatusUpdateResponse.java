package com.dataquadinc.dto;

import java.util.Set;

public class RequirementStatusUpdateResponse {

    private String jobId;
    private String updatedStatus;
    private Set<String> assignedRecruiters;

    // Constructor (without the message field)
    public RequirementStatusUpdateResponse(String jobId, String updatedStatus, Set<String> assignedRecruiters, String s) {
        this.jobId = jobId;
        this.updatedStatus = updatedStatus;
        this.assignedRecruiters = assignedRecruiters;
    }

    // Getters and Setters
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getUpdatedStatus() {
        return updatedStatus;
    }

    public void setUpdatedStatus(String updatedStatus) {
        this.updatedStatus = updatedStatus;
    }

    public Set<String> getAssignedRecruiters() {
        return assignedRecruiters;
    }

    public void setAssignedRecruiters(Set<String> assignedRecruiters) {
        this.assignedRecruiters = assignedRecruiters;
    }
}
