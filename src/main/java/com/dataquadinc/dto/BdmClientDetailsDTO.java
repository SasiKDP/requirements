package com.dataquadinc.dto;

import java.util.List;
import java.util.Map;

public class BdmClientDetailsDTO {
    private List<BdmDetailsDto> bdmDetails;
    private List<BdmClientDto> clientDetails;
    private Map<String, List<BdmSubmissionDTO>> submissions;
    private Map<String, List<BdmInterviewDTO>> interviews;
    private Map<String, List<BdmPlacementDTO>> placements;

    public BdmClientDetailsDTO(List<BdmDetailsDto> bdmDetails,
                               List<BdmClientDto> clientDetails,
                               Map<String, List<BdmSubmissionDTO>> submissions,
                               Map<String, List<BdmInterviewDTO>> interviews,
                               Map<String, List<BdmPlacementDTO>> placements) {
        this.bdmDetails = bdmDetails;
        this.clientDetails = clientDetails;
        this.submissions = submissions;
        this.interviews = interviews;
        this.placements = placements;
    }

    public List<BdmDetailsDto> getBdmDetails() {
        return bdmDetails;
    }

    public List<BdmClientDto> getClientDetails() {
        return clientDetails;
    }

    public Map<String, List<BdmSubmissionDTO>> getSubmissions() {
        return submissions;
    }

    public Map<String, List<BdmInterviewDTO>> getInterviews() {
        return interviews;
    }

    public Map<String, List<BdmPlacementDTO>> getPlacements() {
        return placements;
    }
}