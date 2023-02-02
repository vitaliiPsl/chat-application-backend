package com.example.chat.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.chat.config.security.jwt.JwtAuthenticationToken;
import com.example.chat.model.user.User;
import com.example.chat.repository.UserRepository;
import com.example.chat.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtServiceImpl implements JwtService {
    private final UserRepository userRepository;

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.token-expiration-time-min}")
    private long expirationTimeMin;

    @Override
    public String createToken(Authentication authentication) {
        log.debug("Create jwt token for authentication: {}", authentication);

        Algorithm algorithm = Algorithm.HMAC256(secret);

        User user = (User) authentication.getPrincipal();
        Instant expiresAt = LocalDateTime.now()
                .plusMinutes(expirationTimeMin)
                .atZone(ZoneId.systemDefault()).toInstant();

        return JWT.create().withSubject(user.getId()).withExpiresAt(expiresAt).sign(algorithm);
    }

    @Override
    public Authentication verifyToken(String token) {
        log.debug("Verify jwt token: {}", token);

        String id = decodeToken(token);

        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            log.error("Failed to verify jwt token. User with id {} doesn't exist", id);
            throw new IllegalStateException("Invalid jwt token");
        }

        return new JwtAuthenticationToken(token, user.get(), true);
    }

    private String decodeToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();

            DecodedJWT decodedToken = verifier.verify(token);
            return decodedToken.getSubject();
        } catch (JWTVerificationException e) {
            log.error("Invalid JWT token: {}", token);
            throw new IllegalStateException("Invalid jwt token");
        } catch (Exception e) {
            log.error("Exception while decoding jwt token");
            throw new IllegalStateException("Exception while decoding jwt token");
        }
    }
}
