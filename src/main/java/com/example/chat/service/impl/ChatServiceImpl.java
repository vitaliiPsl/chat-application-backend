package com.example.chat.service.impl;

import com.example.chat.exception.ResourceNotFoundException;
import com.example.chat.model.chat.Chat;
import com.example.chat.model.chat.member.Member;
import com.example.chat.model.chat.member.MemberId;
import com.example.chat.model.chat.member.MemberRole;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.ChatDto;
import com.example.chat.payload.chat.UserId;
import com.example.chat.repository.ChatRepository;
import com.example.chat.repository.MemberRepository;
import com.example.chat.repository.UserRepository;
import com.example.chat.service.ChatService;
import com.example.chat.utils.PayloadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    private final PayloadMapper mapper;

    @Override
    public ChatDto createChat(ChatDto chatDto, User actor) {
        log.debug("Create new chat {}. Actor: {}", chatDto, actor);

        Chat chat = buildChat(chatDto, actor);

        return mapper.mapChatToChatDto(chat);
    }

    @Override
    public ChatDto updateChat(String chatId, ChatDto chatDto, User actor) {
        log.debug("Update chat {}. Update details {}. Actor: {}", chatId, chatDto, actor);

        Member member = getMemberById(actor.getId(), chatId);
        if(member.getRole() == MemberRole.DEFAULT) {
            log.error("Only the owner and admins can update the chat");
            throw new IllegalStateException("Only the owner and admins can update the chat");
        }

        Chat chat = updateChat(member.getChat(), chatDto);
        return mapper.mapChatToChatDto(chat);
    }

    @Override
    public void deleteChat(String chatId, User actor) {
        log.debug("Delete chat {}", chatId);

        Member member = getMemberById(actor.getId(), chatId);
        if(member.getRole() != MemberRole.OWNER) {
            log.error("Only the owner can delete the chat");
            throw new IllegalStateException("Only the owner can delete the chat");
        }

        Chat chat = member.getChat();
        chatRepository.delete(chat);
    }

    @Transactional(readOnly = true)
    @Override
    public ChatDto getChat(String chatId, User actor) {
        log.debug("Get chat by id {}", chatId);

        // Get the member to verify that chat exists and the user is its member
        Member member = getMemberById(actor.getId(), chatId);

        Chat chat = member.getChat();
        return mapper.mapChatToChatDto(chat);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatDto> getChatsOfActor(User actor) {
        log.debug("Get chats of actor");

        List<Chat> chats = chatRepository.findByUserId(actor.getId());

        return chats.stream().map(mapper::mapChatToChatDto).collect(Collectors.toList());
    }

    private Member getMemberById(String userId, String chatId) {
        log.debug("Get member: user id {}, chat id {}", userId, chatId);

        MemberId id = new MemberId(userId, chatId);

        Optional<Member> member = memberRepository.findById(id);
        if(member.isEmpty()) {
            log.error("User {} is not a member of chat {}", userId, chatId);
            throw new IllegalStateException("Not a member of the chat");
        }

        return member.get();
    }

    private User getUserById(String userId) {
        log.debug("Get user by id: {}", userId);

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.error("User with id {} doesn't exist", userId);
            throw new ResourceNotFoundException(userId, User.class);
        }

        return user.get();
    }

    private Chat buildChat(ChatDto chatDto, User actor) {
        // build chat object
        Chat chat = Chat.builder().name(chatDto.getName())
                .description(chatDto.getDescription())
                .createdAt(LocalDateTime.now()).build();

        chat = chatRepository.save(chat);

        // map owner
        Member owner = new Member(actor, chat, MemberRole.OWNER);
        chat.addMember(owner);

        // map other users
        Set<Member> members = mapMembers(chatDto.getUsers(), chat);
        chat.getMembers().addAll(members);

        return chat;
    }

    private Set<Member> mapMembers(List<UserId> users, Chat chat) {
        log.debug("Map users with {} to members of the chat {}", users, chat);

        Set<Member> members = users.stream()
                .map(userId -> mapMember(userId.getId(), chat))
                .collect(Collectors.toSet());

        if(members.size() == 0) {
            log.error("There must be at least one other user in the chat");
            throw new IllegalStateException("You must provide at least one other user to create a chat");
        }

        return members;
    }

    private Member mapMember(String userId, Chat chat) {
        log.debug("Map user with id {} to member of the chat {}", userId, chat);

        User user = getUserById(userId);

        Member member = new Member(user, chat, MemberRole.DEFAULT, chat.getCreatedAt());
        if(chat.getMembers().contains(member)) {
            log.error("Cannot add user {} to the same chat multiple times", user);
            throw new IllegalStateException("Cannot add user to the same chat multiple times");
        }

        return member;
    }

    private static Chat updateChat(Chat chat, ChatDto chatDto) {
        chat.setName(chatDto.getName());
        chat.setDescription(chatDto.getDescription());
        chat.setUpdatedAt(LocalDateTime.now());

        return chat;
    }
}
