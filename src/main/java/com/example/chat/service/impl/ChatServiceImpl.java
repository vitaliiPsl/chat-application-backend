package com.example.chat.service.impl;

import com.example.chat.exception.ResourceNotFoundException;
import com.example.chat.model.chat.Chat;
import com.example.chat.model.chat.member.Member;
import com.example.chat.model.chat.member.MemberRole;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.ChatDto;
import com.example.chat.payload.chat.UserId;
import com.example.chat.repository.ChatRepository;
import com.example.chat.service.ChatService;
import com.example.chat.service.MemberService;
import com.example.chat.service.UserService;
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
    private final UserService userService;
    private final MemberService memberService;

    private final PayloadMapper mapper;

    @Override
    public Chat getChatDomainObject(String chatId) {
        log.debug("Get chat domain object. Id {}", chatId);

        Optional<Chat> chat = chatRepository.findById(chatId);
        if (chat.isEmpty()) {
            log.error("Chat with id {} doesn't exist", chatId);
            throw new ResourceNotFoundException(chatId, Chat.class);
        }

        return chat.get();
    }

    @Override
    public ChatDto createChat(ChatDto chatDto, User actor) {
        log.debug("Create new chat {}. Actor: {}", chatDto, actor);

        Chat chat = buildChat(chatDto, actor);

        return mapper.mapChatToChatDto(chat);
    }

    @Override
    public ChatDto updateChat(String chatId, ChatDto chatDto, User actor) {
        log.debug("Update chat {}. Update details {}. Actor: {}", chatId, chatDto, actor);

        Member member = memberService.getMemberDomainObject(actor.getId(), chatId);
        if (member.getRole() == MemberRole.DEFAULT) {
            log.error("Only the owner and admins can update the chat");
            throw new IllegalStateException("Only the owner and admins can update the chat");
        }

        Chat chat = member.getChat();
        chat.setName(chatDto.getName());
        chat.setDescription(chatDto.getDescription());
        chat.setUpdatedAt(LocalDateTime.now());

        return mapper.mapChatToChatDto(chat);
    }

    @Override
    public void deleteChat(String chatId, User actor) {
        log.debug("Delete chat {}", chatId);

        Member member = memberService.getMemberDomainObject(actor.getId(), chatId);
        if (member.getRole() != MemberRole.OWNER) {
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
        Member member = memberService.getMemberDomainObject(actor.getId(), chatId);

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

        if (members.size() == 0) {
            log.error("There must be at least one other user in the chat");
            throw new IllegalStateException("You must provide at least one other user to create a chat");
        }

        return members;
    }

    private Member mapMember(String userId, Chat chat) {
        log.debug("Map user with id {} to member of the chat {}", userId, chat);

        User user = userService.getUserDomainObject(userId);

        Member member = new Member(user, chat, MemberRole.DEFAULT, chat.getCreatedAt());
        if (chat.getMembers().contains(member)) {
            log.error("Cannot add user {} to the same chat multiple times", user);
            throw new IllegalStateException("Cannot add user to the same chat multiple times");
        }

        return member;
    }
}
