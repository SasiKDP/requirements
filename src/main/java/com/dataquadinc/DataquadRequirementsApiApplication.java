package com.dataquadinc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableJpaRepositories
@EnableFeignClients  // Enable Feign Clients in your application

public class DataquadRequirementsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataquadRequirementsApiApplication.class, args);
	}

}
