package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlacementDto {
    private String candidateId;
    private String candidateName;
    private String email;
    private String contactNumber;
    private String qualification;
    private String overallFeedback;
    private String recruiterName;
}