package com.example.chat.service.impl;

import com.example.chat.model.chat.Chat;
import com.example.chat.model.chat.Message;
import com.example.chat.model.chat.member.Member;
import com.example.chat.model.chat.member.MemberId;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MessageDto;
import com.example.chat.repository.MessageRepository;
import com.example.chat.service.MemberService;
import com.example.chat.service.MessageService;
import com.example.chat.utils.PayloadMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {
    @Mock
    MessageRepository messageRepository;
    @Mock
    MemberService memberService;
    @Mock
    SimpMessagingTemplate messagingTemplate;

    PayloadMapper mapper;
    MessageService messageService;

    @BeforeEach
    void init() {
        ModelMapper modelMapper = Mockito.spy(ModelMapper.class);
        mapper = Mockito.spy(new PayloadMapper(modelMapper));

        messageService = new MessageServiceImpl(messageRepository, memberService, messagingTemplate, mapper);
    }

    @Test
    void whenSaveMessage_givenValidRequest_thenSaveMessageAndUpdateLastChatMessage() {
        // given
        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).email("test@mail.com").build();

        String chatId = "qwer-1234";
        Chat chat = Chat.builder().id(chatId).build();

        Member member = Member.builder().id(new MemberId(actorId, chatId)).user(actor).chat(chat).build();
        MessageDto messageDto = MessageDto.builder().content("Test message").build();

        Message message = Message.builder().content("Test message").user(actor).chat(chat).build();
        MessageDto responseDto = MessageDto.builder().content("Test message").build();

        String destination = "/topic/chats/" + chatId + "/messages";

        // when
        when(memberService.getMemberDomainObject(actorId, chatId)).thenReturn(member);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(mapper.mapMessageToMessageDto(message)).thenReturn(responseDto);

        MessageDto result = messageService.saveMessage(chatId, messageDto, actor);

        // then
        verify(memberService).getMemberDomainObject(actorId, chatId);
        verify(messageRepository).save(any(Message.class));
        verify(messagingTemplate).convertAndSend(destination, responseDto);

        assertThat(result.getContent(), Matchers.is(messageDto.getContent()));

        assertThat(chat.getLastMessage(), Matchers.notNullValue());
        assertThat(chat.getLastMessage().getContent(), Matchers.is(messageDto.getContent()));
    }

    @Test
    void whenSaveMessage_givenActorIsNotChatMember_thenThrowException() {
        // given
        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).email("test@mail.com").build();

        String chatId = "qwer-1234";

        MessageDto messageDto = MessageDto.builder().content("Test message").build();

        // when
        when(memberService.getMemberDomainObject(actorId, chatId)).thenThrow(new IllegalStateException());

        // then
        assertThrows(RuntimeException.class, () -> messageService.saveMessage(chatId, messageDto, actor));
        verify(memberService).getMemberDomainObject(actorId, chatId);
    }

    @Test
    void whenGetMessages_givenLastMessageId_thenReturnMessagesThatAreBeforeMessageWithGiveId() {
        // given
        String chatId = "qwer-1234";

        String userId = "1234-qwer";
        User actor = User.builder().id(userId).build();

        long lastMessageId = 51L;
        int limit = 10;

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        PageRequest pageRequest = PageRequest.ofSize(limit).withSort(sort);

        List<Message> messages = List.of(
                Message.builder().id(50L).build(),
                Message.builder().id(30L).build(),
                Message.builder().id(2L).build()
        );

        Page<Message> page = new PageImpl<>(messages);

        // when
        when(memberService.isMemberOfTheChat(userId, chatId)).thenReturn(true);
        when(messageRepository.findAllByChatIdAndIdIsBefore(chatId, lastMessageId, pageRequest)).thenReturn(page);

        Page<MessageDto> result = messageService.getMessages(chatId, lastMessageId, limit, actor);

        // then
        verify(memberService).isMemberOfTheChat(userId, chatId);
        verify(messageRepository).findAllByChatIdAndIdIsBefore(chatId, lastMessageId, pageRequest);

        assertThat(result.getSize(), Matchers.is(3));
        assertThat(result.getContent().get(0).getId(), Matchers.is(50L));
        assertThat(result.getContent().get(1).getId(), Matchers.is(30L));
        assertThat(result.getContent().get(2).getId(), Matchers.is(2L));
    }

    @Test
    void whenGetMessages_givenLastMessageIdIsNull_thenReturnLastMessages() {
        // given
        String chatId = "qwer-1234";

        String userId = "1234-qwer";
        User actor = User.builder().id(userId).build();

        int limit = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        PageRequest pageRequest = PageRequest.ofSize(limit).withSort(sort);

        List<Message> messages = List.of(
                Message.builder().id(50L).build(),
                Message.builder().id(30L).build(),
                Message.builder().id(2L).build()
        );

        Page<Message> page = new PageImpl<>(messages);

        // when
        when(memberService.isMemberOfTheChat(userId, chatId)).thenReturn(true);
        when(messageRepository.findAllByChatId(chatId, pageRequest)).thenReturn(page);

        Page<MessageDto> result = messageService.getMessages(chatId, null, limit, actor);

        // then
        verify(memberService).isMemberOfTheChat(userId, chatId);
        verify(messageRepository).findAllByChatId(chatId, pageRequest);

        assertThat(result.getSize(), Matchers.is(3));
        assertThat(result.getContent().get(0).getId(), Matchers.is(50L));
        assertThat(result.getContent().get(1).getId(), Matchers.is(30L));
        assertThat(result.getContent().get(2).getId(), Matchers.is(2L));
    }

    @Test
    void whenGetMessages_givenLimitIsLessThan1_thenThrowException() {
        // given
        String chatId = "qwer-1234";

        String userId = "1234-qwer";
        User actor = User.builder().id(userId).build();

        int limit = 0;
        long lastMessageId = 85;

        // when
        when(memberService.isMemberOfTheChat(userId, chatId)).thenReturn(true);

        // then
        assertThrows(RuntimeException.class, () -> messageService.getMessages(chatId, lastMessageId, limit, actor));
        verify(memberService).isMemberOfTheChat(userId, chatId);
    }

    @Test
    void whenGetMessages_givenActorIsNotChatMember_thenThrowException() {
        // given
        String chatId = "qwer-1234";

        String userId = "1234-qwer";
        User actor = User.builder().id(userId).build();

        int limit = 0;
        long lastMessageId = 85;

        // when
        when(memberService.isMemberOfTheChat(userId, chatId)).thenReturn(false);

        // then
        assertThrows(RuntimeException.class, () -> messageService.getMessages(chatId, lastMessageId, limit, actor));
        verify(memberService).isMemberOfTheChat(userId, chatId);
    }
}