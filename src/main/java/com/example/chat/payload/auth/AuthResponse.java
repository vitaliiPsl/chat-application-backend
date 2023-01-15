package com.example.chat.payload.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "JWT token", example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1YTE1ZjRjZS0zNDQxLTQ5NjAtYTUyYy01Nzg0NDY1ZjQxZGQiLCJleHAiOjE2NzQyMjU1MjJ9.dL0NZi19otqdIobqtHqgkRzEKy2p-3Z099aEIg44rA8")
    private String token;
}
