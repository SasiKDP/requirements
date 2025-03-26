package com.dataquadinc.dto;

import java.util.List;
import java.util.Map;

public class CandidateResponseDTO {
    private Map<String, List<SubmittedCandidateDTO>> submittedCandidates;
    private Map<String, List<InterviewScheduledDTO>> scheduledInterviews;
    private Map<String, List<PlacementDetailsDTO>> placements;
    private Map<String, List<JobDetailsDTO>> jobDetails;
    private Map<String, ClientDetailsDTO> clientDetails; // New field for client details

    // Constructor
    public CandidateResponseDTO(Map<String, List<SubmittedCandidateDTO>> submittedCandidates,
                                Map<String, List<InterviewScheduledDTO>> scheduledInterviews,
                                Map<String, List<PlacementDetailsDTO>> placements,
                                Map<String, List<JobDetailsDTO>> jobDetails,
                                Map<String, ClientDetailsDTO> clientDetails) {
        this.submittedCandidates = submittedCandidates;
        this.scheduledInterviews = scheduledInterviews;
        this.placements = placements;
        this.jobDetails = jobDetails;
        this.clientDetails = clientDetails;
    }

    public Map<String, List<SubmittedCandidateDTO>> getSubmittedCandidates() {
        return submittedCandidates;
    }

    public void setSubmittedCandidates(Map<String, List<SubmittedCandidateDTO>> submittedCandidates) {
        this.submittedCandidates = submittedCandidates;
    }

    public Map<String, List<InterviewScheduledDTO>> getScheduledInterviews() {
        return scheduledInterviews;
    }

    public void setScheduledInterviews(Map<String, List<InterviewScheduledDTO>> scheduledInterviews) {
        this.scheduledInterviews = scheduledInterviews;
    }

    public Map<String, List<PlacementDetailsDTO>> getPlacements() {
        return placements;
    }

    public void setPlacements(Map<String, List<PlacementDetailsDTO>> placements) {
        this.placements = placements;
    }

    public Map<String, List<JobDetailsDTO>> getJobDetails() {
        return jobDetails;
    }

    public void setJobDetails(Map<String, List<JobDetailsDTO>> jobDetails) {
        this.jobDetails = jobDetails;
    }

    public Map<String, ClientDetailsDTO> getClientDetails() {
        return clientDetails;
    }

    public void setClientDetails(Map<String, ClientDetailsDTO> clientDetails) {
        this.clientDetails = clientDetails;
    }
// Getters & Setters

}
