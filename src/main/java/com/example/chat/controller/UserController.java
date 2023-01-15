package com.example.chat.controller;

import com.example.chat.payload.user.UserDto;
import com.example.chat.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
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
    List<UserDto> getUserByNickname(
            @Parameter(name = "Nickname", description = "Nickname of the user")
            @RequestParam(name = "nickname") String nickname
    ) {
        log.debug("Get users by nickname: {}", nickname);

        return userService.getUsersByNickname(nickname);
    }
}
