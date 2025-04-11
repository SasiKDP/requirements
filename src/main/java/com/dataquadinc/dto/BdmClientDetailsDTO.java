package com.dataquadinc.dto;

import java.util.List;
import java.util.Map;

public class BdmClientDetailsDTO {
    private List<BdmDetailsDto> bdmDetails;
    private List<BdmClientDto> clientDetails;
    private Map<String, List<RequirementDto>> requirements; // Added requirements field
    private Map<String, List<BdmSubmissionDTO>> submissions;
    private Map<String, List<BdmInterviewDTO>> interviews;
    private Map<String, List<BdmPlacementDTO>> placements;

    public BdmClientDetailsDTO(List<BdmDetailsDto> bdmDetails,
                               List<BdmClientDto> clientDetails,
                               Map<String, List<BdmSubmissionDTO>> submissions,
                               Map<String, List<BdmInterviewDTO>> interviews,
                               Map<String, List<BdmPlacementDTO>> placements,
                               Map<String, List<RequirementDto>> requirements) { // Constructor updated
        this.bdmDetails = bdmDetails;
        this.clientDetails = clientDetails;
        this.submissions = submissions;
        this.interviews = interviews;
        this.placements = placements;
        this.requirements = requirements; // Initialize requirements
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

    public Map<String, List<RequirementDto>> getRequirements() {  // Added getter for requirements
        return requirements;
    }
}
