
package com.dataquadinc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BdmEmployeeDTO {
    @JsonProperty("employeeId")
    private String employeeId;

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("roles")
    private String roles;

    @JsonProperty("email")
    private String email;

    @JsonProperty("status")
    private String status;

    @JsonProperty("clientCount")
    private long clientCount;

    @JsonProperty("requirementsCount")
    private long requirementsCount;

    @JsonProperty("submissionCount")
    private long submissionCount;

    @JsonProperty("interviewCount")
    private long interviewCount;

    @JsonProperty("placementCount")
    private long placementCount;
}
