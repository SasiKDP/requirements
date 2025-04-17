package com.dataquadinc.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.dataquadinc.dto.*;
import com.dataquadinc.exceptions.ErrorResponse;
import com.dataquadinc.exceptions.RecruiterNotFoundException;
import com.dataquadinc.exceptions.RequirementNotFoundException;
import com.dataquadinc.model.RequirementsModel;
import com.dataquadinc.repository.RequirementsDao;
import com.dataquadinc.service.EmailService;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dataquadinc.service.RequirementsService;
import org.springframework.web.multipart.MultipartFile;


@CrossOrigin(origins = {"http://35.188.150.92", "http://192.168.0.140:3000", "http://192.168.0.139:3000","https://mymulya.com","http://localhost:3000",
		"http://192.168.0.135:8080","http://192.168.0.135:80","http://182.18.177.16:443","https:mymulya.com:443","http://mymulya.com:443","http://localhost/"})

@RestController
@RequestMapping("/requirements")
//@CrossOrigin("*")
public class RequirementsController {

	@Autowired
	private RequirementsService service;

	@Autowired
	private EmailService emailService ;


	@Autowired
	private RequirementsDao requirementsDao;

	private static final Logger logger = LoggerFactory.getLogger(RequirementsController.class);

	@PostMapping("/assignJob")
	public ResponseEntity<ResponseBean> addRequirement(
			@RequestParam("jobTitle") String jobTitle,
			@RequestParam("clientName") String clientName,
			@RequestParam(value = "jobDescription", required = false) String jobDescription,
			@RequestParam(value = "jobDescriptionFile", required = false) MultipartFile jobDescriptionFile,
			@RequestParam("jobType") String jobType,
			@RequestParam("location") String location,
			@RequestParam("jobMode") String jobMode,
			@RequestParam("experienceRequired") String experienceRequired,
			@RequestParam("noticePeriod") String noticePeriod,
			@RequestParam("relevantExperience") String relevantExperience,
			@RequestParam("qualification") String qualification,
			@RequestParam(value = "salaryPackage", required = false) String salaryPackage,
			@RequestParam("noOfPositions") int noOfPositions,
			@RequestParam("recruiterIds") Set<String> recruiterIds,
			@RequestParam(value = "recruiterName", required = false) Set<String> recruiterName,
			@RequestParam("assignedBy") String assignedBy
	) throws IOException {
		try {
			// Validate that only one of jobDescription or jobDescriptionFile is provided
			if ((jobDescription != null && !jobDescription.isEmpty()) &&
					(jobDescriptionFile != null && !jobDescriptionFile.isEmpty())) {
				// Return error if both are provided
				return ResponseEntity.badRequest()
						.body(ResponseBean.errorResponse("You can either provide a job description text or upload a job description file, but not both.", "Bad Request"));
			}

			// Create RequirementsDto from request parameters
			RequirementsDto requirementsDto = new RequirementsDto();
			requirementsDto.setJobTitle(jobTitle);
			requirementsDto.setClientName(clientName);

			// Logic to set job description
			String finalJobDescription = null;
			byte[] jobDescriptionBlob = null;

			// If jobDescriptionFile is provided, process the file
			if (jobDescriptionFile != null && !jobDescriptionFile.isEmpty()) {
				// Convert the file content to byte array (BLOB)
				jobDescriptionBlob = jobDescriptionFile.getBytes(); // Save the file as a BLOB
				// Set jobDescription to null since we are using the file (BLOB)
				finalJobDescription = null;
			}
			// If jobDescription (text) is provided, use it
			else if (jobDescription != null && !jobDescription.isEmpty()) {
				finalJobDescription = jobDescription;
				// Set jobDescriptionBlob to null since we are using the text
				jobDescriptionBlob = null;
			}

			// Set the job description (from file or text)
			requirementsDto.setJobDescription(finalJobDescription);
			requirementsDto.setJobDescriptionBlob(jobDescriptionBlob); // Set the BLOB (or null)


			// Set the other fields
			requirementsDto.setJobType(jobType);
			requirementsDto.setLocation(location);
			requirementsDto.setJobMode(jobMode);
			requirementsDto.setExperienceRequired(experienceRequired);
			requirementsDto.setNoticePeriod(noticePeriod);
			requirementsDto.setRelevantExperience(relevantExperience);
			requirementsDto.setQualification(qualification);
			requirementsDto.setSalaryPackage(salaryPackage);
			requirementsDto.setNoOfPositions(noOfPositions);
			requirementsDto.setRecruiterIds(recruiterIds);
			requirementsDto.setRecruiterName(recruiterName);
			requirementsDto.setAssignedBy(assignedBy);

			// Call the service to create the requirement
			RequirementAddedResponse response = service.createRequirement(requirementsDto);

			// Return success response
			return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBean.successResponse("Requirement added successfully", response));

		} catch (IllegalArgumentException e) {
			// Handle specific validation error
			return ResponseEntity.badRequest().body(ResponseBean.errorResponse(e.getMessage(), "Bad Request"));
		} catch (Exception e) {
			// General exception handling
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ResponseBean.errorResponse("Unexpected error occurred: " + e.getMessage(), "Internal Server Error"));
		}
	}

	@GetMapping("/download-job-description/{jobId}")
	public ResponseEntity<Object> downloadJobDescription(@PathVariable String jobId) {
		try {
			logger.info("Downloading job description for job ID: {}", jobId);

			// Fetch the requirement details from the database
			RequirementsModel requirement = requirementsDao.findById(jobId)
					.orElseThrow(() -> new RequirementNotFoundException("Requirement not found with Job Id: " + jobId));

			logger.debug("Requirement found for job ID: {}", jobId);

			// Fetch the job description BLOB from the RequirementsModel entity
			byte[] jobDescriptionBytes = requirement.getJobDescriptionBlob();

			// Check if job description is available
			if (jobDescriptionBytes == null || jobDescriptionBytes.length == 0) {
				logger.error("Job description is missing for job ID: {}", jobId);
				// Return a ResponseBean with an error message
				ResponseBean errorResponse = new ResponseBean(false, "Job description is missing for job ID: " + jobId, "No file found", null);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
			}

			logger.debug("Job description fetched successfully for job ID: {}", jobId);

			// Dynamically set the filename based on the job title and file type
			String filename = requirement.getJobTitle() + "-JobDescription";

			// Use Apache Tika to detect the content type
			Tika tika = new Tika();
			String contentType = tika.detect(jobDescriptionBytes);  // Detects the content type of the byte array
			String fileExtension = ""; // Default extension will be empty to handle dynamic extension

			// Based on detected content type, set appropriate file extension
			if (contentType.equals("application/pdf")) {
				fileExtension = ".pdf";
			} else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
				fileExtension = ".docx";
			} else if (contentType.equals("text/plain")) {
				fileExtension = ".txt";
			} else if (contentType.equals("image/png")) {
				fileExtension = ".png";
			} else if (contentType.equals("image/jpeg")) {
				fileExtension = ".jpg";
			} else {
				// Log warning for unknown content types
				logger.warn("Unknown content type detected: {}", contentType);
			}

			// Append the detected file extension to the filename if available
			if (!fileExtension.isEmpty()) {
				filename += fileExtension;
			}

			// Convert the byte array to a ByteArrayResource for downloading
			ByteArrayResource resource = new ByteArrayResource(jobDescriptionBytes);

			// Return the file as a response for download with the proper content type and filename
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))  // Set the content type based on detection
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
					.body(resource);

		} catch (RequirementNotFoundException e) {
			logger.error("Requirement not found: {}", e.getMessage());
			// Return a ResponseBean with the error message
			ResponseBean errorResponse = new ResponseBean(false, e.getMessage(), "Not Found", null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

		} catch (Exception e) {
			logger.error("Unexpected error while downloading job description for job ID {}: {}", jobId, e.getMessage());
			// Return a ResponseBean with the error message
			ResponseBean errorResponse = new ResponseBean(false, "Unexpected error: " + e.getMessage(), "Internal Server Error", null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}
	// Helper method to check if the byte array is a PDF (you could improve this with more detailed checks)
	private boolean isPdf(byte[] data) {
		return data != null && data.length > 4 && data[0] == '%'
				&& data[1] == 'P' && data[2] == 'D' && data[3] == 'F';
	}

	// Helper method to check if the byte array is a DOCX file
	private boolean isDocx(byte[] data) {
		return data != null && data.length > 4 && data[0] == (byte) 0x50 && data[1] == (byte) 0x4B
				&& data[2] == (byte) 0x03 && data[3] == (byte) 0x04;
	}

	// Helper method to check if the byte array is a plain text file
	private boolean isTextFile(byte[] data) {
		// You can improve this check further (e.g., by checking for byte patterns in text files)
		return data != null && data.length > 0 && new String(data).matches(".*\\w.*");
	}
	// Helper method to check if the byte array is an XLSX file
	private boolean isXlsx(byte[] data) {
		return data != null && data.length > 4 && data[0] == (byte) 0x50 && data[1] == (byte) 0x4B
				&& data[2] == (byte) 0x03 && data[3] == (byte) 0x04;  // (ZIP signature for XLSX)
	}

	// Helper method to check if the byte array is a PowerPoint file
	private boolean isPptx(byte[] data) {
		return data != null && data.length > 4 && data[0] == (byte) 0x50 && data[1] == (byte) 0x4B
				&& data[2] == (byte) 0x03 && data[3] == (byte) 0x04;  // (ZIP signature for PPTX)
	}


	@GetMapping("/getAssignments")
	public ResponseEntity<?> getRequirements() {
		List<RequirementsDto> requirements = (List<RequirementsDto>) service.getRequirementsDetails();

		if (requirements == null || requirements.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Requirements Not Found", LocalDateTime.now()), HttpStatus.NOT_FOUND);
		}

		// Clean up recruiterName field
		for (RequirementsDto dto : requirements) {
			Set<String> cleanedNames = dto.getRecruiterName().stream()
					.map(name -> name.replaceAll("[\\[\\]\"]", "")) // Remove brackets and extra quotes
					.collect(Collectors.toSet());

			dto.setRecruiterName(cleanedNames);
		}

		return new ResponseEntity<>(requirements, HttpStatus.OK);
	}

	@GetMapping("/filterByDate")
	public ResponseEntity<?> getRequirementsByDateRange(
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		List<RequirementsDto> requirements = service.getRequirementsByDateRange(startDate, endDate);

		if (requirements == null || requirements.isEmpty()) {
			logger.warn("⚠️ No requirements found between {} and {}", startDate, endDate);
			return new ResponseEntity<>(new ErrorResponse(
					HttpStatus.NOT_FOUND.value(),
					"No requirements found between " + startDate + " and " + endDate,
					LocalDateTime.now()), HttpStatus.NOT_FOUND);
		}

		for (RequirementsDto dto : requirements) {
			Set<String> cleanedNames = dto.getRecruiterName().stream()
					.map(name -> name.replaceAll("[\\[\\]\"]", ""))
					.collect(Collectors.toSet());
			dto.setRecruiterName(cleanedNames);
		}

		// ✅ This log will now appear last
		ResponseEntity<?> response = new ResponseEntity<>(requirements, HttpStatus.OK);
		logger.info("✅ Fetched {} requirements between {} and {}", requirements.size(), startDate, endDate);
		return response;
	}





	@GetMapping("/get/{jobId}")
	public ResponseEntity<RequirementsDto> getRequirementById(@PathVariable String jobId) {
		return new ResponseEntity<>(service.getRequirementDetailsById(jobId), HttpStatus.OK);
	}

	@PutMapping("/assign")
	public ResponseEntity<?> assignRequirement(@RequestParam String jobId, @RequestParam List<String> recruiterIds) {
		try {
			// Retrieve the job requirement to check if the job exists
			RequirementsModel requirement = requirementsDao.findById(jobId)
					.orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id : " + jobId));

			// Flag to check if any error occurs before sending emails
			boolean allEmailsSentSuccessfully = true;

			// Convert List to Set and clean the recruiter IDs
			Set<String> cleanedRecruiterIds = recruiterIds.stream()
					.map(id -> id.replaceAll("[\"\\[\\]]", "").trim())
					.collect(Collectors.toSet());

			// Assign each recruiter to the job and send emails immediately
			for (String recruiterId : recruiterIds) {
				try {
					// Assign the recruiter and save email to DB
					service.assignToRecruiter(jobId, Collections.singleton(recruiterId));

					// Fetch the recruiter's email and name (assuming these methods exist in your service)
					String recruiterEmail = service.getRecruiterEmail(recruiterId);
					String recruiterName = service.getRecruiterName(recruiterId); // This should be available in the service

					// Log recruiter ID and fetched email
					logger.info("Fetched recruiterId: {} with email: {}", recruiterId, recruiterEmail);

					// Check if the email and name are not null or empty
					if (recruiterEmail == null || recruiterEmail.isEmpty()) {
						throw new RecruiterNotFoundException("Email for recruiter " + recruiterId + " not found");
					}

					// Add the recruiter name to the job requirement's recruiterName set
					requirement.getRecruiterName().add(recruiterName); // Add recruiter name to Set

					System.out.println("Recruiter names before saving: " + requirement.getRecruiterName());

					// Prepare the email subject and body
					String subject = "New Job Assignment: " + requirement.getJobTitle();

					String body = "Dear " + recruiterName + ",\n\n" +
							"I hope this message finds you well. \n\n" +
							"You have been assigned a new job requirement, and the details are outlined below:  \n\n" +
							"Job Title: " + requirement.getJobTitle() + "\n" +
							"Client: " + requirement.getClientName() + "\n" +
							"Location: " + requirement.getLocation() + "\n" +
							"Job Type: " + requirement.getJobType() + "\n" +
							"Experience Required: " + requirement.getExperienceRequired() + " years\n\n" +
							"Assigned By: " + requirement.getAssignedBy() + "\n\n" + // Added Assigned By field
							"Please take a moment to review the details and proceed with the necessary actions. Additional information can be accessed via your dashboard.\n\n" +
							"If you have any questions or require further clarification, feel free to reach out.\n\n" +
							"Best Regards,\nDataquad";

					// Send email to the recruiter
					emailService.sendEmail(recruiterEmail, subject, body);

					// Log the email sending action
					logger.info("Email sent to recruiter {} at {} for job: {}", recruiterEmail, requirement.getJobTitle());

				} catch (Exception e) {
					// Log the full exception stack trace for better error diagnosis
					logger.error("Failed to send email to recruiter {} for job {}. Error: {}", recruiterId, requirement.getJobTitle(), e.getMessage(), e);
					allEmailsSentSuccessfully = false;
				}
			}

			// Return success response after attempting to assign recruiters and send emails
			if (allEmailsSentSuccessfully) {
				return ResponseEntity.ok(ResponseBean.successResponse("Recruiters assigned and email notifications sent successfully.", null));
			} else {
				return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
						.body(ResponseBean.errorResponse("Some recruiter emails failed to send. Please check the logs for details.", null));
			}

		} catch (Exception e) {
			// Handle errors (e.g., if jobId doesn't exist)
			logger.error("Error assigning recruiters: {}", e.getMessage(), e);
			return ResponseEntity.badRequest().body(ResponseBean.errorResponse("Error assigning recruiters: " + e.getMessage(), e.toString()));
		}
	}



	@PutMapping("/updateStatus")
	public ResponseEntity<Void> updateStatus(@RequestBody StatusDto status) {
		service.statusUpdate(status);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/recruiter/{recruiterId}")
	public ResponseEntity<List<RecruiterRequirementsDto>> getJobsByRecruiter(@PathVariable String recruiterId) {
		return new ResponseEntity<>(service.getJobsAssignedToRecruiter(recruiterId),HttpStatus.OK);
	}
	@PutMapping("/updateRequirement/{jobId}")
	public ResponseEntity<Map<String, Object>> updateRequirement(
			@PathVariable String jobId,
			@RequestParam("jobTitle") String jobTitle,
			@RequestParam("clientName") String clientName,
			@RequestParam(value = "jobDescription", required = false) String jobDescription,
			@RequestParam(value = "jobDescriptionFile", required = false) MultipartFile jobDescriptionFile,
			@RequestParam("jobType") String jobType,
			@RequestParam("location") String location,
			@RequestParam("jobMode") String jobMode,
			@RequestParam("status") String status,
			@RequestParam("experienceRequired") String experienceRequired,
			@RequestParam("noticePeriod") String noticePeriod,
			@RequestParam("relevantExperience") String relevantExperience,
			@RequestParam("qualification") String qualification,
			@RequestParam(value = "salaryPackage", required = false) String salaryPackage,
			@RequestParam("noOfPositions") int noOfPositions,
			@RequestParam("recruiterIds") Set<String> recruiterIds,
			@RequestParam(value = "recruiterName", required = false) Set<String> recruiterName,
			@RequestParam("assignedBy") String assignedBy
	) throws IOException {
		try {
			logger.info("Received request to update requirement with jobId: {}", jobId);
			// Fetch the existing requirement
			RequirementsDto existingRequirement = service.getRequirementDetailsById(jobId);
			if (existingRequirement == null) {
				logger.warn("Both jobDescription and jobDescriptionFile provided. Only one should be given.");
				return ResponseEntity.badRequest().body(Map.of("error", "Provide either jobDescription or jobDescriptionFile, not both."));
			}
			logger.info("Updating fields for requirement with jobId: {}", jobId);

			existingRequirement.setJobTitle(jobTitle);
			existingRequirement.setClientName(clientName);
			existingRequirement.setStatus(status);
			existingRequirement.setJobType(jobType);
			existingRequirement.setLocation(location);
			existingRequirement.setJobMode(jobMode);
			existingRequirement.setExperienceRequired(experienceRequired);
			existingRequirement.setNoticePeriod(noticePeriod);
			existingRequirement.setRelevantExperience(relevantExperience);
			existingRequirement.setQualification(qualification);
			existingRequirement.setSalaryPackage(salaryPackage);
			existingRequirement.setNoOfPositions(noOfPositions);
			existingRequirement.setRecruiterIds(recruiterIds);
			existingRequirement.setRecruiterName(recruiterName);
			existingRequirement.setAssignedBy(assignedBy);
			if (jobDescriptionFile != null && !jobDescriptionFile.isEmpty()) {
				logger.info("Setting jobDescriptionBlob from file for jobId: {}", jobId);
				existingRequirement.setJobDescription(null);
				existingRequirement.setJobDescriptionBlob(jobDescriptionFile.getBytes());
			} else {
				logger.info("Setting jobDescription text for jobId: {}", jobId);
				existingRequirement.setJobDescription(jobDescription);
				existingRequirement.setJobDescriptionBlob(null);
			}

			service.updateRequirementDetails(existingRequirement);
			logger.info("Requirement updated successfully for jobId: {}", jobId);

			String subject = "";
			String body = "";

			if (status.equalsIgnoreCase("closed")) {
				subject = "📌 Requirement Closed - " + jobTitle + " (Job ID: " + jobId + ")";
				body = String.format(
						"Dear Recruiter,\n\n" +
								"We would like to inform you that the following requirement has been formally marked as **CLOSED**:\n\n" +
								"• Job Title   : %s\n" +
								"• Client Name : %s\n" +
								"• Job ID      : %s\n" +
								"• Location    : %s\n\n" +
								"Kindly update your records accordingly and stop any further processing for this role.\n\n" +
								"We appreciate your continued efforts and collaboration.\n\n" +
								"Warm regards,\n" +
								"Dataquad Team",
						jobTitle, clientName, jobId, location
				);
				logger.info("Prepared email content for CLOSED status");
			} else if (status.equalsIgnoreCase("hold")) {
				subject = "⏸️ Requirement On Hold - " + jobTitle + " (Job ID: " + jobId + ")";
				body = String.format(
						"Dear Recruiter,\n\n" +
								"Please be informed that the below requirement is currently placed **ON HOLD**:\n\n" +
								"• Job Title   : %s\n" +
								"• Client Name : %s\n" +
								"• Job ID      : %s\n" +
								"• Location    : %s\n\n" +
								"Kindly hold off on submitting any further candidates for this role until we notify you of any updates.\n" +
								"If you have any questions or need clarification, please feel free to reach out to the BDM or the Admin team.\n\n" +
								"Thank you for your cooperation.\n\n" +
								"Warm regards,\n" +
								"Dataquad Team",
						jobTitle, clientName, jobId, location
				);
				logger.info("Prepared email content for HOLD status");
			}

				if ((status.equalsIgnoreCase("closed") || status.equalsIgnoreCase("hold")) && recruiterName != null) {
					for (String email : recruiterName) {
						emailService.sendEmail(email, subject, body);
						logger.info("Email sent to: {}", email);
					}
				}
				Map<String, Object> response = new LinkedHashMap<>();
				response.put("jobId", jobId);
				response.put("updatedStatus", status.toLowerCase());
				response.put("assignedRecruiters", recruiterIds);
				response.put("message", "Status updated and email notifications sent successfully.");
				logger.info("Returning successful update response for jobId: {}", jobId);
				return ResponseEntity.ok(response);

			} catch(Exception e){
				logger.error("Error updating requirement with jobId: {}: {}", jobId, e.getMessage(), e);

				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("error", "Unexpected error: " + e.getMessage()));
			}
		}



	@GetMapping("/{jobId}")
	public ResponseEntity<RecruiterDetailsDTO> getRecruiterDetails(@PathVariable String jobId) {
		RecruiterDetailsDTO recruiterDetails = service.getRecruiterDetailsByJobId(jobId);

		if (recruiterDetails != null) {
			return ResponseEntity.ok(recruiterDetails);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	@GetMapping("/stats")
	public ResponseEntity<CandidateStatsResponse> getCandidateStats() {
		CandidateStatsResponse stats = service.getCandidateStats();
		return ResponseEntity.ok(stats);
	}


	// Fetch both Submitted Candidates and Scheduled Interviews in one API call
	@GetMapping("/list/{userId}")
	public CandidateResponseDTO getCandidateData(@PathVariable String userId) {
		return service.getCandidateData(userId);
	}


}
