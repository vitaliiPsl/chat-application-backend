package com.example.chat.service.impl;

import com.example.chat.exception.ResourceNotFoundException;
import com.example.chat.model.user.User;
import com.example.chat.payload.user.UserDto;
import com.example.chat.repository.UserRepository;
import com.example.chat.utils.PayloadMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    UserRepository userRepository;
    PayloadMapper mapper;

    UserServiceImpl userService;

    @BeforeEach
    void init() {
        // payload mapper requires model mapper
        ModelMapper modelMapper = Mockito.spy(ModelMapper.class);
        mapper = Mockito.spy(new PayloadMapper(modelMapper));

        userService = new UserServiceImpl(userRepository, mapper);
    }

    @Test
    void whenGetUser_givenUserExist_thenReturnUser() {
        // given
        String userId = "1234-qwer";
        User user = User.builder().id(userId).nickname("test.user").email("test.user@mail.com").build();

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserDto result = userService.getUser(userId);

        // then
        verify(userRepository).findById(userId);
        assertThat(result.getId(), Matchers.is(userId));
        assertThat(result.getNickname(), Matchers.is(user.getNickname()));
        assertThat(result.getEmail(), Matchers.is(user.getEmail()));
    }

    @Test
    void whenGetUser_givenUserDoesntExist_thenThrowException() {
        // given
        String userId = "1234-qwer";

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void whenGetUsersByNickname_givenThereAreUsersWithGivenNickname_thenReturnThoseUsers() {
        // given
        String nickname = "test";

        List<User> users = List.of(
                User.builder().id("1234-qwer").nickname("test.user").email("test@mail.com").build(),
                User.builder().id("qwer-1234").nickname("user.test").email("user@mail.com").build()
        );

        // when
        when(userRepository.findByNicknameContainingIgnoreCase(nickname)).thenReturn(users);

        List<UserDto> result = userService.getUsersByNickname(nickname);

        // then
        verify(userRepository).findByNicknameContainingIgnoreCase(nickname);
        assertThat(result, Matchers.hasSize(2));
    }

    @Test
    void whenGetUsersByNickname_givenThereAreNoUsersWithGivenNickname_thenReturnEmptyList() {
        // given
        String nickname = "test";

        List<User> users = List.of();

        // when
        when(userRepository.findByNicknameContainingIgnoreCase(nickname)).thenReturn(users);

        List<UserDto> result = userService.getUsersByNickname(nickname);

        // then
        verify(userRepository).findByNicknameContainingIgnoreCase(nickname);
        assertThat(result, Matchers.empty());
    }

    @Test
    void whenGetUserDomainObject_givenUserExist_thenReturnUser() {
        // given
        String userId = "1234-qwer";
        User user = User.builder().id(userId).email("user@mail.com").build();

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserDomainObject(userId);

        // then
        verify(userRepository).findById(userId);
        assertThat(result, Matchers.is(user));
    }

    @Test
    void whenGetUserDomainObject_givenUserDoesntExist_thenThrowException() {
        // given
        String userId = "1234-qwer";

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserDomainObject(userId));
        verify(userRepository).findById(userId);
    }
}