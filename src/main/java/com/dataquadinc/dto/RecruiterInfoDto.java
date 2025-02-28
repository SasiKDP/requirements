package com.dataquadinc.dto;

public class RecruiterInfoDto {
    private String recruiterId;
    private String recruiterName;

    public RecruiterInfoDto(String recruiterId, String recruiterName) {
        this.recruiterId = recruiterId;
        this.recruiterName = recruiterName;
    }

    // Getters & Setters
    public String getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(String recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }
}



