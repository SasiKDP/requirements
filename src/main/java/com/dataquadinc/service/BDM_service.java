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
import java.time.LocalDateTime;
import java.sql.Timestamp;
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
        dto.setPositionType(client.getPositionType());
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
        client.setPositionType(dto.getPositionType());
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


    public BDM_Dto createClient(BDM_Dto dto, MultipartFile file) throws IOException {
        BDM_Client entity = convertToEntity(dto);
        entity.setId(generateCustomId());

        // Check if file is present before setting it
        if (file != null && !file.isEmpty()) {
            System.out.println("Received File: " + file.getOriginalFilename());
            System.out.println("File Size: " + file.getSize());
            entity.setSupportingDocuments(file.getOriginalFilename());
            entity.setDocumentedData(file.getBytes());
        }


        entity = repository.save(entity);
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
                    List<CandidateDto> submittedCandidatesForRecruiter = recruiterDetailsDTO.getSubmittedCandidates(recruiterName);
                    List<InterviewCandidateDto> interviewScheduledCandidatesForRecruiter = recruiterDetailsDTO.getInterviewScheduledCandidates(recruiterName);

                    System.out.println("Job " + jobId + " | Recruiter: " + recruiterName + " → Submitted: " + submittedCandidatesForRecruiter.size());
                    System.out.println("Job " + jobId + " | Recruiter: " + recruiterName + " → Interview Scheduled: " + interviewScheduledCandidatesForRecruiter.size());

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

            List<Tuple> assignedCandidatesList = requirementsDao.findCandidatesByJobIdAndRecruiterIdAndClientName(jobId, recruiterId);
            List<CandidateDto> assignedCandidateDtos = mapCandidates(assignedCandidatesList, recruiterName, recruiterId);
            submittedCandidates.put(recruiterName, assignedCandidateDtos);

            List<Tuple> interviewCandidatesList = requirementsDao.findInterviewScheduledCandidatesByJobIdAndRecruiterIdAndClientName(jobId, recruiterId, clientName);
            List<InterviewCandidateDto> interviewCandidateDtos = mapInterviewCandidates(interviewCandidatesList, recruiterName, clientName);
            interviewScheduledCandidates.put(recruiterName, interviewCandidateDtos);
        }

        return new RecruiterDetailsDTO(recruiters, submittedCandidates, interviewScheduledCandidates);
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

    private List<CandidateDto> mapCandidates(List<Tuple> candidatesList, String recruiterName, String recruiterId) {
        return candidatesList.stream()
                .map(candidate -> {
                    try {
                        System.out.println("Mapping Candidate: " + getTupleValue(candidate, "candidate_id"));
                        return new CandidateDto(
                                getTupleValue(candidate, "candidate_id"),
                                getTupleValue(candidate, "full_name"),
                                recruiterId,
                                getTupleValue(candidate, "email"),
                                getTupleValue(candidate, "interview_status"),
                                getTupleValue(candidate, "contact_number"),
                                getTupleValue(candidate, "qualification"),
                                getTupleValue(candidate, "skills"),
                                getTupleValue(candidate, "overall_feedback")
                        );
                    } catch (Exception e) {
                        System.err.println("Error mapping candidate: " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<InterviewCandidateDto> mapInterviewCandidates(List<Tuple> candidatesList, String recruiterName, String clientName) {
        return candidatesList.stream()
                .map(candidate -> {
                    try {
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
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
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