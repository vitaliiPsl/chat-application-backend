package com.example.chat.service;

import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MemberDto;

import java.util.List;

public interface MemberService {

    /**
     * Check if user is a member of the chat
     *
     * @param userId id of the user
     * @param chatId id of the chat
     * @return true, if is a member, false, if is not
     */
    boolean isMemberOfTheChat(String userId, String chatId);

    /**
     * Get members of the chat with given id
     *
     * @param chatId id of the chat
     * @param actor  authenticated user
     * @return list of the chat members
     */
    List<MemberDto> getChatMembers(String chatId, User actor);
}
