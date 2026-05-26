package com.myos.exception;

import org.springframework.http.HttpStatus;

/**
 * An enum representing type-safe error codes mapped to their corresponding HTTP status.
 * This is used globally in MyOS to ensure that exceptions carry meaningful, high-level
 * error codes (like "AI_MODEL_NOT_FOUND") that the frontend can read, translate, and handle.
 */
public enum ErrorCode {

    // Configuration / Persistence Error Codes
    MODEL_CONFIG_NOT_FOUND("AI_MODEL_NOT_FOUND", HttpStatus.NOT_FOUND),
    INVALID_API_KEY("INVALID_API_KEY", HttpStatus.BAD_REQUEST),
    ACTIVE_MODEL_REQUIRED("ACTIVE_MODEL_REQUIRED", HttpStatus.BAD_REQUEST),
    DUPLICATE_ACTIVE_MODEL("DUPLICATE_ACTIVE_MODEL", HttpStatus.CONFLICT),
    DECRYPTION_ERROR("DECRYPTION_ERROR", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // Core Request / Auth Error Codes
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN),
    
    // Catch-All
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
