package com.edu.controller;


import com.edu.controller.config.DbTestConfig;
import com.edu.model.Student;
import com.edu.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StudentController.class, excludeAutoConfiguration = DbTestConfig.class)

//@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    private Student sampleStudent;

    @BeforeEach
    void setup() {
        sampleStudent = new Student();
        sampleStudent.setStudentId(1L);
        sampleStudent.setFirstName("Kabir");
        sampleStudent.setLastName("Gautam");
        sampleStudent.setUsername("kabir.g");
        sampleStudent.setEmail("kabir@example.com");
        //sampleStudent.setAddressJson("{\"Address\":{\"city\":\"providence\",\"state\":\"RI\",\"zipcode\":\"02908\"}}");
    }

    @Test
    void testCreateStudent() throws Exception {
        when(studentService.createStudent(any())).thenReturn(1L);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sampleStudent)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Created student with ID:")));
    }

    @Test
    void testUpdateStudent() throws Exception {
        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sampleStudent)))
                .andExpect(status().isOk())
                .andExpect(content().string("Student updated successfully."));
    }

    @Test
    void testGetStudent() throws Exception {
        when(studentService.getStudentById(1L)).thenReturn(sampleStudent);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Kabir"));
    }

    @Test
    void testDeleteStudent() throws Exception {
        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Student deleted successfully."));
    }

    @Test
    void testGetStudent_NotFound() throws Exception {
        when(studentService.getStudentById(1L)).thenThrow(new SQLException("Student not found"));

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Student not found")));
    }

    @Test
    void testUpdateStudent_BadRequest() throws Exception {
        doThrow(new SQLException("Invalid update")).when(studentService).updateStudent(any(Student.class));

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(sampleStudent)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Update failed")));
    }

    @Test
    void testDeleteStudent_InternalError() throws Exception {
        doThrow(new SQLException("Delete failed")).when(studentService).deleteStudentById(1L);

        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Delete failed")));
    }

}
