package com.example.chat.service;

import com.example.chat.model.chat.member.Member;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MemberDto;
import com.example.chat.payload.chat.UserId;

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
     * Get chat member as a domain object
     *
     * @param userId id of the user
     * @param chatId id of the chat
     * @return retrieved member object
     */
    Member getMemberDomainObject(String userId, String chatId);

    /**
     * Get members of the chat with given id
     *
     * @param chatId id of the chat
     * @param actor  authenticated user
     * @return list of the chat members
     */
    List<MemberDto> getChatMembers(String chatId, User actor);

    /**
     * Add new chat member
     *
     * @param chatId id of the chat
     * @param userId id of the user
     * @param actor  authenticated user
     * @return created chat member
     */
    MemberDto addChatMember(String chatId, UserId userId, User actor);

    /**
     * Update member of the chat
     *
     * @param chatId    id of the chat
     * @param userId    id of the user
     * @param memberDto update details
     * @param actor     authenticated user
     * @return updated chat member
     */
    MemberDto updateChatMember(String chatId, String userId, MemberDto memberDto, User actor);

    /**
     * Remove member of the chat
     *
     * @param chatId id of the chat
     * @param user   id of the user
     * @param actor  authenticated user
     */
    void removeChatMember(String chatId, String user, User actor);

    /**
     * Get member of the chat
     * @param chatId id of the chat
     * @param userId id of the user
     * @param actor authenticated user
     * @return member of the chat exist
     */
    MemberDto getChatMember(String chatId, String userId, User actor);
}
