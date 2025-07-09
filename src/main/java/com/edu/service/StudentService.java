package com.edu.service;

import com.edu.model.Student;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.*;

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
            // 💡 Handle null address safely
            if (student.getAddressJson() != null && !student.getAddressJson().trim().isEmpty()) {
                stmt.setCharacterStream(5, new StringReader(student.getAddressJson()), student.getAddressJson().length());
            } else {
                stmt.setNull(5, Types.CLOB); // Explicitly set CLOB to null
            }

            stmt.registerOutParameter(6, Types.NUMERIC);
            stmt.execute();

            return stmt.getLong(6);
        }
    }

    public Student getStudentById(Long studentId) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{call student_pkg.get_student(?, ?)}")) {

            stmt.setLong(1, studentId);
            stmt.registerOutParameter(2, OracleTypes.CURSOR); // Requires oracle.jdbc.OracleTypes

            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getLong("student_id"));
                    student.setFirstName(rs.getString("first_name"));
                    student.setLastName(rs.getString("last_name"));
                    student.setUsername(rs.getString("username"));
                    student.setEmail(rs.getString("email"));

                    Blob blob = rs.getBlob("address_blob");
                    if (blob != null) {
                        String json = new String(blob.getBytes(1, (int) blob.length()));
                        student.setAddressJson(json);
                    }

                    return student;
                } else {
                    throw new SQLException("Student not found with ID: " + studentId);
                }
            }
        }
    }

    public void updateStudent(Student student) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{call student_pkg.update_student(?,?,?,?,?,?)}")) {

            stmt.setLong(1, student.getStudentId());
            stmt.setString(2, student.getFirstName());
            stmt.setString(3, student.getLastName());
            stmt.setString(4, student.getUsername());
            stmt.setString(5, student.getEmail());
            if (student.getAddressJson() != null) {
                stmt.setCharacterStream(6, new StringReader(student.getAddressJson()));
            }else {
                stmt.setNull(6, Types.CLOB);
            }

            stmt.execute();
        }
    }

    public void deleteStudentById(Long studentId) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{call student_pkg.delete_student(?)}")) {

            stmt.setLong(1, studentId);
            stmt.execute();
        }
    }
}