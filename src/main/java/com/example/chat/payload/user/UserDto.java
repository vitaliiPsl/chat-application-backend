package com.example.chat.payload.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(name = "User")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Id", example = "5a15f4ce-3441-4960-a52c-5784465f41dd")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @Schema(accessMode = Schema.AccessMode.READ_WRITE, title = "Nickname", example = "j.doe")
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @NotBlank(message = "Nickname is required")
    private String nickname;

    @Schema(accessMode = Schema.AccessMode.READ_WRITE, title = "Email", example = "j.doe@mail.com")
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @Email(message = "Must be a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, title = "Password", minLength = 8, example = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password is required")
    @Length(min = 8, message = "Password must contain at least 8 symbols")
    private String password;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Registration date")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Update date")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;
}
