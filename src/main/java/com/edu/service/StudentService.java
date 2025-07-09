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


    public Long createStudent(Student student)  {
        Connection conn = null;
        CallableStatement callableStatement = null;
        Long newStudentId = null;
        try {
            conn = dataSource.getConnection();
            callableStatement = conn.prepareCall("{call student_pkg.create_student(?,?,?,?,?,?)}");
            callableStatement.setString(1, student.getFirstName());
            callableStatement.setString(2, student.getLastName());
            callableStatement.setString(3, student.getUsername());
            callableStatement.setString(4, student.getEmail());

            String addressJson = student.getAddressJson();
            //callableStatement.setClob(5, new javax.sql.rowset.serial.SerialClob(addressJson.toCharArray()));
           // callableStatement.setBlob(5, (Blob) new SerialClob(addressJson.toCharArray()));
            callableStatement.setCharacterStream(5, new StringReader(student.getAddressJson()));

            callableStatement.registerOutParameter(6, Types.NUMERIC);
            callableStatement.execute();

            newStudentId = callableStatement.getLong(6);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return newStudentId;
    }
}