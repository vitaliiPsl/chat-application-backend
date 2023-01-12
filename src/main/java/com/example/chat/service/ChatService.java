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

    /**
     * Update chat with the given id. Requires actor to be owner or admin
     *
     * @param chatId  id of the chat
     * @param chatDto update details
     * @param actor   authenticated user
     * @return updated chat
     */
    ChatDto updateChat(String chatId, ChatDto chatDto, User actor);

    /**
     * Delete chat with the given id. Requires actor to the owner
     *
     * @param chatId id of the chat
     * @param actor  authenticated user
     */
    void deleteChat(String chatId, User actor);
}
