package com.edu.controller.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

public class DbTestConfig {

    @Bean
    public CommandLineRunner testConnection(DataSource dataSource) {
        return args -> {
            System.out.println("Connected to DB: " + dataSource.getConnection().getMetaData().getURL());
        };
    }
}
