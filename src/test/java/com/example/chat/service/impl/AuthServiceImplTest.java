package com.example.chat.service.impl;

import com.example.chat.exception.ResourceAlreadyExistException;
import com.example.chat.model.user.User;
import com.example.chat.payload.auth.AuthRequest;
import com.example.chat.payload.auth.AuthResponse;
import com.example.chat.payload.user.UserDto;
import com.example.chat.repository.UserRepository;
import com.example.chat.service.JwtService;
import com.example.chat.utils.PayloadMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    PayloadMapper mapper;

    @Mock
    AuthenticationManager authManager;

    @Mock
    JwtService jwtService;

    AuthServiceImpl authService;

    @BeforeEach
    void init() {
        // payload mapper requires model mapper
        ModelMapper modelMapper = Mockito.spy(ModelMapper.class);
        mapper = Mockito.spy(new PayloadMapper(modelMapper));

        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService, authManager, mapper);
    }

    @DisplayName("Sign up. Creates new user")
    @Test
    void givenSignUp_whenRegistrationDataIsValid_thenCreateNewUser() {
        // given
        UserDto userDto = UserDto.builder()
                .email("j.doe@mail.com")
                .nickname("j.doe")
                .password("password")
                .build();

        String encodedPassword = "rkep4h1etq8i";

        // when
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByNickname(userDto.getNickname())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        UserDto response = authService.signUp(userDto);

        // then
        verify(userRepository).findByEmail(userDto.getEmail());
        verify(userRepository).findByNickname(userDto.getNickname());
        verify(passwordEncoder).encode(userDto.getPassword());
        verify(userRepository).save(any(User.class));

        assertThat(response.getNickname(), is(userDto.getNickname()));
        assertThat(response.getEmail(), is(userDto.getEmail()));
    }

    @DisplayName("Sign up. Email is already taken")
    @Test
    void givenSignUp_whenEmailIsTaken_thenThrowException() {
        // given
        UserDto userDto = UserDto.builder().email("j.doe@mail.com").build();

        User otherUser = User.builder().email(userDto.getEmail()).build();

        // when
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(otherUser));

        // then
        assertThrows(ResourceAlreadyExistException.class, () -> authService.signUp(userDto));
        verify(userRepository).findByEmail(userDto.getEmail());
    }

    @DisplayName("Sign up. Nickname is already taken")
    @Test
    void givenSignUp_whenNicknameIsTaken_thenThrowException() {
        // given
        UserDto userDto = UserDto.builder().nickname("j.doe").build();

        User otherUser = User.builder().nickname(userDto.getNickname()).build();

        // when
        when(userRepository.findByNickname(userDto.getNickname())).thenReturn(Optional.of(otherUser));

        // then
        assertThrows(ResourceAlreadyExistException.class, () -> authService.signUp(userDto));
        verify(userRepository).findByEmail(userDto.getEmail());
    }

    @Test
    void givenSignIn_whenCredentialsAreValid_thenGenerateJwt() {
        // given
        String email = "j.doe@mail.com";
        String password = "password";
        String jwt = "eyJ0eXA.eyJzdWIi.Ou-2-0gYTg";

        AuthRequest request = AuthRequest.builder()
                .email(email)
                .password(password)
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(email, password);

        // when
        when(authManager.authenticate(auth)).thenReturn(auth);
        when(jwtService.createToken(auth)).thenReturn(jwt);

        AuthResponse response = authService.signIn(request);

        // then
        verify(authManager).authenticate(auth);
        verify(jwtService).createToken(auth);

        assertThat(response.getToken(), is(jwt));
    }

    @Test
    void givenSignIn_whenCredentialsAreInvalid_thenThrowException() {
        // given
        String email = "j.doe@mail.com";
        String password = "password";

        AuthRequest request = AuthRequest.builder().email(email).password(password).build();

        Authentication auth = new UsernamePasswordAuthenticationToken(email, password);

        // when
        when(authManager.authenticate(auth)).thenThrow(new BadCredentialsException("Invalid password"));

        // then
        assertThrows(BadCredentialsException.class, () -> authService.signIn(request));
        verify(authManager).authenticate(auth);
    }
}