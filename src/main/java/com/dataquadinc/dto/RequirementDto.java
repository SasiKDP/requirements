package com.dataquadinc.dto;

public class RequirementDto {
    private String recruiterName;
    private String clientName;
    private String jobId;
    private String jobTitle;
    private String assignedBy;
    private String location;
    private String noticePeriod;

    public RequirementDto(String recruiterName, String clientName, String jobId, String jobTitle,
                          String assignedBy, String location, String noticePeriod) {
        this.recruiterName = recruiterName;
        this.clientName = clientName;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.assignedBy = assignedBy;
        this.location = location;
        this.noticePeriod = noticePeriod;
    }

    // Getters and Setters

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNoticePeriod() {
        return noticePeriod;
    }

    public void setNoticePeriod(String noticePeriod) {
        this.noticePeriod = noticePeriod;
    }
}
