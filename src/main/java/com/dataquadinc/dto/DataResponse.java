package com.dataquadinc.dto;

public class DataResponse {
    private String jobId;

    public DataResponse(String jobId) {
        this.jobId = jobId;
    }

    // Getter and Setter
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
