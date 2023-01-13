package com.example.chat.controller;

import com.example.chat.payload.user.UserDto;
import com.example.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("{userId}")
    UserDto getUserById(@PathVariable String userId) {
        log.debug("Get user by id {}", userId);

        return userService.getUser(userId);
    }

    @GetMapping(params = "nickname")
    List<UserDto> getUserByNickname(@RequestParam(name = "nickname") String nickname) {
        log.debug("Get users by nickname: {}", nickname);

        return userService.getUsersByNickname(nickname);
    }
}