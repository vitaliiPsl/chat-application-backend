package com.example.chat.payload.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserId {
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, title = "User id", example = "5a15f4ce-3441-4960-a52c-5784465f41dd")
    @NotBlank(message = "Id of the user is required")
    private String id;
}