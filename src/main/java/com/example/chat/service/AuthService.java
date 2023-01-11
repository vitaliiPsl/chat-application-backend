package com.example.chat.service;

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
}
