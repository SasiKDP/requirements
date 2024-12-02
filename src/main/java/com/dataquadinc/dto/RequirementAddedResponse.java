package com.dataquadinc.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequirementAddedResponse 
{
	private String jobId;
	private String jobTitle;
	private String successMessage;

}
