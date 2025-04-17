package com.dataquadinc.dto;

import java.util.List;

public class CandidateStatsResponse {

    private List<UserStatsDTO> userStats;

    public CandidateStatsResponse() {}

    public CandidateStatsResponse(List<UserStatsDTO> userStats) {
        this.userStats = userStats;
    }

    public List<UserStatsDTO> getUserStats() {
        return userStats;
    }

    public void setUserStats(List<UserStatsDTO> userStats) {
        this.userStats = userStats;
    }
}
