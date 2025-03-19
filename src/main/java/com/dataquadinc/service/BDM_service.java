package com.dataquadinc.service;

import com.dataquadinc.dto.BDM_Dto;
import com.dataquadinc.model.BDM_Client;
import com.dataquadinc.repository.BDM_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BDM_service {

    @Autowired
    private BDM_Repo repository;

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
}
