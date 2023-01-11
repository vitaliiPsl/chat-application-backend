package com.example.chat.controller;

import com.example.chat.exception.ResourceAlreadyExistException;
import com.example.chat.exception.ResourceNotFoundException;
import com.example.chat.payload.error.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class ErrorHandlerController {
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> handleServerError(Throwable e) {
        log.error("handle server error: {}", e.getMessage(), e);

        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Server error");
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("handle http message not readable: {}", e.getMessage(), e);

        String error = "Malformed JSON request";
        return buildResponseEntity(new ApiError(BAD_REQUEST, error, e));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(BadCredentialsException e) {
        log.error("handle authentication exception: {}", e.getMessage(), e);

        String error = "Invalid username or password";
        return buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, error));
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ApiError> handleMethodArgumentNotValid(BindException e) {
        log.error("handle bind exception: {}", e.getMessage(), e);

        Optional<FieldError> fieldError = e.getFieldErrors().stream().findFirst();
        String message = fieldError.isEmpty() ? "Invalid input" : fieldError.get().getDefaultMessage();

        ApiError apiError = new ApiError(BAD_REQUEST, message);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<ApiError> handleEntityNotFoundException(ResourceNotFoundException e) {
        log.error("handle resource not found: {}", e.getMessage(), e);

        return buildResponseEntity(new ApiError(NOT_FOUND, e.getMessage(), e));
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    protected ResponseEntity<ApiError> handleResourceAlreadyExistException(ResourceAlreadyExistException e) {
        log.error("handle resource already exist: {}", e.getMessage(), e);

        return buildResponseEntity(new ApiError(NOT_FOUND, e.getMessage(), e));
    }

    private ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
