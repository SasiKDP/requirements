package com.dataquadinc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)  // Exclude null values in JSON output
public class ClientJobDetailsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<JobDTO> jobs;
    private Map<String, List<RecruiterInfoDto>> recruiters; // ✅ Correct type
    private Map<String, Map<String, List<CandidateDto>>> submittedCandidates;
    private Map<String, Map<String, List<InterviewCandidateDto>>> interviewScheduledCandidates;

    public ClientJobDetailsDTO() {} // Default constructor

    public ClientJobDetailsDTO(List<JobDTO> jobs,
                               Map<String, List<RecruiterInfoDto>> recruiters,
                               Map<String, Map<String, List<CandidateDto>>> submittedCandidates,
                               Map<String, Map<String, List<InterviewCandidateDto>>> interviewScheduledCandidates) {
        this.jobs = jobs;
        this.recruiters = recruiters;
        this.submittedCandidates = submittedCandidates;
        this.interviewScheduledCandidates = interviewScheduledCandidates;
    }

    // ✅ Fixed return type
    public List<JobDTO> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobDTO> jobs) {
        this.jobs = jobs;
    }

    // ✅ Fixed method to return correct type
    public Map<String, List<RecruiterInfoDto>> getRecruiters() {
        return recruiters;
    }

    // ✅ Fixed setter parameter type
    public void setRecruiters(Map<String, List<RecruiterInfoDto>> recruiters) {
        this.recruiters = (recruiters != null) ? recruiters : new HashMap<>();
    }

    public Map<String, Map<String, List<CandidateDto>>> getSubmittedCandidates() {
        return submittedCandidates;
    }

    public void setSubmittedCandidates(Map<String, Map<String, List<CandidateDto>>> submittedCandidates) {
        this.submittedCandidates = (submittedCandidates != null) ? submittedCandidates : new HashMap<>();
    }

    public Map<String, Map<String, List<InterviewCandidateDto>>> getInterviewScheduledCandidates() {
        return interviewScheduledCandidates;
    }

    public void setInterviewScheduledCandidates(Map<String, Map<String, List<InterviewCandidateDto>>> interviewScheduledCandidates) {
        this.interviewScheduledCandidates = (interviewScheduledCandidates != null) ? interviewScheduledCandidates : new HashMap<>();
    }

    @Override
    public String toString() {
        return "ClientJobDetailsDTO{" +
                "jobs=" + jobs +
                ", recruiters=" + recruiters +
                ", submittedCandidates=" + submittedCandidates +
                ", interviewScheduledCandidates=" + interviewScheduledCandidates +
                '}';
    }
}
