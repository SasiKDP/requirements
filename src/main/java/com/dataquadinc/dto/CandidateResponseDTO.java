package com.dataquadinc.dto;

import java.util.List;

public class CandidateResponseDTO {

    private List<SubmittedCandidateDTO> submittedCandidates;
    private List<InterviewScheduledDTO> scheduledInterviews;
    private List<JobDetailsDTO> jobDetails;  // Add a new field for job details

    // Constructor
    public CandidateResponseDTO(List<SubmittedCandidateDTO> submittedCandidates,
                                List<InterviewScheduledDTO> scheduledInterviews,
                                List<JobDetailsDTO> jobDetails) {
        this.submittedCandidates = submittedCandidates;
        this.scheduledInterviews = scheduledInterviews;
        this.jobDetails = jobDetails;  // Initialize jobDetails
    }

    // Getters and Setters
    public List<SubmittedCandidateDTO> getSubmittedCandidates() {
        return submittedCandidates;
    }

    public void setSubmittedCandidates(List<SubmittedCandidateDTO> submittedCandidates) {
        this.submittedCandidates = submittedCandidates;
    }

    public List<InterviewScheduledDTO> getScheduledInterviews() {
        return scheduledInterviews;
    }

    public void setScheduledInterviews(List<InterviewScheduledDTO> scheduledInterviews) {
        this.scheduledInterviews = scheduledInterviews;
    }

    public List<JobDetailsDTO> getJobDetails() {
        return jobDetails;
    }

    public void setJobDetails(List<JobDetailsDTO> jobDetails) {
        this.jobDetails = jobDetails;
    }
}

