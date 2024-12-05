package com.dataquadinc.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataquadinc.dto.AssignRecruiterResponse;
import com.dataquadinc.dto.RecruiterRequirementsDto;
import com.dataquadinc.dto.RequirementAddedResponse;
import com.dataquadinc.dto.RequirementsDto;
import com.dataquadinc.dto.StatusDto;
import com.dataquadinc.exceptions.ErrorResponse;
import com.dataquadinc.exceptions.NoJobsAssignedToRecruiterException;
import com.dataquadinc.exceptions.RequirementAlreadyExistsException;
import com.dataquadinc.exceptions.RequirementNotFoundException;
import com.dataquadinc.model.RequirementsModel;
import com.dataquadinc.repository.RequirementsDao;

@Service
public class RequirementsService {

	@Autowired
	private RequirementsDao requirementsDao;

	@Autowired
	private ModelMapper modelMapper;

	@Transactional
	public RequirementAddedResponse createRequirement(RequirementsDto requirementsDto) {

		RequirementsModel model = requirementsDao.findById(requirementsDto.getJobId()).orElse(null);

		if (model == null) {
			RequirementsModel requirement = modelMapper.map(requirementsDto, RequirementsModel.class);
			requirement.setStatus("In Progress");
			requirement.setRemark("Assigned To Recruiters");
			requirement.setRequirementAddedTimeStamp(LocalDateTime.now());
			requirementsDao.save(requirement);
			return new RequirementAddedResponse(requirementsDto.getJobId(), requirementsDto.getJobTitle(),
					" Requirement Added Successfully ");
		} else {
			throw new RequirementAlreadyExistsException(
					"Requirements Already Exists with Job Id : " + requirementsDto.getJobId());
		}
	}

	public Object getRequirementsDetails() {

		List<RequirementsModel> list = requirementsDao.findAll();
		if (list.isEmpty()) {
			return new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Requirements Not Found", LocalDateTime.now());
		} else {
			return list.stream().map(requirement -> modelMapper.map(requirement, RequirementsDto.class))
					.collect(Collectors.toList());
		}
	}

	public RequirementsDto getRequirementDetailsById(String jobId) {
		RequirementsModel requirement = requirementsDao.findById(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + jobId));
		return modelMapper.map(requirement, RequirementsDto.class);
	}

	@Transactional
	public Object assignToRecruiter(String jobId, String recruiterId) {
		RequirementsModel requirement = requirementsDao.findById(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + jobId));
		if (requirement.getRecruiterIds().contains(recruiterId)) {
			return new ErrorResponse(HttpStatus.CONFLICT.value(),
					"Requirement Already Assigned to Recruiter : " + recruiterId, LocalDateTime.now());
		} else {
			requirement.getRecruiterIds().add(recruiterId);
			requirementsDao.save(requirement);
			return new AssignRecruiterResponse(jobId, recruiterId, "Assigned Successfully");
		}

	}

	@Transactional
	public void statusUpdate(StatusDto status) {
		RequirementsModel requirement = requirementsDao.findById(status.getJobId()).orElseThrow(
				() -> new RequirementNotFoundException("Requirement Not Found with Id : " + status.getJobId()));
		requirement.setStatus(status.getStatus());
		requirement.setRemark(status.getRemark());
		requirementsDao.save(requirement);

	}

	public List<RecruiterRequirementsDto> getJobsAssignedToRecruiter(String recruiterId) {
		List<RequirementsModel> jobsByRecruiterId = requirementsDao.findJobsByRecruiterId(recruiterId);
		if (jobsByRecruiterId.isEmpty()) {
			throw new NoJobsAssignedToRecruiterException("No Jobs Assigned To Recruiter : " + recruiterId);
		} else {
			return jobsByRecruiterId.stream()
					.map(recruiter -> modelMapper.map(recruiter, RecruiterRequirementsDto.class))
					.collect(Collectors.toList());
		}
	}
}
