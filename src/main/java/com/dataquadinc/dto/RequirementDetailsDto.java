package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequirementDetailsDto {

    private RequirementsDto requirement;
    private List<CandidateDto> submittedCandidates;
    private List<InterviewCandidateDto> interviewScheduledCandidates;
    private List<PlacementDto> placements;

    public RequirementDetailsDto(RequirementsDto requirement,
                                 List<CandidateDto> submittedCandidates,
                                 List<InterviewCandidateDto> interviewScheduledCandidates,
                                 List<PlacementDto> placements) {
        this.requirement = requirement;
        this.submittedCandidates = submittedCandidates;
        this.interviewScheduledCandidates = interviewScheduledCandidates;
        this.placements = placements;
    }


    public RequirementsDto getRequirement() {
        return requirement;
    }

    public void setRequirement(RequirementsDto requirement) {
        this.requirement = requirement;
    }

    public List<CandidateDto> getSubmittedCandidates() {
        return submittedCandidates;
    }

    public void setSubmittedCandidates(List<CandidateDto> submittedCandidates) {
        this.submittedCandidates = submittedCandidates;
    }

    public List<InterviewCandidateDto> getInterviewScheduledCandidates() {
        return interviewScheduledCandidates;
    }

    public void setInterviewScheduledCandidates(List<InterviewCandidateDto> interviewScheduledCandidates) {
        this.interviewScheduledCandidates = interviewScheduledCandidates;
    }

    public List<PlacementDto> getPlacements() {
        return placements;
    }

    public void setPlacements(List<PlacementDto> placements) {
        this.placements = placements;
    }
}