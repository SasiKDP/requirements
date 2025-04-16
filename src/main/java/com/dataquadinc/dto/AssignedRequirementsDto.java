package com.dataquadinc.dto;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssignedRequirementsDto {

    private String jobId;  // No need to add @NotNull here if it's auto-generated

    @Size(min = 3, max = 100, message = "Job Title must be between 3 and 100 characters")
    private String jobTitle;

    @NotNull(message = "Client Name cannot be null")
    @Size(min = 3, max = 100, message = "Client Name must be between 3 and 100 characters")
    private String clientName;

    @NotNull(message = "Job Description cannot be null")
    private String jobDescription;

    @NotNull(message = "Job Type cannot be null")
    private String jobType;

    @NotNull(message = "Location cannot be null")
    private String location;

    @NotNull(message = "Job Mode cannot be null")
    private String jobMode;

    @NotNull(message = "Experience Required cannot be null")
    private String experienceRequired;

    @NotNull(message = "Notice Period cannot be null")
    private String noticePeriod;

    @NotNull(message = "Relevant Experience cannot be null")
    private String relevantExperience;

    @NotNull(message = "Qualification cannot be null")
    private String qualification;

    private String salaryPackage;

    private int noOfPositions;

    private LocalDateTime requirementAddedTimeStamp;

    private Set<String> recruiterIds;

    private String status;

    private Set<String> recruiterName;

    // Removed jobDescriptionFile and jobDescriptionBlob

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJobMode() {
        return jobMode;
    }

    public void setJobMode(String jobMode) {
        this.jobMode = jobMode;
    }

    public String getExperienceRequired() {
        return experienceRequired;
    }

    public void setExperienceRequired(String experienceRequired) {
        this.experienceRequired = experienceRequired;
    }

    public String getNoticePeriod() {
        return noticePeriod;
    }

    public void setNoticePeriod(String noticePeriod) {
        this.noticePeriod = noticePeriod;
    }

    public String getRelevantExperience() {
        return relevantExperience;
    }

    public void setRelevantExperience(String relevantExperience) {
        this.relevantExperience = relevantExperience;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getSalaryPackage() {
        return salaryPackage;
    }

    public void setSalaryPackage(String salaryPackage) {
        this.salaryPackage = salaryPackage;
    }

    public int getNoOfPositions() {
        return noOfPositions;
    }

    public void setNoOfPositions(int noOfPositions) {
        this.noOfPositions = noOfPositions;
    }

    public LocalDateTime getRequirementAddedTimeStamp() {
        return requirementAddedTimeStamp;
    }

    public void setRequirementAddedTimeStamp(LocalDateTime requirementAddedTimeStamp) {
        this.requirementAddedTimeStamp = requirementAddedTimeStamp;
    }

    public Set<String> getRecruiterIds() {
        return recruiterIds;
    }

    public void setRecruiterIds(Set<String> recruiterIds) {
        this.recruiterIds = recruiterIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<String> getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(Set<String> recruiterName) {
        this.recruiterName = recruiterName;
    }
}