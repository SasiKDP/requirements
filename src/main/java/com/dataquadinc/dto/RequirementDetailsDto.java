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
}