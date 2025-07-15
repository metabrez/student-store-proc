package com.edu.service;

import com.edu.error.StudentNotFoundException;
import com.edu.model.AddressDTO;
import com.edu.model.GiftDTO;
import com.edu.model.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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


    public Long createStudent(Student student) throws SQLException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(); // JSON serializer

        String addressDTO = mapper.writeValueAsString(student.getAddressDTO());
        String giftDTO = mapper.writeValueAsString(student.getGiftDTO());

        if (giftDTO == null) {
            throw new IllegalArgumentException("giftDTO is missing in the request.");
        }
        if (addressDTO == null) {
            throw new IllegalArgumentException("addressDTO is missing in the request.");
        }

        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{call student_pkg.create_student(?,?,?,?,?,?,?,?)}")) {

            // 🧩 Basic fields
            stmt.setString(1, student.getFirstName());
            stmt.setString(2, student.getLastName());
            stmt.setString(3, student.getUsername());
            stmt.setString(4, student.getEmail());


            // 📦 Address JSON
            stmt.setCharacterStream(5, new StringReader(addressDTO), addressDTO.length());

            // 🎁 Gift JSON
            stmt.setCharacterStream(6, new StringReader(giftDTO), giftDTO.length());

            // 🕒 Gift Date
            if (student.getGiftDate() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(student.getGiftDate()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }

            stmt.registerOutParameter(8, Types.NUMERIC);
            stmt.execute();

            return stmt.getLong(8);
        }
    }

    public Student getStudentById(Long studentId) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();

        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{call student_pkg.get_student(?, ?)}")) {

            stmt.setLong(1, studentId);
            stmt.registerOutParameter(2, OracleTypes.CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
                if (!rs.next()) {
                    throw new StudentNotFoundException(studentId);
                }

                Student student = new Student();
                student.setStudentId(rs.getLong("student_id"));
                student.setFirstName(rs.getString("first_name"));
                student.setLastName(rs.getString("last_name"));
                student.setUsername(rs.getString("username"));
                student.setEmail(rs.getString("email"));

                // 📦 Decode address_blob into AddressDTO
                Blob addressBlob = rs.getBlob("address_blob");
                if (addressBlob != null && addressBlob.length() > 0) {
                    String addressJson = new String(addressBlob.getBytes(1, (int) addressBlob.length()));
                    AddressDTO addressDTO = mapper.readValue(addressJson, AddressDTO.class);
                    student.setAddressDTO(addressDTO);
                }

                // 🎁 Decode gift_blob into GiftDTO
                Blob giftBlob = rs.getBlob("gift_blob");
                if (giftBlob != null && giftBlob.length() > 0) {
                    String giftJson = new String(giftBlob.getBytes(1, (int) giftBlob.length()));
                    GiftDTO giftDTO = mapper.readValue(giftJson, GiftDTO.class);
                    student.setGiftDTO(giftDTO);
                }

                // 🕒 Convert gift_date safely
                Timestamp giftTs = rs.getTimestamp("gift_date");
                if (giftTs != null) {
                    student.setGiftDate(giftTs.toLocalDateTime());
                }

                return student;
            }
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to deserialize BLOB JSON: " + e.getMessage(), e);
        }
    }

        public void updateStudent (Student student) throws SQLException {
            ObjectMapper mapper = new ObjectMapper();

            try (Connection conn = dataSource.getConnection();
                 CallableStatement stmt = conn.prepareCall("{call student_pkg.update_student(?,?,?,?,?,?,?,?)}")) {

                // 🎯 Basic fields
                stmt.setLong(1, student.getStudentId());
                stmt.setString(2, student.getFirstName());
                stmt.setString(3, student.getLastName());
                stmt.setString(4, student.getUsername());
                stmt.setString(5, student.getEmail());

                // 🧩 Serialize AddressDTO
                String addressJson = mapper.writeValueAsString(student.getAddressDTO());
                stmt.setCharacterStream(6, new StringReader(addressJson), addressJson.length());

                // 🧸 Serialize GiftDTO
                String giftJson = mapper.writeValueAsString(student.getGiftDTO());
                stmt.setCharacterStream(7, new StringReader(giftJson), giftJson.length());

                // 🕒 Gift Date
                if (student.getGiftDate() != null) {
                    stmt.setTimestamp(8, Timestamp.valueOf(student.getGiftDate()));
                } else {
                    stmt.setNull(8, Types.TIMESTAMP);
                }

                stmt.execute();

            } catch (JsonProcessingException e) {
                throw new SQLException("Failed to serialize JSON fields: " + e.getMessage(), e);
            }
        }

        public void deleteStudentById (Long studentId) throws SQLException {
            try (Connection conn = dataSource.getConnection();
                 CallableStatement stmt = conn.prepareCall("{call student_pkg.delete_student(?)}")) {

                stmt.setLong(1, studentId);
                stmt.execute();
            }
        }

    }