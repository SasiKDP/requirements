package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtendedRequirementsDto extends RequirementsinfoDto {

    private Map<String, List<CandidateDto>> submittedCandidates;
    private Map<String, List<InterviewCandidateDto>> interviewScheduledCandidates;

    // Constructor copying fields manually without jobDescriptionBlob
    public ExtendedRequirementsDto(RequirementsinfoDto requirement,
                                   Map<String, List<CandidateDto>> submittedCandidates,
                                   Map<String, List<InterviewCandidateDto>> interviewScheduledCandidates) {
        // Call parameterized constructor of RequirementsinfoDto
        super(requirement.getJobId(),
                requirement.getJobTitle(),
                requirement.getClientName(),
                requirement.getJobDescription(),
                requirement.getJobDescriptionFile(),
                requirement.getJobType(),
                requirement.getLocation(),
                requirement.getJobMode(),
                requirement.getExperienceRequired(),
                requirement.getNoticePeriod(),
                requirement.getRelevantExperience(),
                requirement.getQualification(),
                requirement.getSalaryPackage(),
                requirement.getNoOfPositions(),
                requirement.getRequirementAddedTimeStamp(),
                requirement.getRecruiterIds(),
                requirement.getStatus(),
                requirement.getRecruiterName());

        // Initialize additional fields specific to ExtendedRequirementsDto
        this.submittedCandidates = submittedCandidates;
        this.interviewScheduledCandidates = interviewScheduledCandidates;
    }

    public Map<String, List<CandidateDto>> getSubmittedCandidates() {
        return submittedCandidates;
    }

    public void setSubmittedCandidates(Map<String, List<CandidateDto>> submittedCandidates) {
        this.submittedCandidates = submittedCandidates;
    }

    public Map<String, List<InterviewCandidateDto>> getInterviewScheduledCandidates() {
        return interviewScheduledCandidates;
    }

    public void setInterviewScheduledCandidates(Map<String, List<InterviewCandidateDto>> interviewScheduledCandidates) {
        this.interviewScheduledCandidates = interviewScheduledCandidates;
    }
}
