package com.edu.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<String> handleSqlException(SQLException ex) {
        // Customize for Oracle stored proc errors
        if (ex.getMessage().contains("ORA-20001")) {
            return ResponseEntity.badRequest().body("First name is required.");
        } else if (ex.getMessage().contains("ORA-20002")) {
            return ResponseEntity.badRequest().body("Last name is required.");
        } else if (ex.getMessage().contains("ORA-20003")) {
            return ResponseEntity.badRequest().body("Username is required.");
        } else if (ex.getMessage().contains("ORA-20004")) {
            return ResponseEntity.badRequest().body("Email is required.");
        } else if (ex.getMessage().contains("ORA-20010")) {
            return ResponseEntity.badRequest().body("Student ID is required for update.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database error: " + ex.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + ex.getMessage());
    }
}
