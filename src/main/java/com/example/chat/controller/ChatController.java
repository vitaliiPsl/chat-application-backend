package com.example.chat.controller;

import com.example.chat.model.user.User;
import com.example.chat.payload.chat.ChatDto;
import com.example.chat.payload.groups.CreateRequest;
import com.example.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    ChatDto createChat(
            @RequestBody @Validated(CreateRequest.class) ChatDto chatDto,
            @AuthenticationPrincipal User actor
    ) {
        log.debug("Create new chat: {}", chatDto);

        return chatService.createChat(chatDto, actor);
    }

    @PutMapping("{chatId}")
    ChatDto createChat(
            @PathVariable String chatId,
            @RequestBody @Valid ChatDto chatDto,
            @AuthenticationPrincipal User actor
    ) {
        log.debug("Update chat {}. Update details: {}", chatId, chatDto);

        return chatService.updateChat(chatId, chatDto, actor);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{chatId}")
    void createChat(
            @PathVariable String chatId,
            @AuthenticationPrincipal User actor
    ) {
        log.debug("Delete chat {}", chatId);

        chatService.deleteChat(chatId, actor);
    }

    @GetMapping("{chatId}")
    ChatDto getChat(
            @PathVariable String chatId,
            @AuthenticationPrincipal User actor
    ) {
        log.debug("Get chat {}", chatId);

        return chatService.getChat(chatId, actor);
    }
}
