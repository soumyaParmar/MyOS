package com.myos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Standard DTO for all API error responses.
 *
 * This represents a unified error wrapper envelope returned to the frontend
 * whenever any exception occurs, ensuring predictable and structured error handling.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String errorCode; // High-level unique string error identifier (e.g., "AI_MODEL_NOT_FOUND")
    private String message;   // Human-readable detailed description of the error
    private String path;      // The target request endpoint
    private Map<String, String> validationErrors;

    public ErrorResponse() {
    }

    public ErrorResponse(OffsetDateTime timestamp, int status, String error, String errorCode, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(OffsetDateTime timestamp, int status, String error, String errorCode, String message, String path, Map<String, String> validationErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    // Getters and Setters
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Map<String, String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(Map<String, String> validationErrors) { this.validationErrors = validationErrors; }
}
