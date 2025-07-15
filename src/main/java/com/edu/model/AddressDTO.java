package com.edu.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class AddressDTO {
    @Schema(description = "Street name", example = "Park Lane", required = true)
    @NotBlank(message = "Street is required")
    private String street;

    @Schema(description = "City", example = "Chicago", required = true)
    @NotBlank(message = "City is required")
    private String city;

    @Schema(description = "Pin code", example = "60601", required = true)
    @NotBlank(message = "Pin code is required")
    private String pin;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
