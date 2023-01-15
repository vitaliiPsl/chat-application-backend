package com.example.chat.payload.chat;

import com.example.chat.payload.user.UserDto;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIncludeProperties({"id", "nickname"})
    private UserDto user;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @NotNull(message = "Content of the message is required")
    @Size(max = 1024, message = "Max length of the message is 1024 characters")
    private String content;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime sentAt;
}
