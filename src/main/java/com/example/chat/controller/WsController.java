package com.example.chat.controller;

import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MessageDto;
import com.example.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class WsController {
    private final MessageService messageService;

    @MessageMapping("/chats/{chatId}/messages")
    public void saveMessage(@DestinationVariable String chatId, MessageDto message, Authentication auth) {
        log.info("Send message {} to chat {}", message, chatId);

        User user = (User) auth.getPrincipal();
        messageService.saveMessage(chatId, message, user);
    }
}
