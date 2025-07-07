package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InProgressRequirementDTO {
    private String recruiterId;
    private String recruiterName;
    private String jobId;
    private String clientName;
    private String bdm;
    private String teamlead;
    private String technology;
    private LocalDate postedDate;
    private LocalDateTime updatedDateTime;
    private long numberOfSubmissions;
}
