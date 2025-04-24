package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlacementDto {
    private String placementId;
    private String consultantName;
    private String sales;
    private String technology;
    private String recruiter;
    private String client;
    private String vendor;
    private String employmentType;
}

