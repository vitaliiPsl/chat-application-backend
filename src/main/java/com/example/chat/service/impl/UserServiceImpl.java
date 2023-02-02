package com.example.chat.service.impl;

import com.example.chat.exception.ResourceNotFoundException;
import com.example.chat.model.user.User;
import com.example.chat.payload.user.UserDto;
import com.example.chat.repository.UserRepository;
import com.example.chat.service.UserService;
import com.example.chat.utils.PayloadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PayloadMapper mapper;

    @Override
    public User getUserDomainObject(String userId) {
        log.debug("Get user domain object. Id {}", userId);

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.error("User with id {} doesn't exist", userId);
            throw new ResourceNotFoundException(userId, User.class);
        }

        return user.get();
    }

    @Override
    public UserDto getUser(String userId) {
        log.debug("Get user by id {}", userId);

        User user = getUserDomainObject(userId);
        return mapper.mapUserToUserDto(user);
    }

    @Override
    public List<UserDto> getUsersByNickname(String nickname) {
        log.debug("Get users with nickname that matches '{}'", nickname);

        List<User> users = userRepository.findByNicknameContainingIgnoreCase(nickname);

        return users.stream().map(mapper::mapUserToUserDto).collect(Collectors.toList());
    }
}
