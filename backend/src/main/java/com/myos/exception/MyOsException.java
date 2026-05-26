package com.myos.exception;

/**
 * The base custom runtime exception class for the MyOS system.
 *
 * Why a custom RuntimeException?
 * In Spring Boot, transactional boundaries (@Transactional) automatically roll back
 * changes only when a RuntimeException (unchecked exception) is thrown.
 * If we used a checked Exception, we would have to manually configure rollback rules.
 *
 * By carrying an ErrorCode, this exception automatically bundles a structured error identifier
 * and HTTP status code, allowing the GlobalExceptionHandler to parse and serialize it predictably.
 */
public class MyOsException extends RuntimeException {

    private final ErrorCode errorCode;

    public MyOsException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public MyOsException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
