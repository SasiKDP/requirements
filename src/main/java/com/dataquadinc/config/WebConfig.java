package com.dataquadinc.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")               // Allow CORS for all endpoints
                .allowedOrigins("http://192.168.0.140:3000")  // Allow only your frontend domain
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Allowed HTTP methods
                .allowedHeaders("*")                      // Allow all headers
                .allowCredentials(true);                  // Allow credentials (cookies, headers, etc.)
    }
}