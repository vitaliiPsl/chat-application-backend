package com.example.chat.payload.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @NotBlank(message = "Name of the chat is required")
    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private String description;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty(message = "There must be at least one other user in the chat")
    private List<UserId> users;

    @JsonIgnoreProperties({"chat"})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<MemberDto> members;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;
}
