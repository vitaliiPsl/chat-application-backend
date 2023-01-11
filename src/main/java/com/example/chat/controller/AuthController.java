package com.example.chat.controller;

import com.example.chat.payload.auth.AuthRequest;
import com.example.chat.payload.auth.AuthResponse;
import com.example.chat.payload.user.UserDto;
import com.example.chat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
