package com.edu.controller;

import com.edu.model.ErrorResponse;
import com.edu.model.GiftDTO;
import com.edu.model.Student;
import com.edu.model.SuccessResponse;
import com.edu.service.StudentService;
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id) {
        try {
            Student student = studentService.getStudentById(id);
            return ResponseEntity.ok(student);
        } catch (SQLException e) {
            return handleSqlError(e);
        }
    }

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

}