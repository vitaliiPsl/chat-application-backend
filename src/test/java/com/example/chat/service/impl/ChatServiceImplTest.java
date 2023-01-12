package com.example.chat.service.impl;

import com.example.chat.exception.ResourceNotFoundException;
import com.example.chat.model.chat.Chat;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.ChatDto;
import com.example.chat.payload.chat.UserId;
import com.example.chat.repository.ChatRepository;
import com.example.chat.repository.UserRepository;
import com.example.chat.utils.PayloadMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {
    @Mock
    ChatRepository chatRepository;
    @Mock
    UserRepository userRepository;
    PayloadMapper mapper;

    ChatServiceImpl chatService;

    @BeforeEach
    void init() {
        // payload mapper requires model mapper
        ModelMapper modelMapper = Mockito.spy(ModelMapper.class);
        mapper = Mockito.spy(new PayloadMapper(modelMapper));

        chatService = new ChatServiceImpl(chatRepository, userRepository, mapper);
    }

    @Test
    void whenCreateChat_givenValidRequest_thenCreatesNewChat() {
        // given
        User actor = User.builder().id("1234-abcd").email("owner@mail.com").build();
        User other = User.builder().id("abcd-1234").email("other@mail.com").build();

        List<UserId> users = List.of(new UserId(other.getId()));
        ChatDto chatDto = ChatDto.builder().name("Test").description("Test chat").users(users).build();

        // when
        when(userRepository.findById(other.getId())).thenReturn(Optional.of(other));
        when(chatRepository.save(Mockito.any(Chat.class))).then(returnsFirstArg());

        ChatDto result = chatService.createChat(chatDto, actor);

        // then
        verify(userRepository).findById(other.getId());
        verify(chatRepository).save(Mockito.any(Chat.class));

        assertThat(result.getName(), Matchers.is(chatDto.getName()));
        assertThat(result.getDescription(), Matchers.is(chatDto.getDescription()));
        assertThat(result.getMembers(), Matchers.hasSize(2));
    }

    @Test
    void whenCreateChat_givenOneOfTheUsersDoesntExist_thenThrowsExceptions() {
        // given
        User actor = User.builder().id("1234-abcd").email("owner@mail.com").build();

        String userId = "abcd-1234";
        List<UserId> users = List.of(new UserId(userId));

        ChatDto chatDto = ChatDto.builder().name("Test").users(users).build();

        // when
        when(chatRepository.save(Mockito.any(Chat.class))).then(returnsFirstArg());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> chatService.createChat(chatDto, actor));
        verify(userRepository).findById(userId);
    }

    @Test
    void whenCreateChat_givenUserWithIdOfActor_thenThrowsExceptions() {
        // given
        String id = "1234-abcd";
        String email = "owner@mail.com";

        User actor = User.builder().id(id).email(email).build();
        User other = User.builder().id(id).email(email).build();

        List<UserId> users = List.of(new UserId(id));

        ChatDto chatDto = ChatDto.builder().name("Test").users(users).build();

        // when
        when(chatRepository.save(Mockito.any(Chat.class))).then(returnsFirstArg());
        when(userRepository.findById(id)).thenReturn(Optional.of(other));

        // then
        assertThrows(RuntimeException.class, () -> chatService.createChat(chatDto, actor));
        verify(userRepository).findById(id);
    }


    @Test
    void whenCreateChat_givenEmptyUserList_thenThrowsExceptions() {
        // given
        User actor = User.builder().id("1234-abcd").email("owner@mail.com").build();

        List<UserId> users = List.of();

        ChatDto chatDto = ChatDto.builder().name("Test").users(users).build();

        // when
        when(chatRepository.save(Mockito.any(Chat.class))).then(returnsFirstArg());

        // then
        assertThrows(RuntimeException.class, () -> chatService.createChat(chatDto, actor));
    }
}