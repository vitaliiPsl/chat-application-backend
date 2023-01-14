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
