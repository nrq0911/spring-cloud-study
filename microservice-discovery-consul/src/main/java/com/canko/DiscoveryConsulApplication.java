package com.canko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties
public class DiscoveryConsulApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryConsulApplication.class, args);
	}
}
