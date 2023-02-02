package com.example.chat.payload.chat;

import com.example.chat.model.chat.member.MemberRole;
import com.example.chat.payload.groups.UpdateRequest;
import com.example.chat.payload.user.UserDto;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "User")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIncludeProperties({"id", "nickname"})
    private UserDto user;

    @Schema(accessMode = Schema.AccessMode.READ_WRITE, title = "Role", enumAsRef = true, example = "DEFAULT")
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @NotNull(message = "Role of the member is required", groups = UpdateRequest.class)
    private MemberRole role;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Joined at")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime joinedAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Updated at")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;
}
