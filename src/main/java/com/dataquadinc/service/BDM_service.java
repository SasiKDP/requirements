package com.dataquadinc.service;



import com.dataquadinc.dto.*;
import com.dataquadinc.exceptions.ClientNotFoundException;
import com.dataquadinc.exceptions.RequirementNotFoundException;
import com.dataquadinc.model.BDM_Client;
import com.dataquadinc.model.RequirementsModel;
import com.dataquadinc.repository.BDM_Repo;
import com.dataquadinc.repository.RequirementsDao;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BDM_service {

    @Autowired
    private BDM_Repo repository;


    @Autowired
    private RequirementsDao requirementsDao;

    public BDM_Client saveClient(BDM_Client client) {
        client.setId(generateCustomId());
        return repository.save(client);
    }

    private String generateCustomId() {
        List<BDM_Client> clients = repository.findAll();
        int maxNumber = 0;

        for (BDM_Client client : clients) {
            String id = client.getId();
            if (id != null && id.startsWith("CLIENT")) {
                try {
                    int num = Integer.parseInt(id.replace("CLIENT", ""));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid IDs
                }
            }
        }
        return String.format("CLIENT%03d", maxNumber + 1);
    }

    private BDM_Dto convertToDTO(BDM_Client client) {
        BDM_Dto dto = new BDM_Dto();
        dto.setId(client.getId());
        dto.setClientName(client.getClientName());
        dto.setClientAddress(client.getClientAddress());
        dto.setNetPayment(client.getNetPayment());
        dto.setGst(client.getGst());
        dto.setSupportingCustomers(client.getSupportingCustomers());
        dto.setClientWebsiteUrl(client.getClientWebsiteUrl());
        dto.setClientLinkedInUrl(client.getClientLinkedInUrl());
        dto.setClientSpocName(client.getClientSpocName());
        dto.setClientSpocEmailid(client.getClientSpocEmailid());
        dto.setDocumentData(client.getDocumentedData());
        dto.setSupportingDocuments(client.getSupportingDocuments());
        dto.setClientSpocLinkedin(client.getClientSpocLinkedin());
        dto.setClientSpocMobileNumber(client.getClientSpocMobileNumber());
        return dto;
    }

    private BDM_Client convertToEntity(BDM_Dto dto) {
        BDM_Client client = new BDM_Client();
        client.setId(dto.getId());
        client.setClientName(dto.getClientName());
        client.setClientAddress(dto.getClientAddress());
        client.setNetPayment(dto.getNetPayment());
        client.setGst(dto.getGst());
        client.setSupportingCustomers(dto.getSupportingCustomers());
        client.setClientWebsiteUrl(dto.getClientWebsiteUrl());
        client.setClientLinkedInUrl(dto.getClientLinkedInUrl());
        client.setClientSpocName(dto.getClientSpocName());
        client.setClientSpocEmailid(dto.getClientSpocEmailid());
        client.setDocumentedData(dto.getDocumentData());
        client.setSupportingDocuments(dto.getSupportingDocuments());
        client.setClientSpocLinkedin(dto.getClientSpocLinkedin());
        client.setClientSpocMobileNumber(dto.getClientSpocMobileNumber());

        // Store only file name, NOT the file content


        return client;
    }


    public BDM_Dto createClient(BDM_Dto dto, List<MultipartFile> files) throws IOException {
        BDM_Client entity = convertToEntity(dto);
        entity.setId(generateCustomId()); // Generate custom ID

        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Store only file names
        List<String> fileNames = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = file.getOriginalFilename();
                    fileNames.add(fileName);

                    // Save file to disk
                    Path filePath = uploadDir.resolve(fileName);
                    Files.write(filePath, file.getBytes());
                }
            }
        }
        entity.setSupportingDocuments(fileNames);  // ✅ Store file names in DB

        entity = repository.save(entity); // Save client in DB
        return convertToDTO(entity);
    }



    public List<BDM_Dto> getAllClients() {
        return repository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Optional<BDM_Dto> getClientById(String id) {
        return repository.findById(id).map(this::convertToDTO);
    }

    public Optional<BDM_Dto> updateClient(String id, BDM_Dto dto, MultipartFile file) {
        return repository.findById(id).map(existingClient -> {
            BDM_Client updatedClient = convertToEntity(dto);
            updatedClient.setId(id);

            try {
                if (file != null && !file.isEmpty()) {
                    updatedClient.setDocumentedData(file.getBytes());
                } else {
                    updatedClient.setSupportingDocuments(existingClient.getSupportingDocuments());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return convertToDTO(repository.save(updatedClient));
        });
    }

    // Method to get jobs and details by client name
    public ResponseEntity<?> getJobsAndDetailsByClientName(String clientName) {
        try {
            // Fetch client details
            BDM_Client client = repository.findByClientName(clientName)
                    .orElseThrow(() -> new ClientNotFoundException("No client found for: " + clientName));
            String clientId = client.getId();

            // Fetch job details
            List<Object[]> jobs = requirementsDao.findByClientName(clientName);
            if (jobs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(404, "No jobs found for client: " + clientName));
            }

            // Convert job details into DTO
            List<JobDTO> jobDTOs = jobs.stream()
                    .map(job -> new JobDTO(
                            (String) job[0], // jobId
                            (String) job[1], // jobTitle
                            clientId
                    ))
                    .collect(Collectors.toList());

            // Maps for recruiters and candidates
            Map<String, List<RecruiterInfoDto>> recruitersMap = new HashMap<>();
            Map<String, Map<String, List<CandidateDto>>> submittedCandidatesMap = new HashMap<>();
            Map<String, Map<String, List<InterviewCandidateDto>>> interviewScheduledCandidatesMap = new HashMap<>();

            for (JobDTO job : jobDTOs) {
                String jobId = job.getJobId();

                // Fetch recruiter details
                RecruiterDetailsDTO recruiterDetailsDTO = getRecruiterDetailsByJobIdAndClientName(jobId, clientName);
                recruitersMap.put(jobId, recruiterDetailsDTO.getRecruiters());

                // Initialize candidate maps
                Map<String, List<CandidateDto>> submittedCandidates = new HashMap<>();
                Map<String, List<InterviewCandidateDto>> interviewScheduledCandidates = new HashMap<>();

                for (RecruiterInfoDto recruiter : recruiterDetailsDTO.getRecruiters()) {
                    String recruiterName = recruiter.getRecruiterName();
                    // Convert submitted candidates (Map<String, Object>) to CandidateDto
                    // Map submitted candidates for the recruiter
                    List<CandidateDto> submittedCandidatesForRecruiter = recruiterDetailsDTO.getSubmittedCandidates(recruiterName).stream()
                            .map(candidateData -> {
                                try {
                                    // Debug log to inspect candidateData
                                    System.out.println("Candidate Data: " + candidateData);

                                    // Safely get values from the CandidateDto (you should access the fields via getters, not directly from a Map)
                                    String candidateId = candidateData.getCandidateId();
                                    String candidateName = candidateData.getCandidateName();
                                    String email = candidateData.getEmail();
                                    String interviewStatus = candidateData.getInterviewStatus();  // Assuming it's a field in CandidateDto
                                    String contactNumber = candidateData.getContactNumber();  // Assuming it's a field in CandidateDto
                                    String qualification = candidateData.getQualification();  // Assuming it's a field in CandidateDto
                                    String skills = candidateData.getSkills();  // Assuming it's a field in CandidateDto
                                    String overallFeedback = candidateData.getOverallFeedback();  // Assuming it's a field in CandidateDto

                                    // Check for null values before creating CandidateDto (optional, depending on your requirements)
                                    if (candidateId == null || candidateName == null) {
                                        // Log missing data for debugging
                                        System.out.println("Missing candidate data for: " + candidateData);
                                        return null; // or throw an exception if you prefer
                                    }

                                    return new CandidateDto(
                                            candidateId,
                                            candidateName,
                                            recruiter.getRecruiterId(),
                                            email,
                                            interviewStatus,
                                            contactNumber,
                                            qualification,
                                            skills,
                                            overallFeedback
                                    );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null; // Catch and print any exceptions
                                }
                            })
                            .filter(Objects::nonNull) // Filter out null candidates if any were skipped due to missing data
                            .collect(Collectors.toList());

// Map interview scheduled candidates for the recruiter
                    List<InterviewCandidateDto> interviewScheduledCandidatesForRecruiter = recruiterDetailsDTO.getInterviewScheduledCandidates(recruiterName).stream()
                            .map(interviewData -> {
                                try {
                                    // Debug log to inspect interviewData
                                    System.out.println("Interview Data: " + interviewData);

                                    // Safely get values from the InterviewCandidateDto
                                    String candidateId = interviewData.getCandidateId();
                                    String candidateName = interviewData.getCandidateName();  // Assuming it's a field in InterviewCandidateDto
                                    String candidateEmailId = interviewData.getEmail();  // Assuming it's a field in InterviewCandidateDto
                                    String interviewStatus = interviewData.getInterviewStatus();  // Assuming it's a field in InterviewCandidateDto
                                    String interviewLevel = interviewData.getInterviewLevel();  // Assuming it's a field in InterviewCandidateDto
                                    String interviewDateTime = interviewData.getInterviewDateTime();  // Assuming it's a field in InterviewCandidateDto

                                    // Check for null values before creating InterviewCandidateDto
                                    if (candidateId == null || candidateName == null) {
                                        // Log missing data for debugging
                                        System.out.println("Missing interview data for: " + interviewData);
                                        return null; // or throw an exception if you prefer
                                    }

                                    return new InterviewCandidateDto(
                                            candidateId,
                                            candidateName,
                                            candidateEmailId,
                                            interviewStatus,
                                            interviewLevel,
                                            interviewDateTime
                                    );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null; // Catch and print any exceptions
                                }
                            })
                            .filter(Objects::nonNull) // Filter out null candidates if any were skipped due to missing data
                            .collect(Collectors.toList());

// Store the candidates in the maps
                    submittedCandidates.put(recruiterName, submittedCandidatesForRecruiter);
                    interviewScheduledCandidates.put(recruiterName, interviewScheduledCandidatesForRecruiter);
}

                submittedCandidatesMap.put(jobId, submittedCandidates);
                interviewScheduledCandidatesMap.put(jobId, interviewScheduledCandidates);
            }

            // Final response DTO
            ClientJobDetailsDTO dto = new ClientJobDetailsDTO(jobDTOs, recruitersMap, submittedCandidatesMap, interviewScheduledCandidatesMap);
            return ResponseEntity.ok(dto);

        } catch (ClientNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(404, ex.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(500, "An unexpected error occurred"));
        }
    }

    public RecruiterDetailsDTO getRecruiterDetailsByJobIdAndClientName(String jobId, String clientName) {
        Optional<RequirementsModel> requirement = requirementsDao.findRecruitersByJobId(jobId);
        if (requirement.isEmpty()) {
            throw new RequirementNotFoundException("Requirement Not Found with Id: " + jobId);
        }

        List<RecruiterInfoDto> recruiters = getRecruitersForJob(jobId);
        Map<String, List<CandidateDto>> submittedCandidates = new HashMap<>();
        Map<String, List<InterviewCandidateDto>> interviewScheduledCandidates = new HashMap<>();

        for (RecruiterInfoDto recruiter : recruiters) {
            String recruiterId = recruiter.getRecruiterId();
            String recruiterName = recruiter.getRecruiterName();

            // Fetch assigned candidates for the recruiter
            List<Tuple> assignedCandidatesList = requirementsDao.findCandidatesByJobIdAndRecruiterIdAndClientName(jobId, recruiterId);
            List<CandidateDto> assignedCandidateDtos = mapCandidates(assignedCandidatesList, recruiterName, recruiterId);
            submittedCandidates.put(recruiterName, assignedCandidateDtos);

            // Fetch interview scheduled candidates for the recruiter
            List<Tuple> interviewCandidatesList = requirementsDao.findInterviewScheduledCandidatesByJobIdAndRecruiterIdAndClientName(jobId, recruiterId, clientName);
            List<InterviewCandidateDto> interviewCandidateDtos = mapInterviewCandidates(interviewCandidatesList, recruiterName, clientName);
            interviewScheduledCandidates.put(recruiterName, interviewCandidateDtos);
        }

        return new RecruiterDetailsDTO(recruiters, submittedCandidates, interviewScheduledCandidates);
    }
    // Add client and recruiter details to candidates
    private List<CandidateWithDetailsDto> addClientAndRecruiterDetailsToCandidates(List<CandidateWithDetailsDto> candidates, String clientName, String recruiterName) {
        return candidates.stream().map(candidate -> {
            // Set clientName and recruiterName in the existing CandidateWithDetailsDto
            candidate.setClientName(clientName);  // Set the client name
            candidate.setRecruiterName(recruiterName);  // Set the recruiter name
            return candidate;  // No need to wrap again, just return the updated candidate
        }).collect(Collectors.toList());
    }

    // Add client and recruiter details to interview candidates
    private List<InterviewCandidateWithDetailsDto> addClientAndRecruiterDetailsToInterviewCandidates(List<InterviewCandidateWithDetailsDto> candidates, String clientName, String recruiterName) {
        return candidates.stream().map(candidate -> {
            // Set clientName and recruiterName in the existing InterviewCandidateWithDetailsDto
            candidate.setClientName(clientName);  // Set the client name
            candidate.setRecruiterName(recruiterName);  // Set the recruiter name
            return candidate;  // Return the updated interview candidate with details
        }).collect(Collectors.toList());
    }

    private Map<String, Object> createErrorResponse(int statusCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("statusCode", statusCode);
        errorResponse.put("message", message);
        return errorResponse;
    }




    public List<RecruiterInfoDto> getRecruitersForJob(String jobId) {
        RequirementsModel requirement = requirementsDao.findById(jobId)
                .orElseThrow(() -> new RequirementNotFoundException("Requirement Not Found with Id: " + jobId));

        Set<String> recruiterIds = requirement.getRecruiterIds().stream()
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toSet());

        List<RecruiterInfoDto> recruiters = new ArrayList<>();
        for (String recruiterId : recruiterIds) {
            Tuple userTuple = requirementsDao.findUserEmailAndUsernameByUserId(recruiterId);
            if (userTuple != null) {
                String recruiterName = userTuple.get(1, String.class);
                recruiters.add(new RecruiterInfoDto(recruiterId, recruiterName));
            }
        }
        return recruiters;
    }

    // Convert List<Tuple> of candidates into CandidateDto objects
    private List<CandidateDto> mapCandidates(List<Tuple> candidatesList, String recruiterName, String recruiterId) {
        return candidatesList.stream()
                .map(candidate -> new CandidateDto(
                        getTupleValue(candidate, "candidate_id"),
                        getTupleValue(candidate, "full_name"),
                        recruiterId,
                        getTupleValue(candidate, "email"),
                        getTupleValue(candidate, "interview_status"),
                        getTupleValue(candidate, "contact_number"),
                        getTupleValue(candidate, "qualification"),
                        getTupleValue(candidate, "skills"),
                        getTupleValue(candidate, "overall_feedback")
                ))
                .collect(Collectors.toList());
    }

    // Convert List<Tuple> of interview candidates into InterviewCandidateDto objects
    private List<InterviewCandidateDto> mapInterviewCandidates(List<Tuple> candidatesList, String recruiterName, String clientName) {
        return candidatesList.stream()
                .map(candidate -> {
                    Timestamp interviewTimestamp = candidate.get("interview_date_time", Timestamp.class);
                    String interviewDateTime = (interviewTimestamp != null) ? interviewTimestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
                    return new InterviewCandidateDto(
                            getTupleValue(candidate, "candidate_id"),
                            getTupleValue(candidate, "full_name"),
                            getTupleValue(candidate, "candidate_email_id"),
                            getTupleValue(candidate, "interview_status"),
                            getTupleValue(candidate, "interview_level"),
                            interviewDateTime
                    );
                })
                .collect(Collectors.toList());
    }


    private String getTupleValue(Tuple tuple, String columnName) {
        try {
            return tuple.get(columnName, String.class);
        } catch (Exception e) {
            return null;
        }
    }


    public void deleteClient(String id) {
        repository.deleteById(id);
    }


    public BdmClientDetailsDTO getBdmClientDetails(String userId) {
        // 1️⃣ Fetch BDM Details
        List<BdmDetailsDto> bdmDetails = getBdmDetails();

        // 2️⃣ Fetch Clients onboarded by the BDM
        List<BdmClientDto> clientDetails = getClientDetails(userId);

        // 3️⃣ Fetch Submissions for each client
        Map<String, List<BdmSubmissionDTO>> submissions = new HashMap<>();
        for (BdmClientDto client : clientDetails) {
            submissions.put(client.getClientName(), getSubmissions(client.getClientName()));
        }

        // 4️⃣ Fetch Interviews for each client
        Map<String, List<BdmInterviewDTO>> interviews = new HashMap<>();
        for (BdmClientDto client : clientDetails) {
            interviews.put(client.getClientName(), getInterviews(client.getClientName()));
        }

        // 5️⃣ Fetch Placements for each client
        Map<String, List<BdmPlacementDTO>> placements = new HashMap<>();
        for (BdmClientDto client : clientDetails) {
            placements.put(client.getClientName(), getPlacements(client.getClientName()));
        }

        // Return DTO with all details
        return new BdmClientDetailsDTO(bdmDetails, clientDetails, submissions, interviews, placements);
    }

    private List<BdmDetailsDto> getBdmDetails() {
        List<Tuple> bdmTuples = requirementsDao.findBdmEmployeesFromDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Ensure date format is correct

        return bdmTuples.stream()
                .map(tuple -> new BdmDetailsDto(
                        tuple.get("user_id", String.class),
                        tuple.get("user_name", String.class),
                        tuple.get("role_name", String.class), // Ensure alias matches query
                        tuple.get("email", String.class),
                        tuple.get("designation", String.class),
                        convertDate(tuple.get("joining_date")), // Handle both String and Date
                        tuple.get("gender", String.class),
                        convertDate(tuple.get("dob")), // Handle both String and Date
                        tuple.get("phone_number", String.class),
                        tuple.get("personalemail", String.class), // Ensure alias matches query
                        tuple.get("status", String.class),
                        tuple.get("client_name", String.class)
                ))
                .collect(Collectors.toList());
    }

    // Utility method to safely convert Object to String date
    private String convertDate(Object dateObj) {
        if (dateObj == null) return null;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (dateObj instanceof java.sql.Date || dateObj instanceof java.util.Date) {
            return dateFormat.format(dateObj);
        } else if (dateObj instanceof String) {
            try {
                return dateFormat.format(dateFormat.parse((String) dateObj)); // Convert String to Date then format
            } catch (ParseException e) {
                return (String) dateObj; // Return as-is if parsing fails
            }
        }
        return null;
    }



    private List<BdmClientDto> getClientDetails(String userId) {
        List<Tuple> clientTuples = requirementsDao.findClientsByBdmUserId(userId);

        return clientTuples.stream()
                .map(tuple -> new BdmClientDto(
                        tuple.get("id", String.class),
                        tuple.get("client_name", String.class),
                        tuple.get("on_boarded_by", String.class),
                        tuple.get("client_address", String.class),
                        cleanAndConvertToList(tuple.get("client_spoc_name", String.class)),
                        cleanAndConvertToList(tuple.get("client_spoc_emailid", String.class)),
                        cleanAndConvertToList(tuple.get("client_spoc_mobile_number", String.class))
                ))
                .collect(Collectors.toList());
    }

    // ✅ Helper Method to Clean and Convert JSON Arrays / Comma-Separated Strings to List<String>
    private List<String> cleanAndConvertToList(String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>();
        }

        // Remove extra quotes and brackets if it's a JSON array
        input = input.replace("[", "").replace("]", "").replace("\"", "");

        // Split by commas and return a clean list
        return Arrays.stream(input.split(","))
                .map(String::trim) // Trim spaces
                .filter(s -> !s.isEmpty()) // Remove empty entries
                .collect(Collectors.toList());
    }

    // Utility method to safely split strings
    private List<String> splitString(String str) {
        return (str != null && !str.isEmpty()) ? Arrays.asList(str.split(",")) : new ArrayList<>();
    }


    private List<BdmSubmissionDTO> getSubmissions(String clientName) {
        List<Tuple> submissionTuples = requirementsDao.findAllSubmissionsByClientName(clientName);
        return submissionTuples.stream()
                .map(tuple -> new BdmSubmissionDTO(
                        tuple.get("candidate_id", String.class),
                        tuple.get("full_name", String.class),
                        tuple.get("candidateEmailId", String.class), // Use exact alias from SQL
                        tuple.get("contact_number", String.class),
                        tuple.get("qualification", String.class),
                        tuple.get("skills", String.class),
                        tuple.get("overall_feedback", String.class),
                        tuple.get("job_id", String.class),
                        tuple.get("job_title", String.class),
                        tuple.get("client_name", String.class)
                ))
                .collect(Collectors.toList());
    }

    private List<BdmInterviewDTO> getInterviews(String clientName) {
        List<Tuple> interviewTuples = requirementsDao.findAllInterviewsByClientName(clientName);
        return interviewTuples.stream()
                .map(tuple -> {
                    Timestamp timestamp = tuple.get("interview_date_time", Timestamp.class);
                    String interviewDateTimeStr = (timestamp != null)
                            ? OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault())
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            : null;

                    return new BdmInterviewDTO(
                            tuple.get("candidate_id", String.class),
                            tuple.get("full_name", String.class),
                            tuple.get("candidateEmailId", String.class),
                            tuple.get("interview_status", String.class),
                            tuple.get("interview_level", String.class),
                            interviewDateTimeStr,  // ✅ Now safely converted to String
                            tuple.get("job_id", String.class),
                            tuple.get("job_title", String.class),
                            tuple.get("client_name", String.class)
                    );
                })
                .collect(Collectors.toList());
    }


    private List<BdmPlacementDTO> getPlacements(String clientName) {
        List<Tuple> placementTuples = requirementsDao.findAllPlacementsByClientName(clientName);
        return placementTuples.stream()
                .map(tuple -> new BdmPlacementDTO(
                        tuple.get("candidate_id", String.class),
                        tuple.get("full_name", String.class),
                        tuple.get("candidateEmailId", String.class), // Fix alias
                        tuple.get("job_id", String.class),
                        tuple.get("job_title", String.class),
                        tuple.get("client_name", String.class)
                ))
                .collect(Collectors.toList());
    }

    //    public ResponseEntity<ByteArrayResource> downloadSupportingDocument(String id) {
    //        Optional<BDM_Dto> clientDtoOptional = getClientById(id);
    //
    //        if (clientDtoOptional.isPresent()) {
    //            BDM_Dto clientDto = clientDtoOptional.get();
    //            byte[] documentData = clientDto.getSupportingDocuments();
    //
    //            if (documentData != null && documentData.length > 0) {
    //                ByteArrayResource resource = new ByteArrayResource(documentData);
    //                return ResponseEntity.ok()
    //                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document.pdf")
    //                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
    //                        .contentLength(documentData.length)
    //                        .body(resource);
    //            } else {
    //                return ResponseEntity.status(204).build();
    //            }
    //        } else {
    //            return ResponseEntity.status(404).build();
    //        }
    //    }


}