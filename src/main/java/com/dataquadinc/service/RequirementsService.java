package com.dataquadinc.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

			for (String recruiterId : recruiterIds) {
				try {
					// Clean the recruiterId (remove quotes if present)
					String cleanedRecruiterId = cleanRecruiterId(recruiterId);
					logger.debug("Processing recruiter with ID: {} (cleaned ID: {})", recruiterId, cleanedRecruiterId);

					// Fetch recruiter email and username from the database
					Tuple userTuple = requirementsDao.findUserEmailAndUsernameByUserId(cleanedRecruiterId);

					if (userTuple != null) {
						String recruiterEmail = userTuple.get(0, String.class);  // Fetch email
						String recruiterName = userTuple.get(1, String.class);  // Fetch username

						if (recruiterEmail != null && !recruiterEmail.isEmpty()) {
							// Construct and send email
							String subject = "New Job Assignment: " + model.getJobTitle();
							String text = constructEmailBody(model, recruiterName);

							logger.info("Attempting to send email to recruiter: {} <{}> for job ID: {}",
									recruiterName, recruiterEmail, model.getJobId());

							try {
								emailService.sendEmail(recruiterEmail, subject, text);
								logger.info("Email successfully sent to recruiter: {} <{}> for job ID: {}",
										recruiterName, recruiterEmail, model.getJobId());
							} catch (Exception e) {
								logger.error("Failed to send email to recruiter: {} <{}> for job ID: {}. Error: {}",
										recruiterName, recruiterEmail, model.getJobId(), e.getMessage(), e);
							}
						} else {
							logger.error("Empty or null email found for recruiter ID: {} (Name: {}) for job ID: {}",
									cleanedRecruiterId, recruiterName, model.getJobId());
						}
					} else {
						logger.error("No user information found for recruiter ID: {} for job ID: {}",
								cleanedRecruiterId, model.getJobId());
					}
				} catch (Exception e) {
					logger.error("Error processing recruiter {} for job ID: {}. Error: {}",
							recruiterId, model.getJobId(), e.getMessage(), e);
				}
			}

			logger.info("Completed email sending process for job ID: {}", model.getJobId());
		} catch (Exception e) {
			logger.error("Critical error in sending emails to recruiters for job ID: {}. Error: {}",
					model.getJobId(), e.getMessage(), e);
			throw new RuntimeException("Error in sending emails to recruiters: " + e.getMessage(), e);
		}
	}

	// Update constructEmailBody method to use recruiterName instead of fetching separately
	private String constructEmailBody(RequirementsModel model, String recruiterName) {
		return "Dear " + recruiterName + ",\n\n" +
				"I hope you are doing well.\n\n" +
				"You have been assigned a new job requirement. Please find the details below:\n\n" +
				"▶ Job Title: " + model.getJobTitle() + "\n" +
				"▶ Client: " + model.getClientName() + "\n" +
				"▶ Location: " + model.getLocation() + "\n" +
				"▶ Job Type: " + model.getJobType() + "\n" +
				"▶ Experience Required: " + model.getExperienceRequired() + " years\n" +
				"▶ Assigned By: " + model.getAssignedBy() + "\n\n" +
				"Please review the details and proceed with the necessary actions. Additional information is available on your dashboard.\n\n" +
				"If you have any questions or need further clarification, feel free to reach out.\n\n" +
				"Best regards,\n" +
				"Dataquad";
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


	public Object getRequirementsDetails() {
		// Fetch all requirements from the database
		List<RequirementsModel> requirementsList = requirementsDao.findAll();

		// List to hold the DTOs
		List<RequirementsDto> dtoList = requirementsList.stream()
				.map(requirement -> {
					RequirementsDto dto = new RequirementsDto();

					// Set the basic requirement data
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

					// Get the jobId for the current requirement
					String jobId = requirement.getJobId();  // Assuming jobId is a String

					// Fetch individual stats using the DAO methods for the current jobId
					dto.setNumberOfSubmissions(requirementsDao.getNumberOfSubmissionsByJobId(jobId));
					dto.setNumberOfInterviews(requirementsDao.getNumberOfInterviewsByJobId(jobId)); // Pass clientName here

					return dto;
				})
				.collect(Collectors.toList());

		// Return response, if the list is empty, return error response
		if (dtoList.isEmpty()) {
			return new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Requirements Not Found", LocalDateTime.now());
		} else {
			return dtoList;
		}
	}


	public List<RequirementsDto> getRequirementsByDateRange(LocalDate startDate, LocalDate endDate) {
		// 💥 First check: Date range should not exceed 31 days
		if (ChronoUnit.DAYS.between(startDate, endDate) > 31) {
			throw new DateRangeValidationException("Date range must not exceed one month.");
		}

		// 💥 Second check: End date must not be before start date
		if (endDate.isBefore(startDate)) {
			throw new DateRangeValidationException("End date cannot be before start date.");
		}

		// 💥 Third check: Start date must be within the last 1 month
		LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
		if (startDate.isBefore(oneMonthAgo)) {
			throw new DateRangeValidationException("Start date must be within the last 1 month.");
		}

		List<RequirementsModel> requirements = requirementsDao.findByRequirementAddedTimeStampBetween(startDate, endDate);

		if (requirements.isEmpty()) {
			throw new RequirementNotFoundException("No requirements found between " + startDate + " and " + endDate);
		}

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
		List<RequirementsModel> jobsByRecruiterId = requirementsDao.findJobsByRecruiterId(recruiterId);

		if (jobsByRecruiterId.isEmpty()) {
			throw new NoJobsAssignedToRecruiterException("No Jobs Assigned To Recruiter : " + recruiterId);
		}

		return jobsByRecruiterId.stream()
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
			return new ResponseBean(true, "Updated Successfully", null, null);
		} catch (Exception e) {
			logger.error("Error updating requirement", e);
			return new ResponseBean(false, "Error updating requirement", "Internal Server Error", null);
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

	public RecruiterDetailsDTO getRecruiterDetailsByJobId(String jobId) {
		// Fetch the requirement by job ID
		Optional<RequirementsModel> requirement = requirementsDao.findRecruitersByJobId(jobId);

		if (requirement.isEmpty()) {
			throw new RequirementNotFoundException("Requirement Not Found with Id: " + jobId);
		}

		// Get recruiters using the new query method
		List<RecruiterInfoDto> recruiters = getRecruitersForJob(jobId);

		// Initialize maps for submitted and interview scheduled candidates
		Map<String, List<CandidateDto>> submittedCandidates = new HashMap<>();
		Map<String, List<InterviewCandidateDto>> interviewScheduledCandidates = new HashMap<>();

		// Fetch candidates for each recruiter
		for (RecruiterInfoDto recruiter : recruiters) {
			String recruiterId = recruiter.getRecruiterId();
			String recruiterName = recruiter.getRecruiterName();

			// Fetch submitted candidates
			List<Tuple> assignedCandidatesList = requirementsDao.findCandidatesByJobIdAndRecruiterId(jobId, recruiterId);
			List<CandidateDto> assignedCandidateDtos = mapCandidates(assignedCandidatesList, recruiterName);
			submittedCandidates.put(recruiterName, assignedCandidateDtos);

			// Fetch interview scheduled candidates
			List<Tuple> interviewCandidatesList = requirementsDao.findInterviewScheduledCandidatesByJobIdAndRecruiterId(jobId, recruiterId);
			List<InterviewCandidateDto> interviewCandidateDtos = mapInterviewCandidates(interviewCandidatesList, recruiterName);
			interviewScheduledCandidates.put(recruiterName, interviewCandidateDtos);
		}

		// Return DTO with recruiter details and candidate groups
		return new RecruiterDetailsDTO(recruiters, submittedCandidates, interviewScheduledCandidates);
	}

	/**
	 * Maps Tuple data to CandidateDto objects, including recruiter name.
	 */
	private List<CandidateDto> mapCandidates(List<Tuple> candidatesList, String recruiterName) {
		return candidatesList.stream()
				.map(candidate -> {
					try {
						return new CandidateDto(
								getTupleValue(candidate, "candidate_id"),
								getTupleValue(candidate, "full_name"),
								getTupleValue(candidate, "candidate_email_id"),
								getTupleValue(candidate, "interview_status"),
								getTupleValue(candidate, "contact_number"),
								getTupleValue(candidate, "qualification"),
								getTupleValue(candidate, "skills"),
								getTupleValue(candidate, "overall_feedback")// Include recruiter name
						);
					} catch (Exception e) {
						System.err.println("Error mapping candidate: " + candidate + " | Exception: " + e.getMessage());
						return null;
					}
				})
				.filter(Objects::nonNull) // Remove any null entries
				.collect(Collectors.toList());
	}

	/**
	 * Maps Tuple data to InterviewCandidateDto objects, including recruiter name.
	 */
	private List<InterviewCandidateDto> mapInterviewCandidates(List<Tuple> candidatesList, String recruiterName) {
		return candidatesList.stream()
				.map(candidate -> {
					try {
						Timestamp interviewTimestamp = candidate.get("interview_date_time", Timestamp.class);
						String interviewDateTime = interviewTimestamp != null
								? interviewTimestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
								: null;

						return new InterviewCandidateDto(
								getTupleValue(candidate, "candidate_id"),
								getTupleValue(candidate, "full_name"),
								getTupleValue(candidate, "candidate_email_id"),
								getTupleValue(candidate, "interview_status"),
								getTupleValue(candidate, "interview_level"),
								interviewDateTime// ✅ Use formatted date-time string//
						);
					} catch (Exception e) {
						System.err.println("Error mapping interview candidate: " + candidate + " | Exception: " + e.getMessage());
						return null;
					}
				})
				.filter(Objects::nonNull)
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
		List<EmployeeCandidateDTO> employeeDtoList = new ArrayList<>();
		List<TeamleadCandidateDTO> teamleadDtoList = new ArrayList<>();

		// 👤 Employee Stats
		List<Tuple> employeeStats = requirementsDao.getEmployeeCandidateStats();
		employeeDtoList.addAll(employeeStats.stream()
				.map(tuple -> new EmployeeCandidateDTO(
						tuple.get("employeeId", String.class),
						tuple.get("employeeName", String.class),
						tuple.get("employeeEmail", String.class),
						"Employee",
						convertToInt(tuple.get("numberOfClients")),
						convertToInt(tuple.get("numberOfRequirements")),
						convertToInt(tuple.get("numberOfSubmissions")),
						convertToInt(tuple.get("numberOfInterviews")),
						convertToInt(tuple.get("numberOfPlacements"))
				))
				.collect(Collectors.toList()));

		// 👨‍🏫 Teamlead Stats
		List<Tuple> teamleadStats = requirementsDao.getTeamleadCandidateStats();
		teamleadDtoList.addAll(teamleadStats.stream()
				.map(tuple -> new TeamleadCandidateDTO(
						tuple.get("employeeId", String.class),
						tuple.get("employeeName", String.class),
						tuple.get("employeeEmail", String.class),
						"Teamlead",
						convertToInt(tuple.get("numberOfClients")),
						convertToInt(tuple.get("numberOfRequirements")),
						convertToInt(tuple.get("selfSubmissions")),
						convertToInt(tuple.get("selfInterviews")),
						convertToInt(tuple.get("selfPlacements")),
						convertToInt(tuple.get("teamSubmissions")),
						convertToInt(tuple.get("teamInterviews")),
						convertToInt(tuple.get("teamPlacements"))
				))
				.collect(Collectors.toList()));

		return new CandidateStatsResponse(employeeDtoList, teamleadDtoList);
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
		// Fetching the various required details
		List<SubmittedCandidateDTO> submittedCandidates = requirementsDao.findSubmittedCandidatesByUserId(userId);
		List<InterviewScheduledDTO> scheduledInterviews = requirementsDao.findScheduledInterviewsByUserId(userId);
		List<JobDetailsDTO> jobDetails = requirementsDao.findJobDetailsByUserId(userId);
		List<PlacementDetailsDTO> placementDetails = requirementsDao.findPlacementCandidatesByUserId(userId);
		List<ClientDetailsDTO> clientDetails = requirementsDao.findClientDetailsByUserId(userId); // Fetch client details
		List<Tuple> employeeDetailsTuples = requirementsDao.getEmployeeDetailsByUserId(userId); // Fetch employee details

		// Group data dynamically (no need to group employee details by client name)
		Map<String, List<SubmittedCandidateDTO>> groupedSubmissions = groupByClientName(submittedCandidates);
		Map<String, List<InterviewScheduledDTO>> groupedInterviews = groupByClientName(scheduledInterviews);
		Map<String, List<PlacementDetailsDTO>> groupedPlacements = groupByClientName(placementDetails);
		Map<String, List<JobDetailsDTO>> groupedJobDetails = groupByClientName(jobDetails);

		// Convert List<ClientDetailsDTO> to a Map
		Map<String, List<ClientDetailsDTO>> groupedClientDetails = groupByClientName(clientDetails);

		// Convert List<Tuple> to List<EmployeeDetailsDTO>
		List<EmployeeDetailsDTO> employeeDetails = mapEmployeeDetailsTuples(employeeDetailsTuples);

		// Directly return the employeeDetails list in the CandidateResponseDTO constructor
		return new CandidateResponseDTO(groupedSubmissions, groupedInterviews, groupedPlacements,
				groupedJobDetails, groupedClientDetails, employeeDetails);
	}

	// Helper method to add client names to the set (avoids duplicate code)
	private <T> void addClientNamesToSet(List<T> list, Set<String> allClients) {
		list.forEach(item -> {
			String clientName = (item instanceof ClientDetailsDTO) ? ((ClientDetailsDTO) item).getClientName() :
					(item instanceof SubmittedCandidateDTO) ? ((SubmittedCandidateDTO) item).getClientName() :
							(item instanceof InterviewScheduledDTO) ? ((InterviewScheduledDTO) item).getClientName() :
									(item instanceof PlacementDetailsDTO) ? ((PlacementDetailsDTO) item).getClientName() :
											(item instanceof JobDetailsDTO) ? ((JobDetailsDTO) item).getClientName() : "";
			allClients.add(normalizeClientName(clientName));
		});
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

	// Ensure all clients are present in the maps
	private void ensureClientPresenceInMaps(Set<String> allClients, Map<String, List<SubmittedCandidateDTO>> groupedSubmissions,
											Map<String, List<InterviewScheduledDTO>> groupedInterviews, Map<String, List<PlacementDetailsDTO>> groupedPlacements,
											Map<String, List<JobDetailsDTO>> groupedJobDetails, Map<String, List<Tuple>> groupedEmployeeDetails,
											Map<String, List<ClientDetailsDTO>> groupedClientDetails) {
		allClients.forEach(client -> {
			groupedSubmissions.putIfAbsent(client, new ArrayList<>());
			groupedInterviews.putIfAbsent(client, new ArrayList<>());
			groupedPlacements.putIfAbsent(client, new ArrayList<>());
			groupedJobDetails.putIfAbsent(client, new ArrayList<>());
			groupedEmployeeDetails.putIfAbsent(client, new ArrayList<>());

			// Ensure ClientDetailsDTO exists, even if empty
			groupedClientDetails.putIfAbsent(client, Collections.singletonList(new ClientDetailsDTO() {
				@Override
				public String getClientName() {
					return client;
				}

				@Override
				public String getClientId() {
					return null;
				}

				@Override
				public String getClientAddress() {
					return null;
				}

				@Override
				public String getOnBoardedBy() {
					return null;
				}

				@Override
				public String getClientSpocName() {
					return null;
				}

				@Override
				public String getClientSpocMobileNumber() {
					return null;
				}
			}));

			// Ensure employee details exist, even if empty
			groupedEmployeeDetails.putIfAbsent(client, new ArrayList<>());
		});
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
			EmployeeDetailsDTO dto = new EmployeeDetailsDTO(
					tuple.get("employeeId", String.class),
					tuple.get("employeeName", String.class),
					tuple.get("role", String.class),
					tuple.get("employeeEmail", String.class),
					tuple.get("designation", String.class),
                    LocalDate.parse(tuple.get("joiningDate", String.class)), // Assuming it's a String, you can convert it to LocalDate if needed
					tuple.get("gender", String.class),
                    LocalDate.parse(tuple.get("dob", String.class)), // Assuming it's a String, you can convert it to LocalDate if needed
					tuple.get("phoneNumber", String.class),
					tuple.get("personalEmail", String.class),
					tuple.get("status", String.class)
			);
			employeeDetails.add(dto);
		}

		return employeeDetails;
	}
}