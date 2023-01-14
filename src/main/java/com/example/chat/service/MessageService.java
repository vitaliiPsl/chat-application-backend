package com.example.chat.service;

import com.example.chat.model.chat.Message;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MessageDto;

/**
 * Message service
 *
 * @see Message
 */
public interface MessageService {

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
