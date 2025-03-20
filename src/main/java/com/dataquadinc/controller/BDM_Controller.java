package com.dataquadinc.controller;

import com.dataquadinc.dto.BDM_Dto;
import com.dataquadinc.dto.ResponseBean;
import com.dataquadinc.service.BDM_service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = {
        "http://35.188.150.92",
        "http://192.168.0.140:3000",
        "http://192.168.0.139:3000",
        "https://mymulya.com",
        "http://localhost:3000",
        "http://192.168.0.135:8080"
})
@RestController
@RequestMapping("/requirements")
public class BDM_Controller {

    @Autowired
    private BDM_service service;

    private final ObjectMapper objectMapper = new ObjectMapper(); // Reuse ObjectMapper

    @PostMapping("/bdm/addClient")
    public ResponseEntity<ResponseBean> createClient(
            @RequestPart("dto") String dtoJson,
            @RequestPart(value = "supportingDocuments", required = false) MultipartFile file) {

        try {
            BDM_Dto dto = objectMapper.readValue(dtoJson, BDM_Dto.class);

            if (file != null && !file.isEmpty()) {
                dto.setSupportingDocuments(file.getOriginalFilename());
            }

            BDM_Dto createdClient = service.createClient(dto, file);
            return ResponseEntity.ok(ResponseBean.successResponse("Client added successfully", createdClient));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBean.errorResponse("Invalid JSON", "Error parsing client data"));
        }
    }

    @GetMapping("/bdm/getAll")
    public ResponseEntity<ResponseBean> getAllClients() {
        List<BDM_Dto> clients = service.getAllClients();
        return ResponseEntity.ok(ResponseBean.successResponse("Clients fetched successfully", clients));
    }

    @GetMapping("/bdm/{id}")
    public ResponseEntity<ResponseBean> getClientById(@PathVariable String id) {
        return service.getClientById(id)
                .map(client -> ResponseEntity.ok(ResponseBean.successResponse("Client found", client)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseBean.errorResponse("Client not found", "No client exists with ID: " + id)));
    }

    @PutMapping("/bdm/{id}")
    public ResponseEntity<ResponseBean> updateClient(
            @PathVariable String id,
            @RequestPart("dto") String dtoJson,
            @RequestPart(value = "supportingDocuments", required = false) MultipartFile file) {

        try {
            BDM_Dto dto = objectMapper.readValue(dtoJson, BDM_Dto.class);

            Optional<BDM_Dto> updatedClient = service.updateClient(id, dto, file);
            return updatedClient.map(client -> ResponseEntity.ok(ResponseBean.successResponse("Client updated successfully", client)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ResponseBean.errorResponse("Update failed", "No client exists with ID: " + id)));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBean.errorResponse("Invalid JSON", "Error parsing client data"));
        }
    }

    @DeleteMapping("/bdm/{id}")
    public ResponseEntity<ResponseBean> deleteClient(@PathVariable String id) {
        service.deleteClient(id);
        return ResponseEntity.ok(ResponseBean.successResponse("Client deleted successfully", null));
    }

    @GetMapping("/bdm/{id}/download")
    public ResponseEntity<?> downloadSupportingDocument(@PathVariable String id) {
        Optional<BDM_Dto> clientDtoOptional = service.getClientById(id);

        if (clientDtoOptional.isPresent()) {
            BDM_Dto clientDto = clientDtoOptional.get();
            byte[] documentData = clientDto.getDocumentData();
            String fileName = clientDto.getSupportingDocuments();

            if (documentData != null && documentData.length > 0) {
                ByteArrayResource resource = new ByteArrayResource(documentData);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .contentLength(documentData.length)
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(ResponseBean.errorResponse("No document found", "No document data available for client ID: " + id));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseBean.errorResponse("Client not found", "No client exists with ID: " + id));
        }
    }
    @GetMapping(value = "/bdm/jobs/{clientName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getJobsAndDetailsByClientName(@PathVariable String clientName) {
        try {
            // ✅ Log the request
            System.out.println("Fetching jobs for client: " + clientName);

            // ✅ Directly return the ResponseEntity from service
            ResponseEntity<?> response = service.getJobsAndDetailsByClientName(clientName);

            // ✅ Log the response
            System.out.println("Response: " + response.getStatusCode());

            return response;
        } catch (RuntimeException ex) {
            System.err.println("Client Not Found: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(404, ex.getMessage()));
        } catch (Exception ex) {
            System.err.println("Unexpected Error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(500, "An unexpected error occurred"));
        }
    }


    private Map<String, Object> createErrorResponse(int statusCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("statusCode", statusCode);
        return errorResponse;
    }

}