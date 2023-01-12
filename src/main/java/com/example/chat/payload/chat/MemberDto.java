package com.example.chat.payload.chat;

import com.example.chat.model.chat.member.MemberRole;
import com.example.chat.payload.user.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIgnoreProperties({"email"})
    private UserDto user;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private MemberRole role;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime joinedAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;
}
