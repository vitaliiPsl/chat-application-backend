package com.example.chat.service.impl;

import com.example.chat.model.chat.Chat;
import com.example.chat.model.chat.Message;
import com.example.chat.model.chat.member.Member;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MessageDto;
import com.example.chat.repository.MessageRepository;
import com.example.chat.service.MemberService;
import com.example.chat.service.MessageService;
import com.example.chat.utils.PayloadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MemberService memberService;
    private final PayloadMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public Page<MessageDto> getMessages(String chatId, Long lastMessageId, Integer limit, User actor) {
        log.debug("Get top {} messages of the chat {} that are before message with id {}", limit, chatId, lastMessageId);

        if(!memberService.isMemberOfTheChat(actor.getId(), chatId)) {
            log.error("User {} is not a member of chat {}", actor.getId(), chatId);
            throw new IllegalStateException("Not a member of the chat");
        }

        if (limit < 1) {
            log.error("Number of messages is less than 1");
            throw new IllegalStateException("Page size must not be less than one");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.ofSize(limit).withSort(sort);

        Page<Message> messages;
        if (lastMessageId == null) {
            messages = messageRepository.findAllByChatId(chatId, pageable);
        } else {
            messages = messageRepository.findAllByChatIdAndIdIsBefore(chatId, lastMessageId, pageable);
        }

        return messages.map(mapper::mapMessageToMessageDto);
    }

    @Override
    public MessageDto saveMessage(String chatId, MessageDto messageDto, User actor) {
        log.debug("Save message {} sent to the chat {}", messageDto, chatId);

        Member actorMember = memberService.getMemberDomainObject(actor.getId(), chatId);
        Chat chat = actorMember.getChat();

        Message message = buildMessage(messageDto, actor, chat);
        message = messageRepository.save(message);
        chat.setLastMessage(message);

        return mapper.mapMessageToMessageDto(message);
    }

    private Message buildMessage(MessageDto messageDto, User actor, Chat chat) {
        return Message.builder().user(actor).chat(chat)
                .content(messageDto.getContent())
                .sentAt(LocalDateTime.now()).build();
    }
}
