package com.example.chat.service;

import com.example.chat.model.chat.Message;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MessageDto;
import org.springframework.data.domain.Page;

/**
 * Message service
 *
 * @see Message
 */
public interface MessageService {

    /**
     * Get chat messages.
     * If given id of the last message, then return messages sent before that message
     *
     * @param chatId        id of the chat
     * @param lastMessageId id of the last message
     * @param limit         number of messages to retrieve
     * @param actor         authenticated user
     * @return list of chat messages
     */
    Page<MessageDto> getMessages(String chatId, Long lastMessageId, Integer limit, User actor);

    /**
     * Save new message
     *
     * @param chatId     id of the chat
     * @param messageDto message to save
     * @param actor      authenticated user
     * @return saved message
     */
    MessageDto saveMessage(String chatId, MessageDto messageDto, User actor);
}
