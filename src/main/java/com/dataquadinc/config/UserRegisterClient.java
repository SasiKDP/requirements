package com.dataquadinc.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-register-service", url = "${user.register.microservice.url}") // Use the property here
public interface UserRegisterClient {

    // Remove the dynamic part from the URL here, and use @PathVariable in the method
    @GetMapping("/{userId}/email")  // This is the static part of the URL
    String getRecruiterEmail(@PathVariable("userId") String userId);

    // Method to get recruiter's username
    @GetMapping("/{userId}/username")
    String getRecruiterUsername(@PathVariable("userId") String userId);

}
