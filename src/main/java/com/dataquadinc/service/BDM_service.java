package com.dataquadinc.service;

import com.dataquadinc.dto.*;
import com.dataquadinc.model.BDM_Client;
import com.dataquadinc.repository.BDM_Repo;
import com.dataquadinc.repository.RequirementsDao;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @Autowired
    private BDM_Repo repo;

    public BDM_Client saveClient(BDM_Client client) {
        client.setId(generateCustomId());
        return repository.save(client);
    }

    private String generateCustomId() {
        List<BDM_Client> clients = repository.findAll();
        int maxNumber = clients.stream()
                .map(client -> {
                    try {
                        return Integer.parseInt(client.getId().replace("CLIENT", ""));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compare)
                .orElse(0);

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
        dto.setSupportingDocuments(client.getSupportingDocuments());  // List<byte[]>
        dto.setClientSpocLinkedin(client.getClientSpocLinkedin());
        dto.setClientSpocMobileNumber(client.getClientSpocMobileNumber());
        dto.setOnBoardedBy(client.getOnBoardedBy());
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
        client.setSupportingDocuments(dto.getSupportingDocuments());  // List<byte[]>
        client.setClientSpocLinkedin(dto.getClientSpocLinkedin());
        client.setClientSpocMobileNumber(dto.getClientSpocMobileNumber());
        client.setOnBoardedBy(dto.getOnBoardedBy());
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

        entity = repo.save(entity); // Save client in DB
        return convertToDTO(entity);
    }





    public List<BDM_Dto> getAllClients() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<BDM_Dto> getClientById(String id) {
        return repository.findById(id).map(this::convertToDTO);
    }

    public Optional<BDM_Dto> updateClient(String id, BDM_Dto dto, List<MultipartFile> files) {
        return repository.findById(id).map(existingClient -> {

            // Update fields only if they are not null in dto
            if (dto.getClientName() != null) existingClient.setClientName(dto.getClientName());
            if (dto.getOnBoardedBy() != null) existingClient.setOnBoardedBy(dto.getOnBoardedBy());
            if (dto.getClientAddress() != null) existingClient.setClientAddress(dto.getClientAddress());
            if (dto.getNetPayment() != 0) existingClient.setNetPayment(dto.getNetPayment());
            if (dto.getGst() != 0.0) existingClient.setGst(dto.getGst());
            if (dto.getClientWebsiteUrl() != null) existingClient.setClientWebsiteUrl(dto.getClientWebsiteUrl());
            if (dto.getClientLinkedInUrl() != null) existingClient.setClientLinkedInUrl(dto.getClientLinkedInUrl());
            if (dto.getClientSpocName() != null) existingClient.setClientSpocName(dto.getClientSpocName());
            if (dto.getClientSpocEmailid() != null) existingClient.setClientSpocEmailid(dto.getClientSpocEmailid());
            if (dto.getClientSpocLinkedin() != null) existingClient.setClientSpocLinkedin(dto.getClientSpocLinkedin());
            if (dto.getClientSpocMobileNumber() != null) existingClient.setClientSpocMobileNumber(dto.getClientSpocMobileNumber());

            try {
                if (files != null && !files.isEmpty()) {
                    // Ensure the uploads directory exists
                    Path uploadDir = Paths.get("uploads");
                    if (!Files.exists(uploadDir)) {
                        Files.createDirectories(uploadDir);
                    }

                    List<String> fileNames = new ArrayList<>(existingClient.getSupportingDocuments());

                    for (MultipartFile file : files) {
                        if (!file.isEmpty()) {
                            String fileName = file.getOriginalFilename();
                            fileNames.add(fileName);

                            // Save the file to disk
                            Path filePath = uploadDir.resolve(fileName);
                            Files.write(filePath, file.getBytes());
                        }
                    }
                    existingClient.setSupportingDocuments(fileNames);  // ✅ Store only file names
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return convertToDTO(repository.save(existingClient));
        });
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
}