package com.dataquadinc.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RecruiterDetailsDTO {
    private List<RecruiterInfoDto> recruiters;
    private Map<String, List<CandidateDto>> submitted_Candidates;
    private Map<String, List<InterviewCandidateDto>> interview_Scheduled_Candidates;

    public RecruiterDetailsDTO(List<RecruiterInfoDto> recruiters, Map<String, List<CandidateDto>> submitted_Candidates, Map<String, List<InterviewCandidateDto>> interview_Scheduled_Candidates) {
        this.recruiters = recruiters;
        this.submitted_Candidates = submitted_Candidates;
        this.interview_Scheduled_Candidates = interview_Scheduled_Candidates;
    }

    public List<RecruiterInfoDto> getRecruiters() {
        return recruiters;
    }

    public void setRecruiters(List<RecruiterInfoDto> recruiters) {
        this.recruiters = recruiters;
    }

    public Map<String, List<CandidateDto>> getSubmitted_Candidates() {
        return submitted_Candidates;
    }

    public void setSubmitted_Candidates(Map<String, List<CandidateDto>> submitted_Candidates) {
        this.submitted_Candidates = submitted_Candidates;
    }

    public Map<String, List<InterviewCandidateDto>> getInterview_Scheduled_Candidates() {
        return interview_Scheduled_Candidates;
    }

    public void setInterview_Scheduled_Candidates(Map<String, List<InterviewCandidateDto>> interview_Scheduled_Candidates) {
        this.interview_Scheduled_Candidates = interview_Scheduled_Candidates;
    }

    // Method to get submitted candidates for a specific recruiter
    public List<CandidateDto> getSubmittedCandidates(String recruiterName) {
        return submitted_Candidates.get(recruiterName);
    }

    // Method to get interview scheduled candidates for a specific recruiter
    public List<InterviewCandidateDto> getInterviewScheduledCandidates(String recruiterName) {
        return interview_Scheduled_Candidates.get(recruiterName);
    }
}
