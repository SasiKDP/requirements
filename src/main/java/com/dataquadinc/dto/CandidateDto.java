package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateDto {
    private String candidateId;
    private String candidateName;
    private String email;
    private String interviewStatus;
}

