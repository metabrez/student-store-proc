package com.edu.service;

import com.edu.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

@Service
public class StudentService {

    @Autowired
    private DataSource dataSource;

    public StudentService(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public Long createStudent(Student student) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{call student_pkg.create_student(?,?,?,?,?,?)}")) {

            stmt.setString(1, student.getFirstName());
            stmt.setString(2, student.getLastName());
            stmt.setString(3, student.getUsername());
            stmt.setString(4, student.getEmail());
            stmt.setCharacterStream(5, new StringReader(student.getAddressJson()));
            stmt.registerOutParameter(6, Types.NUMERIC);

            stmt.execute();
            return stmt.getLong(6);
        }
    }

}