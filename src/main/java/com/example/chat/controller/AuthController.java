package com.example.chat.controller;

import com.example.chat.model.user.User;
import com.example.chat.payload.auth.AuthRequest;
import com.example.chat.payload.auth.AuthResponse;
import com.example.chat.payload.user.UserDto;
import com.example.chat.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    UserDto signUp(@Valid @RequestBody UserDto userDto) {
        return authService.signUp(userDto);
    }

    @PostMapping("/signin")
    AuthResponse signIn(@Valid @RequestBody AuthRequest request) {
        return authService.signIn(request);
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("me")
    UserDto getAuthenticatedUser(@Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return authService.getActor(user);
    }
}
