package com.quince.lawyeraiassistant.common.exception;

import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;

    protected BusinessException(
            ErrorCode errorCode,
            String message,
            HttpStatus status) {

        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    protected BusinessException(
            ErrorCode errorCode,
            String message,
            HttpStatus status,
            Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.name();
    }

    public HttpStatus getStatus() {
        return status;
    }
}