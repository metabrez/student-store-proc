package com.edu.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleStudentNotFound(StudentNotFoundException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", 404);
        errorBody.put("message", "Student not found with ID: " + ex.getStudentId());
        errorBody.put("title", "Student not found exception");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleValidationFailure(IllegalArgumentException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("title", "Validation Error");
        error.put("message", ex.getMessage());
        error.put("status", 400);
        return ResponseEntity.badRequest().body(error);
    }





}
