package com.dataquadinc.dto;

import java.util.List;

public class CandidateStatsResponse {

    private List<EmployeeCandidateDTO> employees;
    private List<TeamleadCandidateDTO> teamleads;

    // ✅ No-args constructor required by Jackson
    public CandidateStatsResponse() {}

    // ✅ All-args constructor for convenience
    public CandidateStatsResponse(List<EmployeeCandidateDTO> employees, List<TeamleadCandidateDTO> teamleads) {
        this.employees = employees;
        this.teamleads = teamleads;
    }

    // ✅ Getters and setters
    public List<EmployeeCandidateDTO> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeCandidateDTO> employees) {
        this.employees = employees;
    }

    public List<TeamleadCandidateDTO> getTeamleads() {
        return teamleads;
    }

    public void setTeamleads(List<TeamleadCandidateDTO> teamleads) {
        this.teamleads = teamleads;
    }
}
