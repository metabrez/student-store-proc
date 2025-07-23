package com.edu.controller;

import com.edu.model.ErrorResponse;
import com.edu.model.GiftDTO;
import com.edu.model.Student;
import com.edu.model.SuccessResponse;
import com.edu.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@Tag(name = "Student", description = "Operations related to student management")
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;


    @Operation(
            summary = "Create a new student",
            description = "Adds a student record with validated fields and nested gift/address JSON."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Student created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Successful Creation",
                                            summary = "Valid request payload for creating a student",
                                            value = "{ \"firstName\": \"Kabir\", \"lastName\": \"Gautam\", \"username\": \"kabir.g\", \"email\": \"kabir@example.com\", \"addressDTO\": { \"street\": \"Park Lane\", \"city\": \"Chicago\", \"pin\": \"60601\" }, \"giftDTO\": { \"giftName\": \"Toy Car\", \"category\": \"Toys\", \"status\": \"Delivered\" }, \"giftDate\": \"2025-07-14 09:15:00\" }"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Missing First Name",
                                            summary = "firstName is required",
                                            value = "{ \"firstName\": \"\", \"lastName\": \"Gautam\", \"username\": \"kabir.g\", \"email\": \"kabir@example.com\", \"addressDTO\": { \"street\": \"Main St\", \"city\": \"Chicago\", \"pin\": \"60601\" }, \"giftDTO\": { \"giftName\": \"Toy\", \"category\": \"Games\", \"status\": \"Delivered\" }, \"giftDate\": \"2025-07-01 10:00:00\" }"
                                    ),
                                    @ExampleObject(
                                            name = "Missing Email",
                                            summary = "Email must be a valid format",
                                            value = "{ \"firstName\": \"Kabir\", \"lastName\": \"Gautam\", \"username\": \"kabir.g\", \"email\": \"invalid-email\", \"addressDTO\": { \"street\": \"Main St\", \"city\": \"Chicago\", \"pin\": \"60601\" }, \"giftDTO\": { \"giftName\": \"Toy\", \"category\": \"Games\", \"status\": \"Delivered\" }, \"giftDate\": \"2025-07-01 10:00:00\" }"
                                    ),
                                    @ExampleObject(
                                            name = "Missing Street",
                                            summary = "Street in addressDTO is required",
                                            value = "{ \"firstName\": \"Kabir\", \"lastName\": \"Gautam\", \"username\": \"kabir.g\", \"email\": \"kabir@example.com\", \"addressDTO\": { \"street\": \"\", \"city\": \"Chicago\", \"pin\": \"60601\" }, \"giftDTO\": { \"giftName\": \"Toy\", \"category\": \"Games\", \"status\": \"Delivered\" }, \"giftDate\": \"2025-07-01 10:00:00\" }"
                                    ),
                                    @ExampleObject(
                                            name = "Missing Gift Status",
                                            summary = "Status in giftDTO is required",
                                            value = "{ \"firstName\": \"Kabir\", \"lastName\": \"Gautam\", \"username\": \"kabir.g\", \"email\": \"kabir@example.com\", \"addressDTO\": { \"street\": \"Main St\", \"city\": \"Chicago\", \"pin\": \"60601\" }, \"giftDTO\": { \"giftName\": \"Toy\", \"category\": \"Games\", \"status\": \"\" }, \"giftDate\": \"2025-07-01 10:00:00\" }"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Server Error",
                                            summary = "Occurs when something fails internally, like DB connection or JSON parsing",
                                            value = "{ \"status\": 500, \"message\": \"Failed to serialize JSON fields: No serializer found for class\", \"title\": \"Unexpected Error\" }"
                                    )
                            }
                    )
            )

    })
    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody @Valid Student student) {
        try {
            GiftDTO giftDTO = student.getGiftDTO();

            if (giftDTO == null) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse(400, "giftDTO is missing in the request.", "Validation Error")
                );
            }

            String status = giftDTO.getStatus();

            if ("Delivered".equalsIgnoreCase(status) && student.getGiftDate() == null) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse(400, "giftDate is missing for status 'Delivered'", "Validation Error")
                );
            }

            if (!"Delivered".equalsIgnoreCase(status) && student.getGiftDate() != null) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse(400, "giftDate should not be passed when status is not 'Delivered'", "Validation Error")
                );
            }

            Long id = studentService.createStudent(student);
            return ResponseEntity.ok(new SuccessResponse(200, "Student created with Id: " + id));

        } catch (SQLException e) {
            return handleSqlError(e);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new ErrorResponse(500, e.getMessage(), "unexpected error")
            );
        }
    }
    @Operation(
            summary = "Update an existing student",
            description = "Updates a student’s personal, address, and gift details by ID. Field-level validation applies."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Student updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Successful Update",
                                            summary = "All fields are valid",
                                            value = "{ \"status\": 200, \"message\": \"Student updated successfully\" }"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Missing Last Name",
                                            summary = "lastName is empty",
                                            value = "{ \"status\": 400, \"message\": \"Last name is required\", \"title\": \"Validation Error\" }"
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Gift Status",
                                            summary = "Gift status is required",
                                            value = "{ \"status\": 400, \"message\": \"Gift status is required\", \"title\": \"Validation Error\" }"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Server Crash",
                                            summary = "Database or deserialization error",
                                            value = "{ \"status\": 500, \"message\": \"Unable to connect to database\", \"title\": \"Unexpected Error\" }"
                                    )
                            }
                    )
            )
    })

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody @Valid Student student) {
        try {
            student.setStudentId(id);
            studentService.updateStudent(student);
            return ResponseEntity.ok(new SuccessResponse(200, "Student updated successfully"));
        } catch (SQLException e) {
            return handleSqlError(e);
        }
    }


    @Operation(
            summary = "Retrieve student by ID",
            description = "Fetches full student details including address and gift information."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Student retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Valid Student",
                                            summary = "Student exists",
                                            value = "{ \"studentId\": 101, \"firstName\": \"Kabir\", \"lastName\": \"Gautam\", \"username\": \"kabir.g\", \"email\": \"kabir@example.com\", \"addressDTO\": { \"street\": \"Main St\", \"city\": \"Chicago\", \"pin\": \"60601\" }, \"giftDTO\": { \"giftName\": \"Toy Car\", \"category\": \"Toys\", \"status\": \"Delivered\" }, \"giftDate\": \"2025-07-01 10:00:00\" }"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Missing ID",
                                            summary = "No ID provided",
                                            value = "{ \"status\": 400, \"message\": \"Student ID is required\", \"title\": \"Validation Error\" }"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Student not found or server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Student Not Found",
                                            summary = "ID does not exist",
                                            value = "{ \"status\": 500, \"message\": \"Student not found\", \"title\": \"Unexpected Error\" }"
                                    )
                            }
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id) {
        try {
            Student student = studentService.getStudentById(id);
            return ResponseEntity.ok(student);
        } catch (SQLException e) {
            return handleSqlError(e);
        }
    }

    @Operation(
            summary = "Delete student by ID",
            description = "Removes a student record permanently."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Student deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Successful Deletion",
                                            summary = "Student removed",
                                            value = "{ \"status\": 200, \"message\": \"Student deleted successfully\" }"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing or invalid ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Missing ID",
                                            summary = "ID not provided",
                                            value = "{ \"status\": 400, \"message\": \"Student ID is required for deletion\", \"title\": \"Validation Error\" }"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Student not found or internal error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Student Not Found",
                                            summary = "ID does not match any record",
                                            value = "{ \"status\": 500, \"message\": \"Student not found\", \"title\": \"Unexpected Error\" }"
                                    )
                            }
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudentById(id);
            return ResponseEntity.ok(new SuccessResponse(200, "Student deleted successfully"));
        } catch (SQLException e) {
            return handleSqlError(e);
        }
    }

    private ResponseEntity<ErrorResponse> badRequest(String msg) {
        return ResponseEntity.badRequest().body(new ErrorResponse(400, msg, "field validation error"));
    }

    private ResponseEntity<ErrorResponse> handleSqlError(SQLException e) {
        String m = e.getMessage();
        if (m.contains("ORA-20001")) return badRequest("First name is required.");
        if (m.contains("ORA-20002")) return badRequest("Last name is required.");
        if (m.contains("ORA-20003")) return badRequest("Username is required.");
        if (m.contains("ORA-20004")) return badRequest("Email is required.");
        if (m.contains("ORA-20005")) return badRequest("Email format is invalid.");
        if (m.contains("ORA-20006")) return badRequest("giftDate should not be passed when status is not Delivered.");
        if (m.contains("ORA-20010")) return badRequest("Student ID is required for update.");
        if (m.contains("ORA-20031")) return badRequest("Student not found.");
        return ResponseEntity.status(500).body(new ErrorResponse(500, m, "unexpected error"));
    }

    public boolean checkValidDeliveryStatus(Student student) {
        if(student!=null){
            if(student.getGiftDTO()==null){
                throw new IllegalArgumentException("Gift DTO is required.");
            }
            return student.getGiftDTO().toString().equals("Delivered");
        }
        return false;
    }

}