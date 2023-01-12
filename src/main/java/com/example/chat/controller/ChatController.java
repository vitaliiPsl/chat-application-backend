package com.example.chat.controller;

import com.example.chat.model.user.User;
import com.example.chat.payload.chat.ChatDto;
import com.example.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    ChatDto createChat(
            @RequestBody @Valid ChatDto chatDto,
            @AuthenticationPrincipal User actor
    ) {
        log.debug("Create new chat: {}", chatDto);

        return chatService.createChat(chatDto, actor);
    }
}
