package com.edu;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
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

	public static void main(String[] args) {
		SpringApplication.run(StudentRestApplication.class, args);
	}



}



