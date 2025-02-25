package com.dataquadinc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RecruiterServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user.register.microservice.url}")
    private String userRegisterBaseUrl;

    public RecruiterServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Method to get recruiter's email based on recruiterId
    public String getRecruiterEmail(String recruiterId) {
        String url = userRegisterBaseUrl + "/users/" + recruiterId + "/email"; // Assuming endpoint structure

        // Send GET request and parse response
        return restTemplate.getForObject(url, String.class);  // Assuming response is just the email as a string
    }

    // Method to get recruiter's username based on recruiterId
    public String getRecruiterUsername(String recruiterId) {
        recruiterId = recruiterId.replace("\"", "");

        String url = userRegisterBaseUrl + "/users/" + recruiterId + "/username"; // Assuming username endpoint structure

        // Send GET request and parse response
        return restTemplate.getForObject(url, String.class);  // Assuming response is just the username as a string
    }
}
