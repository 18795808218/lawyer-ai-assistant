package com.quince.lawyeraiassistant.common.exception;

import org.springframework.http.HttpStatus;

public class KnowledgeBaseException extends BusinessException {

    public KnowledgeBaseException(
            ErrorCode errorCode,
            String message) {

        super(
                errorCode,
                message,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public KnowledgeBaseException(
            ErrorCode errorCode,
            String message,
            Throwable cause) {

        super(
                errorCode,
                message,
                HttpStatus.INTERNAL_SERVER_ERROR,
                cause);
    }
}