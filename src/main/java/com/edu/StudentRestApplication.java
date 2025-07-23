package com.edu;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@OpenAPIDefinition(
		info = @Info(
				title = "Student API",
				version = "1.0",
				description = "API for managing students and their gifts"
		)
)
@SpringBootApplication
public class StudentRestApplication {
private static final Logger log = LoggerFactory.getLogger(StudentRestApplication.class);
	public static void main(String[] args) {

		SpringApplication.run(StudentRestApplication.class, args);

		log.info("Spring Boot Application Started");

	}



}



