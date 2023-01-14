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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {
    @Mock
    MessageRepository messageRepository;
    @Mock
    MemberService memberService;

    PayloadMapper mapper;
    MessageService messageService;

    @BeforeEach
    void init() {
        ModelMapper modelMapper = Mockito.spy(ModelMapper.class);
        mapper = Mockito.spy(new PayloadMapper(modelMapper));

        messageService = new MessageServiceImpl(messageRepository, memberService, mapper);
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

        // when
        when(memberService.getMemberDomainObject(actorId, chatId)).thenReturn(member);
        when(messageRepository.save(any(Message.class))).then(returnsFirstArg());

        MessageDto result = messageService.saveMessage(chatId, messageDto, actor);

        // then
        verify(memberService).getMemberDomainObject(actorId, chatId);
        verify(messageRepository).save(any(Message.class));

        assertThat(result.getContent(), Matchers.is(messageDto.getContent()));
        assertThat(result.getSentAt(), Matchers.notNullValue());

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
}