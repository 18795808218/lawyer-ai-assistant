package com.quince.lawyeraiassistant.exception;

import com.quince.lawyeraiassistant.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SensitiveWordException.class)
    public ResponseEntity<ErrorResponse> handleSensitiveWordException(
            SensitiveWordException exception) {

        log.warn(
                "AI request rejected by content guardrail, matchedWord={}",
                exception.getMatchedWord());

        ErrorResponse response = ErrorResponse.of(
                "AI_CONTENT_REJECTED",
                exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}