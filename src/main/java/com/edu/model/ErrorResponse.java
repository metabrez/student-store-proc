package com.edu.model;

public class ErrorResponse {
    private int status;
    private String message;
    private String title;

    public ErrorResponse(int status, String message, String title) {
        this.status = status;
        this.message = message;
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
