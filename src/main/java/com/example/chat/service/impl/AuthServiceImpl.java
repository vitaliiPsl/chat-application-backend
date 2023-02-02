package com.example.chat.service.impl;

import com.example.chat.exception.ResourceAlreadyExistException;
import com.example.chat.model.user.User;
import com.example.chat.payload.auth.AuthRequest;
import com.example.chat.payload.auth.AuthResponse;
import com.example.chat.payload.user.UserDto;
import com.example.chat.repository.UserRepository;
import com.example.chat.service.AuthService;
import com.example.chat.service.JwtService;
import com.example.chat.utils.PayloadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final PayloadMapper mapper;

    @Override
    public UserDto signUp(UserDto userDto) {
        log.debug("Register new user with email '{}' and nickname '{}'", userDto.getEmail(), userDto.getNickname());

        // check if email is available
        Optional<User> existing = userRepository.findByEmail(userDto.getEmail());
        if (existing.isPresent()) {
            log.error("Email '{}' is already taken", userDto.getEmail());
            throw new ResourceAlreadyExistException("email", userDto.getEmail(), User.class);
        }

        // check if nickname is available
        existing = userRepository.findByNickname(userDto.getNickname());
        if (existing.isPresent()) {
            log.error("Nickname '{}' is already taken", userDto.getNickname());
            throw new ResourceAlreadyExistException("nickname", userDto.getNickname(), User.class);
        }

        User user = createUser(userDto);
        return mapper.mapUserToUserDto(user);
    }

    @Override
    public AuthResponse signIn(AuthRequest request) {
        log.debug("Authenticate user: {}", request.getEmail());

        Authentication authentication = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        authentication = authManager.authenticate(authentication);

        String token = jwtService.createToken(authentication);
        return new AuthResponse(token);
    }

    @Override
    public UserDto getActor(User user) {
        log.debug("Get actor user {}", user);

        return mapper.mapUserToUserDto(user);
    }

    private User createUser(UserDto userDto) {
        User user = User.builder()
                .email(userDto.getEmail())
                .nickname(userDto.getNickname())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .createdAt(LocalDateTime.now())
                .enabled(true)
                .build();

        return userRepository.save(user);
    }
}
