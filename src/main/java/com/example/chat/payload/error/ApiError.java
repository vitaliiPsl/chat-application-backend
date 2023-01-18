package com.example.chat.payload.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Error mesage", example = "Invalid argument")
    private String message;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Error status")
    private HttpStatus status;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Debug message")
    private String debugMessage;

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
