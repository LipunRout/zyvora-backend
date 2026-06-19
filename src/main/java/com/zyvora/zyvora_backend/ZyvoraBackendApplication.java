package com.zyvora.zyvora_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.zyvora")
@EnableJpaRepositories(basePackages = "com.zyvora.repository")
@EntityScan(basePackages = "com.zyvora.entity")
public class ZyvoraBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(ZyvoraBackendApplication.class, args);
	}
}