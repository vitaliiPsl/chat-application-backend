package com.example.chat.service;

import org.springframework.security.core.Authentication;

/**
 * Jwt service
 */
public interface JwtService {

    /**
     * Build jwt token from the user authentication
     *
     * @param authentication user authentication
     * @return jwt token
     */
    String createToken(Authentication authentication);

    /**
     * Verify jwt token
     *
     * @param token jwt token
     * @return authentication token
     */
    Authentication verifyToken(String token);
}
