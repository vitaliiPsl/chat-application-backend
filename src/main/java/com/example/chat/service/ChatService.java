package com.example.chat.service;

import com.example.chat.model.chat.Chat;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.ChatDto;

import java.util.List;

/**
 * Chat service
 *
 * @see Chat
 */
public interface ChatService {

    /**
     * Get chat as domain object
     *
     * @param chatId id of the chat
     * @return retrieved chat
     */
    Chat getChatDomainObject(String chatId);

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

    /**
     * Get chat by id
     *
     * @param chatId id of the chat
     * @param actor  authenticated user
     * @return retrieved chat
     */
    ChatDto getChat(String chatId, User actor);

    /**
     * Get chats of authenticated user
     *
     * @param actor authenticated user
     * @return retrieved chat
     */
    List<ChatDto> getChatsOfActor(User actor);
}
