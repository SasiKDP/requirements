package com.dataquadinc.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.dataquadinc.dto.*;
import com.dataquadinc.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Tuple;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataquadinc.model.RequirementsModel;
import com.dataquadinc.repository.RequirementsDao;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RequirementsService {
	private static final Logger logger = LoggerFactory.getLogger(RequirementsService.class);


	@Autowired
	private RequirementsDao requirementsDao;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private EmailService emailService;


	@Transactional
	public RequirementAddedResponse createRequirement(RequirementsDto requirementsDto) throws IOException {
		// Map DTO to model
		RequirementsModel model = modelMapper.map(requirementsDto, RequirementsModel.class);

		// Handle recruiterIds properly
		if (requirementsDto.getRecruiterIds() != null && !requirementsDto.getRecruiterIds().isEmpty()) {
			model.setRecruiterIds(requirementsDto.getRecruiterIds());
		}
		// Convert recruiterIds array (from frontend) to JSON string
		if (requirementsDto.getRecruiterIds() != null && !requirementsDto.getRecruiterIds().isEmpty()) {
			// Convert recruiterIds (Set<String>) to JSON String
			ObjectMapper objectMapper = new ObjectMapper();
			String recruiterIdsJson = objectMapper.writeValueAsString(requirementsDto.getRecruiterIds());
			model.setRecruiterIds(Collections.singleton(recruiterIdsJson));  // Set the JSON string in the model
		}

		// Clean and process recruiter IDs
		if (requirementsDto.getRecruiterIds() != null && !requirementsDto.getRecruiterIds().isEmpty()) {
			Set<String> cleanedRecruiterIds = requirementsDto.getRecruiterIds().stream()
					.map(this::cleanRecruiterId)
					.collect(Collectors.toSet());
			model.setRecruiterIds(cleanedRecruiterIds);
		}

		// Validate that only one of the fields (jobDescription or jobDescriptionFile) is provided
		if ((requirementsDto.getJobDescription() != null && !requirementsDto.getJobDescription().isEmpty()) &&
				(requirementsDto.getJobDescriptionFile() != null && !requirementsDto.getJobDescriptionFile().isEmpty())) {
			// Both fields are provided, throw an exception or return an error response
			throw new IllegalArgumentException("You can either provide a job description text or upload a job description file, but not both.");
		}

		// Handle job description: either text or file
		String jobDescription = ""; // Initialize to hold text-based description

		// If the job description is provided as text (string), save it and set the BLOB to null
		if (requirementsDto.getJobDescription() != null && !requirementsDto.getJobDescription().isEmpty()) {
			jobDescription = requirementsDto.getJobDescription();
			model.setJobDescription(jobDescription);  // Set the text-based description
			model.setJobDescriptionBlob(null);  // Ensure the file-based description is null
		}

		// If a file for the job description is uploaded, process the file using processJobDescriptionFile
		if (requirementsDto.getJobDescriptionFile() != null && !requirementsDto.getJobDescriptionFile().isEmpty()) {
			// Use processJobDescriptionFile to extract text or convert image to Base64
			String processedJobDescription = processJobDescriptionFile(requirementsDto.getJobDescriptionFile());

			// If it's text-based, set it as the job description
			model.setJobDescription(processedJobDescription);

			// If it's a Base64 image, store it as a BLOB (in case you need to handle images differently)
			if (processedJobDescription.startsWith("data:image")) { // This means it is an image in Base64 format
				byte[] jobDescriptionBytes = Base64.getDecoder().decode(processedJobDescription.split(",")[1]);
				model.setJobDescriptionBlob(jobDescriptionBytes);
			} else {
				model.setJobDescriptionBlob(null);  // Ensure BLOB is null if text is extracted
			}
		}


		// If jobId is not set, let @PrePersist handle the generation
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


		// Send email to each recruiter assigned to the requirement
		sendEmailsToRecruiters(model);


		// Return the response using the generated jobId
		return new RequirementAddedResponse(model.getJobId(), requirementsDto.getJobTitle(), "Requirement Added Successfully");
	}

	// Helper method to clean recruiter ID
	private String cleanRecruiterId(String recruiterId) {
		// Remove all quotes, brackets, and whitespace
		return recruiterId.replaceAll("[\"\\[\\]\\s]", "");
	}

	public String getRecruiterEmail(String recruiterId) {
		Tuple userTuple = requirementsDao.findUserEmailAndUsernameByUserId(recruiterId);
		return userTuple != null ? userTuple.get(0, String.class) : null;
	}


	private byte[] saveJobDescriptionFileAsBlob(MultipartFile jobDescriptionFile, String jobId) throws IOException {
		// Check if the file is empty
		if (jobDescriptionFile.isEmpty()) {
			throw new IOException("File is empty.");
		}

		// Get the byte array of the uploaded file
		byte[] jobDescriptionBytes = jobDescriptionFile.getBytes();

		// Find the job requirement by jobId
		RequirementsModel requirement = requirementsDao.findById(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement not found with Job Id: " + jobId));

		// Save the byte array (job description) into the database (BLOB column)
		requirement.setJobDescriptionBlob(jobDescriptionBytes);

		// Save the updated requirement back to the database
		requirementsDao.save(requirement);

		return jobDescriptionBytes;  // Return the byte array if needed
	}


	// Update the sendEmailsToRecruiters method to handle comma-separated string
	public void sendEmailsToRecruiters(RequirementsModel model) {
		logger.info("Starting email sending process for job ID: {}", model.getJobId());

		try {
			Set<String> recruiterIds = model.getRecruiterIds();
			if (recruiterIds == null || recruiterIds.isEmpty()) {
				logger.warn("No recruiter IDs found for job ID: {}", model.getJobId());
				return;
			}


			logger.info("Found {} recruiters to send emails to for job ID: {}", recruiterIds.size(), model.getJobId());

			String status = (model.getStatus() == null || model.getStatus().trim().isEmpty())
					? "in progress"
					: model.getStatus().toLowerCase();

			// Send emails to recruiters
			for (String recruiterId : recruiterIds) {
				try {
					String cleanedRecruiterId = cleanRecruiterId(recruiterId);
					logger.debug("Processing recruiter with ID: {} (cleaned ID: {})", recruiterId, cleanedRecruiterId);

					Tuple userTuple = requirementsDao.findUserEmailAndUsernameByUserId(cleanedRecruiterId);

					if (userTuple != null) {
						String recruiterEmail = userTuple.get(0, String.class);
						String recruiterName = userTuple.get(1, String.class);

						if (recruiterEmail != null && !recruiterEmail.isEmpty()) {
							String subject;
							String body;

							switch (status) {
								case "closed":
									subject = "Job Closed Notification - Job ID: " + model.getJobId();
									body = constructClosedEmailBody(model, recruiterName);
									break;
								case "hold":
									subject = "Job On Hold Notification - Job ID: " + model.getJobId();
									body = constructHoldEmailBody(model, recruiterName);
									break;
								default:
									subject = "New Job Assignment - Job ID: " + model.getJobId();
									body = constructEmailBody(model, recruiterName);
									break;
							}

							emailService.sendEmail(recruiterEmail, subject, body);
							logger.info("Email successfully sent to recruiter: {} <{}> for job ID: {}",
									recruiterName, recruiterEmail, model.getJobId());
						} else {
							logger.error("Email not found for recruiter ID: {}", cleanedRecruiterId);
						}
					} else {
						logger.error("No user data found for recruiter ID: {}", cleanedRecruiterId);
					}
				} catch (Exception e) {
					logger.error("Error processing recruiter {} for job ID: {}. Error: {}", recruiterId, model.getJobId(), e.getMessage(), e);
				}
			}

			// Send email to Team Lead (assignedBy)
			try {
				String assignedByUsername = model.getAssignedBy();

				if (assignedByUsername != null && !assignedByUsername.trim().isEmpty()) {
					String cleanedUsername = assignedByUsername.trim().replaceAll("^\"|\"$", "").toLowerCase();
					logger.info("Looking up Team Lead using username → Raw: '{}', Cleaned: '{}'", assignedByUsername, cleanedUsername);

					Tuple leadTuple = requirementsDao.findUserEmailAndUsernameByAssignedBy(cleanedUsername);

					if (leadTuple != null) {
						String leadEmail = leadTuple.get(0, String.class);
						String leadName = leadTuple.get(1, String.class);

						if (leadEmail != null && !leadEmail.isEmpty()) {
							String subject;
							String body;

							switch (status) {
								case "closed":
									subject = "Team Lead Notification: Job Closed - Job ID: " + model.getJobId();
									body = constructClosedEmailBodyForLead(model, leadName);
									break;
								case "hold":
									subject = "Team Lead Notification: Job On Hold - Job ID: " + model.getJobId();
									body = constructHoldEmailBodyForLead(model, leadName);
									break;
								default:
									subject = "Team Lead Notification: New Job Assigned - Job ID: " + model.getJobId();
									body = constructLeadAssignmentBody(model, leadName);
									break;
							}

							emailService.sendEmail(leadEmail, subject, body);
							logger.info("✅ Email sent to Team Lead: {} <{}> for job ID: {}", leadName, leadEmail, model.getJobId());
						} else {
							logger.warn("⚠️ Lead email is empty for assignedBy username: '{}'", assignedByUsername);
						}
					} else {
						logger.warn("❌ No user found as Team Lead with username: '{}'", assignedByUsername);
					}
				}
			} catch (Exception e) {
				logger.error("🚨 Error sending email to Team Lead for job ID: {}. Error: {}", model.getJobId(), e.getMessage(), e);
			}

			logger.info("Completed email sending process for job ID: {}", model.getJobId());
		} catch (Exception e) {
			logger.error("Critical error in sending emails to recruiters/team lead for job ID: {}. Error: {}", model.getJobId(), e.getMessage(), e);
			throw new RuntimeException("Error in sending emails: " + e.getMessage(), e);
		}
	}

	private String constructLeadAssignmentBody(RequirementsModel model, String leadName) {
		return "Dear " + leadName + ",\n\n" +
				"You have successfully assigned a job requirement to your team. Here are the details:\n\n" +
				"▶ Job ID: " + model.getJobId() + "\n" +
				"▶ Job Title: " + model.getJobTitle() + "\n" +
				"▶ Client: " + model.getClientName() + "\n" +
				"▶ Location: " + model.getLocation() + "\n" +
				"▶ Job Type: " + model.getJobType() + "\n" +
				"▶ Experience Required: " + model.getExperienceRequired() + " years\n\n" +
				"Recruiters have been notified.\n\n" +
				"Regards,\nDataquad";
	}

	private String constructClosedEmailBodyForLead(RequirementsModel model, String leadName) {
		return "Dear " + leadName + ",\n\n" +
				"The job requirement you assigned has been marked as *Closed*:\n\n" +
				"▶ Job ID: " + model.getJobId() + "\n" +
				"▶ Job Title: " + model.getJobTitle() + "\n" +
				"▶ Client: " + model.getClientName() + "\n\n" +
				"Regards,\nDataquad";
	}

	private String constructHoldEmailBodyForLead(RequirementsModel model, String leadName) {
		return "Dear " + leadName + ",\n\n" +
				"The job requirement you assigned has been put on *Hold*:\n\n" +
				"▶ Job ID: " + model.getJobId() + "\n" +
				"▶ Job Title: " + model.getJobTitle() + "\n" +
				"▶ Client: " + model.getClientName() + "\n\n" +
				"We’ll inform you once it’s resumed.\n\n" +
				"Regards,\nDataquad";
	}


	// Update constructEmailBody method to use recruiterName instead of fetching separately
	private String constructEmailBody(RequirementsModel model, String recruiterName) {
		return "Dear " + recruiterName + ",\n\n" +
				"Dataquad";
	}


	private String constructClosedEmailBody(RequirementsModel model, String recruiterName) {
		return "Dear " + recruiterName + ",\n\n" +
				"This is to inform you that the following job requirement has been marked as *Closed*:\n\n" +
				"▶ Job Title: " + model.getJobTitle() + "\n" +
				"▶ Client: " + model.getClientName() + "\n" +
				"▶ Location: " + model.getLocation() + "\n" +
				"▶ Job Type: " + model.getJobType() + "\n\n" +
				"No further action is required from your end.\n\n" +
				"Regards,\nDataquad";
	}


	private String constructHoldEmailBody(RequirementsModel model, String recruiterName) {
		return "Dear " + recruiterName + ",\n\n" +
				"Please note that the following job requirement has been put on *Hold*:\n\n" +
				"▶ Job Title: " + model.getJobTitle() + "\n" +
				"▶ Client: " + model.getClientName() + "\n" +
				"▶ Location: " + model.getLocation() + "\n" +
				"▶ Job Type: " + model.getJobType() + "\n\n" +
				"You will be notified when the status changes.\n\n" +
				"Thanks for your patience.\n\n" +
				"Best,\nDataquad";
	}


	public String processJobDescriptionFile(MultipartFile jobDescriptionFile) throws IOException {
		// Check if the file is empty
		if (jobDescriptionFile.isEmpty()) {
			throw new IOException("File is empty.");
		}

		// Get file extension to determine file type
		String fileName = jobDescriptionFile.getOriginalFilename();
		String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

		switch (fileExtension) {
			case "pdf":
				return extractTextFromPdf(jobDescriptionFile);  // Return extracted text
			case "docx":
				return extractTextFromDocx(jobDescriptionFile);  // Return extracted text
			case "txt":
				return extractTextFromTxt(jobDescriptionFile);  // Return extracted text
			case "jpg":
			case "jpeg":
			case "png":
			case "gif":
				return convertImageToBase64(jobDescriptionFile);  // Convert image to base64 (instead of saving locally)
			default:
				throw new IOException("Unsupported file type.");
		}
	}

	// Method to extract text from PDF file
	private String extractTextFromPdf(MultipartFile file) throws IOException {
		PDDocument document = PDDocument.load(file.getInputStream());
		PDFTextStripper stripper = new PDFTextStripper();
		String text = stripper.getText(document);
		document.close();
		return text;  // Return extracted text from PDF
	}

	// Method to extract text from DOCX file
	private String extractTextFromDocx(MultipartFile file) throws IOException {
		XWPFDocument docx = new XWPFDocument(file.getInputStream());
		StringBuilder text = new StringBuilder();
		docx.getParagraphs().forEach(paragraph -> text.append(paragraph.getText()).append("\n"));
		return text.toString();  // Return extracted text from DOCX
	}

	// Method to extract text from TXT file
	private String extractTextFromTxt(MultipartFile file) throws IOException {
		InputStream inputStream = file.getInputStream();
		String text = new String(inputStream.readAllBytes());
		return text;  // Return the text content of the TXT file
	}

	// Method to convert image to base64 (instead of saving it locally)
	private String convertImageToBase64(MultipartFile file) throws IOException {
		byte[] imageBytes = file.getBytes();
		return Base64.getEncoder().encodeToString(imageBytes);  // Return base64 encoded image
	}


	public List<RequirementsDto> getRequirementsDetails() {
		// 1. Get the first and last date of the current month
		LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
		LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

		// 2. Fetch data from repository
		List<RequirementsModel> requirementsList =
				requirementsDao.findByRequirementAddedTimeStampBetween(startOfMonth, endOfMonth);


		// 4. Convert to DTOs
		return requirementsList.stream()
				.map(requirement -> {
					RequirementsDto dto = new RequirementsDto();

					dto.setJobId(requirement.getJobId());
					dto.setJobTitle(requirement.getJobTitle());
					dto.setClientName(requirement.getClientName());
					dto.setJobDescription(requirement.getJobDescription());
					dto.setJobDescriptionBlob(requirement.getJobDescriptionBlob());
					dto.setJobType(requirement.getJobType());
					dto.setLocation(requirement.getLocation());
					dto.setJobMode(requirement.getJobMode());
					dto.setExperienceRequired(requirement.getExperienceRequired());
					dto.setNoticePeriod(requirement.getNoticePeriod());
					dto.setRelevantExperience(requirement.getRelevantExperience());
					dto.setQualification(requirement.getQualification());
					dto.setSalaryPackage(requirement.getSalaryPackage());
					dto.setNoOfPositions(requirement.getNoOfPositions());
					dto.setRequirementAddedTimeStamp(requirement.getRequirementAddedTimeStamp());
					dto.setRecruiterIds(requirement.getRecruiterIds());
					dto.setStatus(requirement.getStatus());
					dto.setRecruiterName(requirement.getRecruiterName());
					dto.setAssignedBy(requirement.getAssignedBy());

					// Submissions and Interviews
					String jobId = requirement.getJobId();
					dto.setNumberOfSubmissions(requirementsDao.getNumberOfSubmissionsByJobId(jobId));
					dto.setNumberOfInterviews(requirementsDao.getNumberOfInterviewsByJobId(jobId));

					return dto;
				})
				.collect(Collectors.toList());
	}


	public List<RequirementsDto> getRequirementsByDateRange(LocalDate startDate, LocalDate endDate) {

		// 💥 Second check: End date must not be before start date
		if (endDate.isBefore(startDate)) {
			throw new DateRangeValidationException("End date cannot be before start date.");
		}


		List<RequirementsModel> requirements = requirementsDao.findByRequirementAddedTimeStampBetween(startDate, endDate);



		// ✅ Add logger here since this block is guaranteed to have results
		Logger logger = LoggerFactory.getLogger(RequirementsService.class);
		logger.info("✅ Fetched {} requirements between {} and {}", requirements.size(), startDate, endDate);

		return requirements.stream().map(requirement -> {
			RequirementsDto dto = new RequirementsDto();
			dto.setJobId(requirement.getJobId());
			dto.setJobTitle(requirement.getJobTitle());
			dto.setClientName(requirement.getClientName());
			dto.setJobDescription(requirement.getJobDescription());
			dto.setJobDescriptionBlob(requirement.getJobDescriptionBlob());
			dto.setJobType(requirement.getJobType());
			dto.setLocation(requirement.getLocation());
			dto.setJobMode(requirement.getJobMode());
			dto.setExperienceRequired(requirement.getExperienceRequired());
			dto.setNoticePeriod(requirement.getNoticePeriod());
			dto.setRelevantExperience(requirement.getRelevantExperience());
			dto.setQualification(requirement.getQualification());
			dto.setSalaryPackage(requirement.getSalaryPackage());
			dto.setNoOfPositions(requirement.getNoOfPositions());
			dto.setRequirementAddedTimeStamp(requirement.getRequirementAddedTimeStamp());
			dto.setRecruiterIds(requirement.getRecruiterIds());
			dto.setStatus(requirement.getStatus());
			dto.setRecruiterName(requirement.getRecruiterName());
			dto.setAssignedBy(requirement.getAssignedBy());

			// Stats
			String jobId = requirement.getJobId();
			dto.setNumberOfSubmissions(requirementsDao.getNumberOfSubmissionsByJobId(jobId));
			dto.setNumberOfInterviews(requirementsDao.getNumberOfInterviewsByJobId(jobId));

			return dto;
		}).collect(Collectors.toList());
	}



	public RequirementsDto getRequirementDetailsById(String jobId) {
		RequirementsModel requirement = requirementsDao.findById(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + jobId));
		return modelMapper.map(requirement, RequirementsDto.class);
	}

	@Transactional
	public Object assignToRecruiter(String jobId, Set<String> recruiterIds) {
		// Fetch the existing requirement by jobId
		RequirementsModel requirement = requirementsDao.findById(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + jobId));

		// Get current recruiter IDs
		Set<String> cleanedRecruiterIds = recruiterIds.stream()
				.map(this::cleanRecruiterId)
				.collect(Collectors.toSet());

		// Check if the recruiter is already assigned to the job
		if (cleanedRecruiterIds.contains(recruiterIds)) {
			return new ErrorResponse(HttpStatus.CONFLICT.value(),
					"Requirement Already Assigned to Recruiter : " + cleanedRecruiterIds, LocalDateTime.now());
		}

		// Add new recruiter ID
		requirement.setRecruiterIds(cleanedRecruiterIds);

		// Save the recruiter relationship in the database
		RequirementsModel jobRecruiterEmail = new RequirementsModel();
		jobRecruiterEmail.setJobId(cleanedRecruiterIds.toString());
		jobRecruiterEmail.setRecruiterIds(cleanedRecruiterIds);

		// Save the updated requirement in the DB
		requirementsDao.save(requirement);

		// Send email to the recruiter using the existing method
		sendEmailsToRecruiters(requirement);

		return new AssignRecruiterResponse(jobId, String.join(",", cleanedRecruiterIds));
	}


	@Transactional
	public void statusUpdate(StatusDto status) {
		RequirementsModel requirement = requirementsDao.findById(status.getJobId()).orElseThrow(
				() -> new RequirementNotFoundException("Requirement Not Found with Id : " + status.getJobId()));
		requirement.setStatus(status.getStatus());
//        requirement.setRemark(status.getRemark());  // If you are using remark, set it here
		requirementsDao.save(requirement);
	}

	public List<RecruiterRequirementsDto> getJobsAssignedToRecruiter(String recruiterId) {
		// 📅 Calculate current month start and end
		LocalDate today = LocalDate.now();
		LocalDateTime startDateTime = today.withDayOfMonth(1).atStartOfDay();
		LocalDateTime endDateTime = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);

		// 🔍 Fetch jobs for recruiter within current month
		List<RequirementsModel> jobsByRecruiterId = requirementsDao.findJobsByRecruiterIdAndDateRange(
				recruiterId, startDateTime, endDateTime);



		// 🔁 Map to DTOs
		return jobsByRecruiterId.stream()
				.map(job -> {
					RecruiterRequirementsDto dto = modelMapper.map(job, RecruiterRequirementsDto.class);
					dto.setAssignedBy(job.getAssignedBy());
					return dto;
				})
				.collect(Collectors.toList());
	}


	public List<RecruiterRequirementsDto> getJobsAssignedToRecruiterByDate(String recruiterId, LocalDate startDate, LocalDate endDate) {

		// 💥 First check: Null check for input dates
		if (startDate == null || endDate == null) {
			throw new DateRangeValidationException("Start date and End date must not be null.");
		}

		// 💥 Second check: End date must not be before start date
		if (endDate.isBefore(startDate)) {
			throw new DateRangeValidationException("End date cannot be before start date.");
		}

		// 💥 Convert LocalDate to LocalDateTime for comparison
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

		// 🔍 Fetch jobs assigned to recruiter within the date range
		List<RequirementsModel> jobs = requirementsDao.findJobsByRecruiterIdAndDateRange(recruiterId, startDateTime, endDateTime);



		// ✅ Add logger here since this block is guaranteed to have results
		Logger logger = LoggerFactory.getLogger(RequirementsService.class);
		logger.info("✅ Fetched {} jobs assigned to recruiter {} between {} and {}", jobs.size(), recruiterId, startDate, endDate);

		// 🔄 Map entities to DTOs
		return jobs.stream()
				.map(job -> {
					RecruiterRequirementsDto dto = modelMapper.map(job, RecruiterRequirementsDto.class);
					dto.setAssignedBy(job.getAssignedBy()); // Ensure assignedBy is mapped
					return dto;
				})
				.collect(Collectors.toList());
	}



	@Transactional
	public ResponseBean updateRequirementDetails(RequirementsDto requirementsDto) {
		try {
			// Fetch the existing requirement by jobId
			RequirementsModel existingRequirement = requirementsDao.findById(requirementsDto.getJobId())
					.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + requirementsDto.getJobId()));

			// Log before update
			logger.info("Before update: " + existingRequirement);

			// Update the existing requirement with the new details from the DTO
			if (requirementsDto.getJobTitle() != null) existingRequirement.setJobTitle(requirementsDto.getJobTitle());
			if (requirementsDto.getClientName() != null)
				existingRequirement.setClientName(requirementsDto.getClientName());

			// Handle job description: either text or file
			if (requirementsDto.getJobDescription() != null && !requirementsDto.getJobDescription().isEmpty()) {
				existingRequirement.setJobDescription(requirementsDto.getJobDescription());  // Set text-based description
				existingRequirement.setJobDescriptionBlob(null);  // Nullify the BLOB if text is provided
			}

			// If a file for job description is provided, set it as BLOB and nullify the text description
			if (requirementsDto.getJobDescriptionFile() != null && !requirementsDto.getJobDescriptionFile().isEmpty()) {
				byte[] jobDescriptionBytes = saveJobDescriptionFileAsBlob(requirementsDto.getJobDescriptionFile(), requirementsDto.getJobId());
				existingRequirement.setJobDescriptionBlob(jobDescriptionBytes);  // Set the BLOB field
				existingRequirement.setJobDescription(null);  // Nullify the text-based description
			}

			// If the jobDescriptionFile is null, but jobDescriptionBlob is updated, update the BLOB
			if (requirementsDto.getJobDescriptionFile() == null && requirementsDto.getJobDescriptionBlob() != null) {
				existingRequirement.setJobDescriptionBlob(requirementsDto.getJobDescriptionBlob());  // Set the BLOB field
				existingRequirement.setJobDescription(null);  // Nullify the text-based description
			}

			// Set other fields
			existingRequirement.setJobType(requirementsDto.getJobType());
			existingRequirement.setLocation(requirementsDto.getLocation());
			existingRequirement.setJobMode(requirementsDto.getJobMode());
			existingRequirement.setExperienceRequired(requirementsDto.getExperienceRequired());
			existingRequirement.setNoticePeriod(requirementsDto.getNoticePeriod());
			existingRequirement.setRelevantExperience(requirementsDto.getRelevantExperience());
			existingRequirement.setQualification(requirementsDto.getQualification());
			existingRequirement.setSalaryPackage(requirementsDto.getSalaryPackage());
			existingRequirement.setNoOfPositions(requirementsDto.getNoOfPositions());
			existingRequirement.setRecruiterIds(requirementsDto.getRecruiterIds());
			existingRequirement.setRecruiterName(requirementsDto.getRecruiterName());
			existingRequirement.setAssignedBy(requirementsDto.getAssignedBy());
			if (requirementsDto.getStatus() != null) existingRequirement.setStatus(requirementsDto.getStatus());


			// Save the updated requirement to the database
			requirementsDao.save(existingRequirement);

			// Log after update
			logger.info("After update: " + existingRequirement);

			// Send emails to recruiters (after the requirement has been successfully updated)
			sendEmailsToRecruiters(existingRequirement); // Assuming this method handles the sending of emails to recruiters

			// Return success response
			String successMessage = "Requirement updated successfully. Emails sent to assigned recruiters and Team Lead.";
			return new ResponseBean(true, successMessage, null, null);
		} catch (Exception e) {
			logger.error("Error updating requirement", e);
			return new ResponseBean(false, "Error updating requirement" + e.getMessage(), "Internal Server Error", null);
		}
	}


	@Transactional
	public ResponseBean deleteRequirementDetails(String jobId) {
		// Fetch the existing requirement by jobId
		RequirementsModel existingRequirement = requirementsDao.findById(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + jobId));

		// Delete the requirement from the database
		requirementsDao.delete(existingRequirement);

		// Return a response indicating successful deletion
		return new ResponseBean(true, "Deleted Successfully", null, new DataResponse(jobId));
	}

	public String getRecruiterName(String recruiterId) {
		String cleanedRecruiterId = recruiterId.trim().replace("\"", "");

		try {
			logger.info("Fetching recruiter for ID: {}", cleanedRecruiterId);

			Tuple userTuple = requirementsDao.findUserEmailAndUsernameByUserId(cleanedRecruiterId);

			if (userTuple != null) {
				logger.info("Tuple found: {}", userTuple);
				return userTuple.get("user_name", String.class);
			} else {
				logger.warn("No recruiter found with ID: {}", cleanedRecruiterId);
				return null;
			}
		} catch (Exception e) {
			logger.error("Error fetching recruiter username", e);
			throw new RuntimeException("Error fetching recruiter username", e);
		}
	}

	public List<RecruiterInfoDto> getRecruitersForJob(String jobId) {
		// Get the requirement with the given job ID
		RequirementsModel requirement = requirementsDao.findById(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id: " + jobId));

		// Log raw recruiter IDs
		System.out.println("Raw Recruiter IDs: " + requirement.getRecruiterIds());

		// Extract recruiter IDs properly
		Set<String> recruiterIds = requirement.getRecruiterIds().stream()
				.map(String::trim)  // Trim any spaces
				.filter(id -> !id.isEmpty())  // Remove empty strings
				.collect(Collectors.toSet());

		// Log cleaned recruiter IDs
		System.out.println("Cleaned Recruiter IDs: " + recruiterIds);

		// Fetch recruiter details for each recruiter ID
		List<RecruiterInfoDto> recruiters = new ArrayList<>();
		for (String recruiterId : recruiterIds) {
			Tuple userTuple = requirementsDao.findUserEmailAndUsernameByUserId(recruiterId);

			// Log recruiter fetching status
			System.out.println("Recruiter ID: " + recruiterId + ", Found in DB: " + (userTuple != null));

			if (userTuple != null) {
				String recruiterName = userTuple.get(1, String.class);  // Assuming username is at index 1
				recruiters.add(new RecruiterInfoDto(recruiterId, recruiterName));
			} else {
				System.err.println("Recruiter not found in DB for ID: " + recruiterId);
			}
		}

		return recruiters;
	}

	public RequirementDetailsDto getRequirementDetailsByJobId(String jobId) {
		// Fetch the requirement
		RequirementsModel requirement = requirementsDao.findByJobId(jobId)
				.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id: " + jobId));

		// Manually convert to DTO
		RequirementsDto requirementsDto = mapToDto(requirement);

		// Fetch submitted candidates
		List<Tuple> submittedCandidatesList = requirementsDao.findCandidatesByJobId(jobId);
		List<CandidateDto> submittedCandidates = mapCandidates(submittedCandidatesList);

		// Fetch interview scheduled candidates
		List<Tuple> interviewCandidatesList = requirementsDao.findInterviewScheduledCandidatesByJobId(jobId);
		List<InterviewCandidateDto> interviewScheduledCandidates = mapInterviewCandidates(interviewCandidatesList);

		// Fetch placements
		List<Tuple> placementsList = requirementsDao.findPlacementsByJobId(jobId);
		List<PlacementDto> placements = mapPlacements(placementsList);

		// Set submission and interview counts
		requirementsDto.setNumberOfSubmissions(submittedCandidates.size());
		requirementsDto.setNumberOfInterviews(interviewScheduledCandidates.size());

		// Build final DTO
		return new RequirementDetailsDto(requirementsDto, submittedCandidates, interviewScheduledCandidates, placements);
	}

	private RequirementsDto mapToDto(RequirementsModel requirement) {
		RequirementsDto dto = new RequirementsDto();

		dto.setJobId(requirement.getJobId());
		dto.setJobTitle(requirement.getJobTitle());
		dto.setClientName(requirement.getClientName());
		dto.setJobDescription(requirement.getJobDescription());
		dto.setJobType(requirement.getJobType());
		dto.setLocation(requirement.getLocation());
		dto.setJobMode(requirement.getJobMode());
		dto.setExperienceRequired(requirement.getExperienceRequired());
		dto.setNoticePeriod(requirement.getNoticePeriod());
		dto.setRelevantExperience(requirement.getRelevantExperience());
		dto.setQualification(requirement.getQualification());
		dto.setSalaryPackage(requirement.getSalaryPackage());
		dto.setNoOfPositions(requirement.getNoOfPositions());
		dto.setRequirementAddedTimeStamp(requirement.getRequirementAddedTimeStamp());
		dto.setRecruiterIds(requirement.getRecruiterIds());
		dto.setRecruiterName(requirement.getRecruiterName());
		dto.setStatus(requirement.getStatus());
		dto.setAssignedBy(requirement.getAssignedBy());
		dto.setJobDescriptionBlob(requirement.getJobDescriptionBlob());

		return dto;
	}


	/**
	 * Maps Tuple data to CandidateDto objects, including recruiter name.
	 */
	private List<CandidateDto> mapCandidates(List<Tuple> candidatesList) {
		return candidatesList.stream()
				.map(candidate -> {
					try {
						// Retrieve recruiter name from the Tuple
						String recruiterName = candidate.get("recruiterName", String.class);

						return new CandidateDto(
								getTupleValue(candidate, "candidate_id"),
								getTupleValue(candidate, "full_name"),
								getTupleValue(candidate, "candidate_email_id"),
								getTupleValue(candidate, "interviewStatus"),
								getTupleValue(candidate, "contact_number"),
								getTupleValue(candidate, "qualification"),
								getTupleValue(candidate, "skills"),
								getTupleValue(candidate, "overall_feedback"),
								recruiterName  // Add recruiter name
						);
					} catch (Exception e) {
						System.err.println("Error mapping candidate: " + candidate + " | Exception: " + e.getMessage());
						return null;
					}
				})
				.filter(Objects::nonNull)  // Remove any null entries
				.collect(Collectors.toList());
	}


	/**
	 * Maps Tuple data to InterviewCandidateDto objects, including recruiter name.
	 */
	private List<InterviewCandidateDto> mapInterviewCandidates(List<Tuple> candidatesList) {
		return candidatesList.stream()
				.map(candidate -> {
					try {
						// Retrieve recruiter name from the Tuple
						String recruiterName = candidate.get("recruiterName", String.class);

						// Format interview date-time if available
						Timestamp interviewTimestamp = candidate.get("interview_date_time", Timestamp.class);
						String interviewDateTime = interviewTimestamp != null
								? interviewTimestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
								: null;

						return new InterviewCandidateDto(
								getTupleValue(candidate, "candidate_id"),
								getTupleValue(candidate, "full_name"),
								getTupleValue(candidate, "candidate_email_id"),
								getTupleValue(candidate, "interviewStatus"),
								getTupleValue(candidate, "interview_level"),
								interviewDateTime,
								recruiterName  // Add recruiter name
						);
					} catch (Exception e) {
						System.err.println("Error mapping interview candidate: " + candidate + " | Exception: " + e.getMessage());
						return null;
					}
				})
				.filter(Objects::nonNull)  // Remove any null entries
				.collect(Collectors.toList());
	}

	/**
	 * Maps Tuple data to PlacementDto objects.
	 */
	private List<PlacementDto> mapPlacements(List<Tuple> placementsList) {
		return placementsList.stream()
				.map(placement -> {
					try {
						return new PlacementDto(
								getTupleValue(placement, "placement_id"),
								getTupleValue(placement, "consultant_name"),
								getTupleValue(placement, "sales"),
								getTupleValue(placement, "technology"),
								getTupleValue(placement, "recruiter"),
								getTupleValue(placement, "client"),
								getTupleValue(placement, "vendor"),
								getTupleValue(placement, "employment_type")
						);
					} catch (Exception e) {
						System.err.println("Error mapping placement: " + placement + " | Exception: " + e.getMessage());
						return null;
					}
				})
				.filter(Objects::nonNull) // Remove any null entries
				.collect(Collectors.toList());
	}


	/**
	 * Safely retrieves values from Tuple, handling null cases.
	 */
	private String getTupleValue(Tuple tuple, String columnName) {
		try {
			return tuple.get(columnName, String.class);
		} catch (Exception e) {
			System.err.println("Error fetching column: " + columnName + " | Exception: " + e.getMessage());
			return null;
		}
	}

	public CandidateStatsResponse getCandidateStats() {
		List<UserStatsDTO> userStatsList = new ArrayList<>();

		// 👤 Employee Stats
		List<Tuple> employeeStats = requirementsDao.getEmployeeCandidateStats();
		userStatsList.addAll(employeeStats.stream()
				.map(tuple -> {
					UserStatsDTO dto = new UserStatsDTO();
					dto.setEmployeeId(tuple.get("employeeId", String.class));
					dto.setEmployeeName(tuple.get("employeeName", String.class));
					dto.setEmployeeEmail(tuple.get("employeeEmail", String.class));
					dto.setRole("Employee");

					dto.setNumberOfClients(convertToInt(tuple.get("numberOfClients")));
					dto.setNumberOfRequirements(convertToInt(tuple.get("numberOfRequirements")));
					dto.setNumberOfSubmissions(convertToInt(tuple.get("numberOfSubmissions")));
					dto.setNumberOfInterviews(convertToInt(tuple.get("numberOfInterviews")));
					dto.setNumberOfPlacements(convertToInt(tuple.get("numberOfPlacements")));

					return dto;
				}).collect(Collectors.toList())
		);

		// 👨‍🏫 Teamlead Stats
		List<Tuple> teamleadStats = requirementsDao.getTeamleadCandidateStats();
		userStatsList.addAll(teamleadStats.stream()
				.map(tuple -> {
					UserStatsDTO dto = new UserStatsDTO();
					dto.setEmployeeId(tuple.get("employeeId", String.class));
					dto.setEmployeeName(tuple.get("employeeName", String.class));
					dto.setEmployeeEmail(tuple.get("employeeEmail", String.class));
					dto.setRole("Teamlead");

					dto.setNumberOfClients(convertToInt(tuple.get("numberOfClients")));
					dto.setNumberOfRequirements(convertToInt(tuple.get("numberOfRequirements")));
					dto.setSelfSubmissions(convertToInt(tuple.get("selfSubmissions")));
					dto.setSelfInterviews(convertToInt(tuple.get("selfInterviews")));
					dto.setSelfPlacements(convertToInt(tuple.get("selfPlacements")));
					dto.setTeamSubmissions(convertToInt(tuple.get("teamSubmissions")));
					dto.setTeamInterviews(convertToInt(tuple.get("teamInterviews")));
					dto.setTeamPlacements(convertToInt(tuple.get("teamPlacements")));

					return dto;
				}).collect(Collectors.toList())
		);

		return new CandidateStatsResponse(userStatsList);
	}



	private int convertToInt(Object value) {
		if (value instanceof BigInteger) {
			return ((BigInteger) value).intValue(); // MySQL COUNT() returns BigInteger
		} else if (value instanceof BigDecimal) {
			return ((BigDecimal) value).intValue(); // Handles numeric decimal cases
		} else if (value instanceof Number) {
			return ((Number) value).intValue(); // Fallback for any numeric types
		} else {
			return 0; // Default to 0 if null or unknown type
		}
	}


	// Fetch both Submitted Candidates, Scheduled Interviews, and Employee Details in one call
	public CandidateResponseDTO getCandidateData(String userId) {
		// Fetch role and username
		Tuple roleInfo = requirementsDao.getUserRoleAndUsername(userId);
		String role = roleInfo.get("role", String.class);
		String username = roleInfo.get("userName", String.class);

		List<SubmittedCandidateDTO> submittedCandidates;
		List<InterviewScheduledDTO> scheduledInterviews;
		List<JobDetailsDTO> jobDetails;
		List<PlacementDetailsDTO> placementDetails;
		List<ClientDetailsDTO> clientDetails;
		List<Tuple> employeeDetailsTuples;

		if ("Teamlead".equalsIgnoreCase(role)) {
			submittedCandidates = requirementsDao.findSubmittedCandidatesByAssignedBy(username);
			scheduledInterviews = requirementsDao.findScheduledInterviewsByAssignedBy(username);
			jobDetails = requirementsDao.findJobDetailsByAssignedBy(username);
			placementDetails = requirementsDao.findPlacementCandidatesByAssignedBy(username);
			clientDetails = requirementsDao.findClientDetailsByAssignedBy(username);
			employeeDetailsTuples = requirementsDao.getTeamleadDetailsByUserId(userId); // if different query needed
		} else {
			submittedCandidates = requirementsDao.findSubmittedCandidatesByUserId(userId);
			scheduledInterviews = requirementsDao.findScheduledInterviewsByUserId(userId);
			jobDetails = requirementsDao.findJobDetailsByUserId(userId);
			placementDetails = requirementsDao.findPlacementCandidatesByUserId(userId);
			clientDetails = requirementsDao.findClientDetailsByUserId(userId);
			employeeDetailsTuples = requirementsDao.getEmployeeDetailsByUserId(userId);
		}

		Map<String, List<SubmittedCandidateDTO>> groupedSubmissions = groupByClientName(submittedCandidates);
		Map<String, List<InterviewScheduledDTO>> groupedInterviews = groupByClientName(scheduledInterviews);
		Map<String, List<PlacementDetailsDTO>> groupedPlacements = groupByClientName(placementDetails);
		Map<String, List<JobDetailsDTO>> groupedJobDetails = groupByClientName(jobDetails);
		Map<String, List<ClientDetailsDTO>> groupedClientDetails = groupByClientName(clientDetails);
		List<EmployeeDetailsDTO> employeeDetails = mapEmployeeDetailsTuples(employeeDetailsTuples);

		return new CandidateResponseDTO(
				groupedSubmissions, groupedInterviews, groupedPlacements,
				groupedJobDetails, groupedClientDetails, employeeDetails
		);
	}


	// Generic method to group a list by normalized client name
	private <T> Map<String, List<T>> groupByClientName(List<T> list) {
		return list.stream()
				.collect(Collectors.groupingBy(item -> normalizeClientName(getClientName(item)),
						LinkedHashMap::new, Collectors.toList()));
	}

	// Generic method to retrieve client name from various DTO types
	private <T> String getClientName(T item) {
		if (item instanceof ClientDetailsDTO) {
			return ((ClientDetailsDTO) item).getClientName();
		} else if (item instanceof SubmittedCandidateDTO) {
			return ((SubmittedCandidateDTO) item).getClientName();
		} else if (item instanceof InterviewScheduledDTO) {
			return ((InterviewScheduledDTO) item).getClientName();
		} else if (item instanceof PlacementDetailsDTO) {
			return ((PlacementDetailsDTO) item).getClientName();
		} else if (item instanceof JobDetailsDTO) {
			return ((JobDetailsDTO) item).getClientName();
		} else {
			return "";
		}
	}

	// Helper method to normalize the client name (simple toLowerCase as an example)
	private String normalizeClientName(String clientName) {
		return clientName != null ? clientName.toLowerCase() : "";
	}


	// Method to map Tuple to EmployeeDetailsDTO
	// Helper method to convert List<Tuple> to List<EmployeeDetailsDTO>
	private List<EmployeeDetailsDTO> mapEmployeeDetailsTuples(List<Tuple> employeeDetailsTuples) {
		List<EmployeeDetailsDTO> employeeDetails = new ArrayList<>();

		for (Tuple tuple : employeeDetailsTuples) {
			String joiningDateStr = tuple.get("joiningDate", String.class);
			String dobStr = tuple.get("dob", String.class);

			// Parsing date strings into LocalDate if not null or empty
			LocalDate joiningDate = joiningDateStr != null && !joiningDateStr.isEmpty() ? LocalDate.parse(joiningDateStr) : null;
			LocalDate dob = dobStr != null && !dobStr.isEmpty() ? LocalDate.parse(dobStr) : null;

			EmployeeDetailsDTO dto = new EmployeeDetailsDTO(
					tuple.get("employeeId", String.class),
					tuple.get("employeeName", String.class),
					tuple.get("role", String.class),
					tuple.get("employeeEmail", String.class),
					tuple.get("designation", String.class),
					joiningDate,  // use correct type
					tuple.get("gender", String.class),
					dob,           // use correct type
					tuple.get("phoneNumber", String.class),
					tuple.get("personalEmail", String.class),
					tuple.get("status", String.class)
			);
			employeeDetails.add(dto);
		}
		return employeeDetails;
	}


	public List<RequirementsDto> getRequirementsByAssignedBy(String userId) {
		// 1. Check if the user exists
		int userExists = requirementsDao.countByUserId(userId);
		if (userExists == 0) {
			logger.warn("User ID '{}' not found in the database", userId);
			throw new ResourceNotFoundException("User ID '" + userId + "' not found in the database.");
		}

		// 2. Get the user_name of the recruiter
		String assignedBy = requirementsDao.findUserNameByUserId(userId);

		// 3. Get current month start and end datetime
		LocalDate today = LocalDate.now();
		LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
		LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);

		// 4. Fetch requirements
		List<RequirementsModel> requirements = requirementsDao.findJobsAssignedByNameAndDateRange(
				assignedBy, startOfMonth, endOfMonth
		);

		// 5. Logging
		logger.info("Fetched {} requirements for user ID '{}' (assigned_by='{}') for current month {} to {}",
				requirements.size(), userId, assignedBy, startOfMonth.toLocalDate(), endOfMonth.toLocalDate());

		// 6. Map to DTO
		return requirements.stream()
				.map(requirement -> {
					RequirementsDto dto = new RequirementsDto();

					dto.setJobId(requirement.getJobId());
					dto.setJobTitle(requirement.getJobTitle());
					dto.setClientName(requirement.getClientName());
					dto.setJobDescription(requirement.getJobDescription());
					dto.setJobDescriptionBlob(requirement.getJobDescriptionBlob());
					dto.setJobType(requirement.getJobType());
					dto.setLocation(requirement.getLocation());
					dto.setJobMode(requirement.getJobMode());
					dto.setExperienceRequired(requirement.getExperienceRequired());
					dto.setNoticePeriod(requirement.getNoticePeriod());
					dto.setRelevantExperience(requirement.getRelevantExperience());
					dto.setQualification(requirement.getQualification());
					dto.setSalaryPackage(requirement.getSalaryPackage());
					dto.setNoOfPositions(requirement.getNoOfPositions());
					dto.setRequirementAddedTimeStamp(requirement.getRequirementAddedTimeStamp());
					dto.setRecruiterIds(requirement.getRecruiterIds());
					dto.setStatus(requirement.getStatus());
					dto.setRecruiterName(requirement.getRecruiterName());
					dto.setAssignedBy(requirement.getAssignedBy());
					dto.setNumberOfSubmissions(requirementsDao.getNumberOfSubmissionsByJobId(requirement.getJobId()));
					dto.setNumberOfInterviews(requirementsDao.getNumberOfInterviewsByJobId(requirement.getJobId()));

					return dto;
				})
				.collect(Collectors.toList());
	}

	public List<RequirementsDto> getRequirementsByAssignedByAndDateRange(String userId, LocalDate startDate, LocalDate endDate) {
		// 1. Validate date range
		if (startDate == null || endDate == null) {
			throw new DateRangeValidationException("Start date and End date must not be null.");
		}
		if (endDate.isBefore(startDate)) {
			throw new DateRangeValidationException("End date cannot be before start date.");
		}

		// 2. Check if user exists
		int userExists = requirementsDao.countByUserId(userId);
		if (userExists == 0) {
			logger.warn("User ID '{}' not found in the database", userId);
			throw new ResourceNotFoundException("User ID '" + userId + "' not found in the database.");
		}

		// 3. Get userName from userId
		String assignedBy = requirementsDao.findUserNameByUserId(userId);

		// 4. Prepare date range
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

		// 5. Fetch data
		List<RequirementsModel> requirements = requirementsDao.findJobsAssignedByNameAndDateRange(
				assignedBy, startDateTime, endDateTime
		);

		// 6. Handle empty result


		// 7. Map to DTOs
		List<RequirementsDto> dtoList = requirements.stream().map(requirement -> {
			RequirementsDto dto = new RequirementsDto();

			dto.setJobId(requirement.getJobId());
			dto.setJobTitle(requirement.getJobTitle());
			dto.setClientName(requirement.getClientName());
			dto.setJobDescription(requirement.getJobDescription());
			dto.setJobDescriptionBlob(requirement.getJobDescriptionBlob());
			dto.setJobType(requirement.getJobType());
			dto.setLocation(requirement.getLocation());
			dto.setJobMode(requirement.getJobMode());
			dto.setExperienceRequired(requirement.getExperienceRequired());
			dto.setNoticePeriod(requirement.getNoticePeriod());
			dto.setRelevantExperience(requirement.getRelevantExperience());
			dto.setQualification(requirement.getQualification());
			dto.setSalaryPackage(requirement.getSalaryPackage());
			dto.setNoOfPositions(requirement.getNoOfPositions());
			dto.setRequirementAddedTimeStamp(requirement.getRequirementAddedTimeStamp());
			dto.setRecruiterIds(requirement.getRecruiterIds());
			dto.setStatus(requirement.getStatus());
			dto.setRecruiterName(requirement.getRecruiterName());
			dto.setAssignedBy(requirement.getAssignedBy());
			dto.setNumberOfSubmissions(requirementsDao.getNumberOfSubmissionsByJobId(requirement.getJobId()));
			dto.setNumberOfInterviews(requirementsDao.getNumberOfInterviewsByJobId(requirement.getJobId()));

			return dto;
		}).collect(Collectors.toList());

		// 8. Log and return
		logger.info("✅ Fetched {} requirements assigned by '{}' (userId: {}) between {} and {}",
				dtoList.size(), assignedBy, userId, startDate, endDate);

		return dtoList;
	}


}