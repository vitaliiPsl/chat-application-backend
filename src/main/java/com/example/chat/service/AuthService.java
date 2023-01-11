package com.example.chat.service;

import com.example.chat.payload.auth.AuthRequest;
import com.example.chat.payload.auth.AuthResponse;
import com.example.chat.payload.user.UserDto;

/**
 * Authentication service
 */
public interface AuthService {

    /**
     * Register a new user
     *
     * @param userDto user to register
     * @return registered user
     */
    UserDto signUp(UserDto userDto);

    /**
     * Authenticate the user with given credentials
     *
     * @param request credentials of the user
     * @return auth response that contains the JWT token
     */
    AuthResponse signIn(AuthRequest request);
}
