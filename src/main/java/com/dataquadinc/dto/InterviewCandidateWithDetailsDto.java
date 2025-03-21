package com.dataquadinc.dto;

public class InterviewCandidateWithDetailsDto {
    private InterviewCandidateDto interviewCandidateDto;
    private String clientName;
    private String recruiterName;

    // Constructor
    public InterviewCandidateWithDetailsDto(InterviewCandidateDto interviewCandidateDto, String clientName, String recruiterName) {
        this.interviewCandidateDto = interviewCandidateDto;
        this.clientName = clientName;
        this.recruiterName = recruiterName;
    }

    // Getters and setters
    public InterviewCandidateDto getInterviewCandidateDto() {
        return interviewCandidateDto;
    }

    public void setInterviewCandidateDto(InterviewCandidateDto interviewCandidateDto) {
        this.interviewCandidateDto = interviewCandidateDto;
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
