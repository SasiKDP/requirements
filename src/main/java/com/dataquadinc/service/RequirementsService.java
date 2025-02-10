package com.dataquadinc.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.dataquadinc.dto.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataquadinc.exceptions.ErrorResponse;
import com.dataquadinc.exceptions.NoJobsAssignedToRecruiterException;
import com.dataquadinc.exceptions.RequirementAlreadyExistsException;
import com.dataquadinc.exceptions.RequirementNotFoundException;
import com.dataquadinc.model.RequirementsModel_prod;
import com.dataquadinc.repository.RequirementsDao;

@Service
public class RequirementsService {

	@Autowired
	private RequirementsDao requirementsDao;

	@Autowired
	private ModelMapper modelMapper;

	@Transactional
	public RequirementAddedResponse createRequirement(RequirementsDto requirementsDto) {
		// Do not check for jobId existence manually as @PrePersist will handle it
		RequirementsModel_prod model = modelMapper.map(requirementsDto, RequirementsModel_prod.class);

		// If jobId is not set, let @PrePersist handle the generation
		if (model.getJobId() == null || model.getJobId().isEmpty()) {
			model.setStatus("In Progress");
			model.setRequirementAddedTimeStamp(LocalDateTime.now());
			requirementsDao.save(model);
		} else {
			// Throw exception if the jobId already exists
			throw new RequirementAlreadyExistsException(
					"Requirements Already Exists with Job Id : " + model.getJobId());
		}

		// Return the response using the generated jobId
		return new RequirementAddedResponse(model.getJobId(), requirementsDto.getJobTitle(), "Requirement Added Successfully");
	}

	public Object getRequirementsDetails() {
		List<RequirementsModel_prod> list = requirementsDao.findAll();
		if (list.isEmpty()) {
			return new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Requirements Not Found", LocalDateTime.now());
		} else {
			return list.stream()
					.map(requirement -> modelMapper.map(requirement, RequirementsDto.class))
					.collect(Collectors.toList());
		}
	}

	public RequirementsDto getRequirementDetailsById(String jobId) {
		RequirementsModel_prod requirement = requirementsDao.findById(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + jobId));
		return modelMapper.map(requirement, RequirementsDto.class);
	}

	@Transactional
	public Object assignToRecruiter(String jobId, String recruiterId) {
		RequirementsModel_prod requirement = requirementsDao.findById(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + jobId));

		if (requirement.getRecruiterIds().contains(recruiterId)) {
			return new ErrorResponse(HttpStatus.CONFLICT.value(),
					"Requirement Already Assigned to Recruiter : " + recruiterId, LocalDateTime.now());
		} else {
			requirement.getRecruiterIds().add(recruiterId);
			requirementsDao.save(requirement);
			return new AssignRecruiterResponse(jobId, recruiterId);
		}
	}

	@Transactional
	public void statusUpdate(StatusDto status) {
		RequirementsModel_prod requirement = requirementsDao.findById(status.getJobId()).orElseThrow(
				() -> new RequirementNotFoundException("Requirement Not Found with Id : " + status.getJobId()));
		requirement.setStatus(status.getStatus());
//        requirement.setRemark(status.getRemark());  // If you are using remark, set it here
		requirementsDao.save(requirement);
	}

	public List<RecruiterRequirementsDto> getJobsAssignedToRecruiter(String recruiterId) {
		List<RequirementsModel_prod> jobsByRecruiterId = requirementsDao.findJobsByRecruiterId(recruiterId);
		if (jobsByRecruiterId.isEmpty()) {
			throw new NoJobsAssignedToRecruiterException("No Jobs Assigned To Recruiter : " + recruiterId);
		} else {
			return jobsByRecruiterId.stream()
					.map(recruiter -> modelMapper.map(recruiter, RecruiterRequirementsDto.class))
					.collect(Collectors.toList());
		}
	}
	@Transactional
	public ResponseBean updateRequirementDetails(RequirementsDto requirementsDto) {
		// Fetch the existing requirement by jobId
		RequirementsModel_prod existingRequirement = requirementsDao.findById(requirementsDto.getJobId())
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + requirementsDto.getJobId()));

		// Update the details (excluding jobId)
		existingRequirement.setJobTitle(requirementsDto.getJobTitle());
		existingRequirement.setClientName(requirementsDto.getClientName());
		existingRequirement.setJobDescription(requirementsDto.getJobDescription());
		existingRequirement.setJobType(requirementsDto.getJobType());
		existingRequirement.setLocation(requirementsDto.getLocation());
		existingRequirement.setJobMode(requirementsDto.getJobMode());
		existingRequirement.setExperienceRequired(requirementsDto.getExperienceRequired());
		existingRequirement.setNoticePeriod(requirementsDto.getNoticePeriod());
		existingRequirement.setRelevantExperience(requirementsDto.getRelevantExperience());
		existingRequirement.setQualification(requirementsDto.getQualification());
		existingRequirement.setRecruiterIds(requirementsDto.getRecruiterIds());
		existingRequirement.setStatus(requirementsDto.getStatus());
		existingRequirement.setRecruiterName(requirementsDto.getRecruiterName());
		existingRequirement.setNoOfPositions(requirementsDto.getNoOfPositions());
		existingRequirement.setSalaryPackage(requirementsDto.getSalaryPackage());

		// Save the updated requirement to the database
		requirementsDao.save(existingRequirement);

		// Prepare the response
		DataResponse dataResponse = new DataResponse(existingRequirement.getJobId());
		return new ResponseBean(true, "Updated Successfully", null, dataResponse);
	}

	@Transactional
	public ResponseBean deleteRequirementDetails(String jobId) {
		// Fetch the existing requirement by jobId
		RequirementsModel_prod existingRequirement = requirementsDao.findById(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + jobId));

		// Delete the requirement from the database
		requirementsDao.delete(existingRequirement);

		// Return a response indicating successful deletion
		return new ResponseBean(true, "Deleted Successfully", null, new DataResponse(jobId));
	}
}
