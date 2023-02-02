package com.example.chat.controller;

import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MessageDto;
import com.example.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chats")
public class MessageController {
    private final MessageService messageService;


    @GetMapping("{chatId}/messages")
    Page<MessageDto> getChatMessages(
            @PathVariable String chatId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Get {} messages of the chat {} with id less than {}", limit, chatId, lastId);

        return messageService.getMessages(chatId, lastId, limit, actor);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("{chatId}/messages")
    MessageDto saveChatMessage(
            @PathVariable String chatId,
            @RequestBody @Valid MessageDto messageDto,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Save message {} sent to the chat {}", messageDto, chatId);

        return messageService.saveMessage(chatId, messageDto, actor);
    }
}
