package com.dataquadinc.service;



import com.dataquadinc.dto.BDM_Dto;
import com.dataquadinc.model.BDM_Client;
import com.dataquadinc.repository.BDM_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BDM_service {

    @Autowired
    private BDM_Repo repository;

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