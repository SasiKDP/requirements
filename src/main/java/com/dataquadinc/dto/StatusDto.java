package com.dataquadinc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusDto
{
	private String jobId;
	private String status;
	private String remark;

}
