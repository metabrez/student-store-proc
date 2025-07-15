package com.edu.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class Student {
    @Schema(description = "First name", example = "Kabir", required = true)
    @NotBlank(message = "First name is required")
    private String firstName;

    @Schema(description = "Last name", example = "Gautam", required = true)
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Schema(description = "Username", example = "kabir.g", required = true)
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Email address", example = "kabir@example.com", required = true)
    @Email(message = "Email format is invalid")
    private String email;

    @Valid
    private AddressDTO addressDTO;

    @Valid
    private GiftDTO giftDTO;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime giftDate;

    private Long studentId;

    public Student() {}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDateTime getGiftDate() {
        return giftDate;
    }

    public void setGiftDate(LocalDateTime giftDate) {
        this.giftDate = giftDate;
    }

    public GiftDTO getGiftDTO() {
        return giftDTO;
    }

    public void setGiftDto(GiftDTO giftDTO) {
        this.giftDTO = giftDTO;
    }

    public AddressDTO getAddressDTO() {
        return addressDTO;
    }

    public void setAddressDTO(AddressDTO addressDTO) {
        this.addressDTO = addressDTO;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setGiftDTO(GiftDTO giftDTO) {
        this.giftDTO = giftDTO;
    }

    @Override
    public String toString() {
        return "Student{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", addressDTO=" + addressDTO +
                ", giftDto=" + giftDTO +
                ", giftDate=" + giftDate +
                ", studentId=" + studentId +
                '}';
    }
}
