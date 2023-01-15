package com.example.chat.payload.chat;

import com.example.chat.payload.groups.CreateRequest;
import com.example.chat.payload.groups.UpdateRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Id", example = "5a15f4ce-3441-4960-a52c-5784465f41dd")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @Schema(accessMode = Schema.AccessMode.READ_WRITE, title = "Name", example = "Group chat")
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @NotBlank(message = "Name of the chat is required", groups = {CreateRequest.class, UpdateRequest.class})
    private String name;

    @Schema(accessMode = Schema.AccessMode.READ_WRITE, title = "Description", example = "Test chat")
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private String description;

    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, title = "Ids of the members")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty(message = "There must be at least one other user in the chat", groups = {CreateRequest.class})
    private List<UserId> users;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Members")
    @JsonIgnoreProperties({"chat"})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<MemberDto> members;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Last message")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private MessageDto lastMessage;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Created at")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Updated at")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;
}
