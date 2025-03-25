package com.dataquadinc.dto;

public class CandidateWithDetailsDto {
    private CandidateDto candidateDto;
    private String clientName;
    private String recruiterName;

    // Constructor
    public CandidateWithDetailsDto(CandidateDto candidateDto, String clientName, String recruiterName) {
        this.candidateDto = candidateDto;
        this.clientName = clientName;
        this.recruiterName = recruiterName;
    }

    // Getters and setters
    public CandidateDto getCandidateDto() {
        return candidateDto;
    }

    public void setCandidateDto(CandidateDto candidateDto) {
        this.candidateDto = candidateDto;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }
}

