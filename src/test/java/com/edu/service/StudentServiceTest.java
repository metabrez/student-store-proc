package com.edu.service;

import com.edu.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private CallableStatement callableStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private StudentService studentService;

    private Student sampleStudent;

    @BeforeEach
    void setup() throws Exception {
        sampleStudent = new Student();
        sampleStudent.setStudentId(1L);
        sampleStudent.setFirstName("Kabir");
        sampleStudent.setLastName("Gautam");
        sampleStudent.setUsername("kabir.g");
        sampleStudent.setEmail("kabir@example.com");
        sampleStudent.setAddressJson("{\"Address\":{\"city\":\"providence\",\"state\":\"RI\",\"zipcode\":\"02908\"}}");

        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void testCreateStudent() throws Exception {
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getLong(6)).thenReturn(1L);

        Long id = studentService.createStudent(sampleStudent);
        assertEquals(1L, id);
        verify(callableStatement).execute();
    }

    @Test
    void testUpdateStudent() throws Exception {
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        studentService.updateStudent(sampleStudent);
        verify(callableStatement).execute();
    }

    @Test
    void testDeleteStudentById() throws Exception {
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        studentService.deleteStudentById(1L);
        verify(callableStatement).execute();
    }

    @Test
    void testGetStudentById() throws Exception {
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getObject(2)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("student_id")).thenReturn(1L);
        when(resultSet.getString("first_name")).thenReturn("Kabir");
        when(resultSet.getString("last_name")).thenReturn("Gautam");
        when(resultSet.getString("username")).thenReturn("kabir.g");
        when(resultSet.getString("email")).thenReturn("kabir@example.com");

        Blob blob = new javax.sql.rowset.serial.SerialBlob(sampleStudent.getAddressJson().getBytes());
        when(resultSet.getBlob("address_blob")).thenReturn(blob);

        Student result = studentService.getStudentById(1L);
        assertEquals("Kabir", result.getFirstName());
    }
    @Test
    void testGetStudentById_NotFound() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getObject(2)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No record found

        SQLException thrown = assertThrows(SQLException.class, () -> {
            studentService.getStudentById(99L); // Simulate missing ID
        });

        assertTrue(thrown.getMessage().contains("Student not found with ID: 99"));
    }

}
