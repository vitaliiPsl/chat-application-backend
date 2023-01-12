package com.example.chat.service;

import com.example.chat.model.user.User;
import com.example.chat.payload.chat.ChatDto;

/**
 * Chat service
 */
public interface ChatService {

    /**
     * Create new chat
     *
     * @param chatDto chat details
     * @param actor   authenticated user, owner of the chat
     * @return created chat
     */
    ChatDto createChat(ChatDto chatDto, User actor);
}
