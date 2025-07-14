package com.edu.controller;

import com.edu.model.Student;
import com.edu.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@Tag(name = "Student", description = "Operations related to student management")
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Operation(summary = "Create student", description = "Adds a student with optional gift info")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields"),
            @ApiResponse(responseCode = "500", description = "Server error during creation")
    })
    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody Student student) throws Exception {
        Long id = studentService.createStudent(student);
        return ResponseEntity.ok("Created student with ID: " + id);
    }

    @Operation(summary = "Get student by ID", description = "Retrieves student info using ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student found"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id) {
        try {
            Student student = studentService.getStudentById(id);
            return ResponseEntity.ok(student);
        } catch (SQLException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student not found: " + ex.getMessage());
        }
    }

    @Operation(summary = "Update student", description = "Updates a student by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — incorrect inputs"),
            @ApiResponse(responseCode = "404", description = "Student ID not found"),
            @ApiResponse(responseCode = "500", description = "Server error during update")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        try {
            student.setStudentId(id);
            studentService.updateStudent(student);
            return ResponseEntity.ok("Student updated successfully.");
        } catch (SQLException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Update failed: " + ex.getMessage());
        }
    }


    @Operation(
            summary = "Delete student by ID",
            description = "Removes a student record from the system using their student ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error during deletion")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudentById(id);
            return ResponseEntity.ok("Student deleted successfully.");
        } catch (SQLException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Delete failed: " + ex.getMessage());
        }
    }
}