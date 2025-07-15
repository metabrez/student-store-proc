package com.edu.error;

public class StudentNotFoundException extends RuntimeException{


        private final Long studentId;

        public StudentNotFoundException(Long studentId) {
            super("Student not found with ID: " + studentId);
            this.studentId = studentId;
        }

        public Long getStudentId() {
            return studentId;
        }
    }

