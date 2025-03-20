package com.dataquadinc.dto;

public class JobDTO {
    private String jobId;
    private String jobTitle;
    private String clientId;

    // Default constructor (important for Jackson)
    public JobDTO() {
    }

    // Parameterized constructor
    public JobDTO(String jobId, String jobTitle, String clientId) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.clientId = clientId;
    }

    // Getters and Setters
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
