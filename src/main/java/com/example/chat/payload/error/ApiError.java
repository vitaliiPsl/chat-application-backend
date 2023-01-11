package com.example.chat.payload.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String message;

    private HttpStatus status;

    private String debugMessage;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiError(HttpStatus status, String message, Throwable throwable) {
        this.status = status;
        this.message = message;
        this.debugMessage = throwable.getMessage();
    }
}
