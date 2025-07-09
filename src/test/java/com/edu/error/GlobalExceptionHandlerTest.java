package com.edu.error;

import com.edu.controller.StudentController;
import com.edu.error.GlobalExceptionHandler;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StudentController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testHandleSqlException_ORA_20001() throws Exception {
        Student student = new Student();
        student.setFirstName(""); // Simulate missing first name
        student.setLastName("Test");
        student.setUsername("test.user");
        student.setEmail("test@example.com");
        student.setAddressJson("{\"city\":\"Boston\"}");

        when(studentService.createStudent(any())).thenThrow(new SQLException("ORA-20001: First name is required"));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("First name is required."));
    }

    @Test
    void testHandleSqlException_GenericOracleError() throws Exception {
        doThrow(new SQLException("ORA-22000: Some unknown Oracle error"))
                .when(studentService).deleteStudentById(999L);

        mockMvc.perform(delete("/api/students/999"))
                .andExpect(status().isInternalServerError())
               // .andExpect(content().string(containsString("Database error:")));
                .andExpect(content().string("Delete failed: ORA-22000: Some unknown Oracle error"));

    }

    @Test
    void testHandleGeneralException() throws Exception {
        when(studentService.getStudentById(999L)).thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/api/students/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error: Unexpected failure"));
    }
}
