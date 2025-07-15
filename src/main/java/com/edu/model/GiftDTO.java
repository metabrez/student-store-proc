package com.edu.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class GiftDTO {

    @Schema(description = "Gift name", example = "Toy Car", required = true)
    @NotBlank(message = "Gift name is required")
    private String giftName;

    @Schema(description = "Category", example = "Toys", required = true)
    @NotBlank(message = "Category is required")
    private String category;

    @Schema(description = "Gift status", example = "Delivered", required = true)
    @NotBlank(message = "Gift status is required")
    private String status;

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
