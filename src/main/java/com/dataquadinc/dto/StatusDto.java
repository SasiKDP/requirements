package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusDto {
	private String jobId;
	private String status;
	private Set<String> recruiterIds;  // ✅ Add this field
}

