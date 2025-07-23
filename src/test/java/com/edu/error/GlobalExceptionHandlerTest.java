package com.edu.error;

import com.edu.controller.StudentController;
import com.edu.model.Student;
import com.edu.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Student sampleStudent() {
        Student student = new Student();
        student.setFirstName("Kabir");
        student.setLastName("Gautam");
        student.setUsername("kabir.g");
        student.setEmail("kabir@example.com");
        //student.setAddressJson("{\"city\":\"Boston\"}");
        return student;
    }

    @Test
    void testORA20001_FirstNameMissing() throws Exception {
        when(studentService.createStudent(any())).thenThrow(new SQLException("ORA-20001: First name is required"));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleStudent())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("First name is required."));
    }

    @Test
    void testORA20002_LastNameMissing() throws Exception {
        when(studentService.createStudent(any())).thenThrow(new SQLException("ORA-20002: Last name is required"));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleStudent())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Last name is required."));
    }

    @Test
    void testORA20003_UsernameMissing() throws Exception {
        when(studentService.createStudent(any())).thenThrow(new SQLException("ORA-20003: Username is required"));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleStudent())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is required."));
    }

    @Test
    void testORA20004_EmailMissing() throws Exception {
        when(studentService.createStudent(any())).thenThrow(new SQLException("ORA-20004: Email is required"));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleStudent())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is required."));
    }

    @Test
    void testORA20010_StudentIdMissingForUpdate() throws Exception {
        doThrow(new SQLException("ORA-20010: Student ID is required for update"))
                .when(studentService).updateStudent(any());

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleStudent())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Student ID is required for update."));
    }

    @Test
    void testGenericSQLExceptionFallback() throws Exception {
        when(studentService.getStudentById(999L))
                .thenThrow(new SQLException("ORA-99999: Unknown error"));

        mockMvc.perform(get("/api/students/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Database error: ORA-99999: Unknown error"));
    }

    @Test
    void testGenericExceptionFallback() throws Exception {
        when(studentService.getStudentById(999L))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/api/students/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error: Unexpected failure"));
    }
}
