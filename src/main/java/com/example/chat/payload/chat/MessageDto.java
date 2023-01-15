package com.example.chat.payload.chat;

import com.example.chat.payload.user.UserDto;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Id", example = "9")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "User")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIncludeProperties({"id", "nickname"})
    private UserDto user;

    @Schema(accessMode = Schema.AccessMode.READ_WRITE, title = "Content", maxLength = 1024, example = "Test message")
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @NotNull(message = "Content of the message is required")
    @Size(max = 1024, message = "Max length of the message is 1024 characters")
    private String content;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Sent at")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime sentAt;
}
