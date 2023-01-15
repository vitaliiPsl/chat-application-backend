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
import com.example.chat.service.MemberService;
import com.example.chat.service.UserService;
import com.example.chat.utils.PayloadMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
    UserService userService;
    @Mock
    MemberService memberService;

    PayloadMapper mapper;

    ChatServiceImpl chatService;

    @BeforeEach
    void init() {
        // payload mapper requires model mapper
        ModelMapper modelMapper = Mockito.spy(ModelMapper.class);
        mapper = Mockito.spy(new PayloadMapper(modelMapper));

        chatService = new ChatServiceImpl(chatRepository, userService, memberService, mapper);
    }

    @Test
    void whenGetChatDomainObject_givenChatExist_thenReturnChat() {
        // given
        String chatId = "qwer-1234";
        Chat chat = Chat.builder().id(chatId).build();

        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        Chat result = chatService.getChatDomainObject(chatId);

        // then
        verify(chatRepository).findById(chatId);
        assertThat(result, Matchers.is(chat));
    }

    @Test
    void whenGetChatDomainObject_givenChatDoesntExist_thenThrowException() {
        // given
        String chatId = "qwer-1234";

        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> chatService.getChatDomainObject(chatId));
        verify(chatRepository).findById(chatId);
    }

    @Test
    void whenCreateChat_givenValidRequest_thenCreatesNewChat() {
        // given
        User actor = User.builder().id("1234-abcd").email("owner@mail.com").build();
        User other = User.builder().id("abcd-1234").email("other@mail.com").build();

        List<UserId> users = List.of(new UserId(other.getId()));
        ChatDto chatDto = ChatDto.builder().name("Test").description("Test chat").users(users).build();

        // when
        when(userService.getUserDomainObject(other.getId())).thenReturn(other);
        when(chatRepository.save(Mockito.any(Chat.class))).then(returnsFirstArg());

        ChatDto result = chatService.createChat(chatDto, actor);

        // then
        verify(userService).getUserDomainObject(other.getId());
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
        when(userService.getUserDomainObject(userId)).thenThrow(new ResourceNotFoundException(userId, User.class));

        // then
        assertThrows(ResourceNotFoundException.class, () -> chatService.createChat(chatDto, actor));
        verify(userService).getUserDomainObject(userId);
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
        when(userService.getUserDomainObject(id)).thenReturn(other);

        // then
        assertThrows(RuntimeException.class, () -> chatService.createChat(chatDto, actor));
        verify(userService).getUserDomainObject(id);
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

    @ParameterizedTest
    @EnumSource(value = MemberRole.class, mode = EnumSource.Mode.INCLUDE, names = {"OWNER", "ADMIN"})
    void whenUpdateChat_givenValidRequest_thenUpdateChat(MemberRole role) {
        // given
        User actor = User.builder().id("1234-abcd").email("owner@mail.com").build();
        Chat chat = Chat.builder().id("4321-qwer").name("Test").build();

        MemberId memberId = new MemberId(actor.getId(), chat.getId());
        Member member = Member.builder().id(memberId).user(actor).chat(chat).role(role).build();

        ChatDto chatDto = ChatDto.builder().name("Updated test").description("Updated description").build();

        // when
        when(memberService.getMemberDomainObject(actor.getId(), chat.getId())).thenReturn(member);

        ChatDto result = chatService.updateChat(chat.getId(), chatDto, actor);

        // then
        verify(memberService).getMemberDomainObject(actor.getId(), chat.getId());

        assertThat(result.getName(), Matchers.is(chatDto.getName()));
        assertThat(result.getDescription(), Matchers.is(chatDto.getDescription()));
        assertThat(result.getUpdatedAt(), Matchers.notNullValue());
    }

    @Test
    void whenUpdateChat_givenMemberIsDefaultUser_thenThrowException() {
        // given
        User actor = User.builder().id("1234-abcd").email("owner@mail.com").build();
        Chat chat = Chat.builder().id("4321-qwer").name("Test").build();

        MemberId memberId = new MemberId(actor.getId(), chat.getId());
        Member member = Member.builder().id(memberId).user(actor).chat(chat).role(MemberRole.DEFAULT).build();

        ChatDto chatDto = ChatDto.builder().name("Updated test").description("Updated description").build();

        // when
        when(memberService.getMemberDomainObject(actor.getId(), chat.getId())).thenReturn(member);

        // then
        assertThrows(RuntimeException.class, () -> chatService.updateChat(chat.getId(), chatDto, actor));
        verify(memberService).getMemberDomainObject(actor.getId(), chat.getId());
    }

    @Test
    void whenUpdateChat_givenMemberDoesntExist_thenThrowException() {
        // given
        String actorId = "1234-abcd";
        User actor = User.builder().id(actorId).email("owner@mail.com").build();

        String chatId = "4321-qwer";
        ChatDto chatDto = ChatDto.builder().name("Updated test").description("Updated description").build();

        // when
        when(memberService.getMemberDomainObject(actorId, chatId)).thenThrow(new IllegalStateException());

        // then
        assertThrows(RuntimeException.class, () -> chatService.updateChat(chatId, chatDto, actor));
        verify(memberService).getMemberDomainObject(actorId, chatId);
    }

    @Test
    void whenDeleteChat_givenValidRequest_thenDeleteChat() {
        // given
        String actorId = "1234-abcd";
        User actor = User.builder().id(actorId).email("owner@mail.com").build();

        String chatId = "4321-qwer";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        MemberId memberId = new MemberId(actor.getId(), chat.getId());
        Member member = Member.builder().id(memberId).user(actor).chat(chat).role(MemberRole.OWNER).build();

        // when
        when(memberService.getMemberDomainObject(actorId, chatId)).thenReturn(member);

        chatService.deleteChat(chat.getId(), actor);

        // then
        verify(memberService).getMemberDomainObject(actorId, chatId);
        verify(chatRepository).delete(chat);
    }

    @Test
    void whenDeleteChat_givenMemberDoesntExist_thenThrowException() {
        // given
        String actorId = "1234-abcd";
        User actor = User.builder().id(actorId).email("owner@mail.com").build();

        String chatId = "4321-qwer";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        // when
        when(memberService.getMemberDomainObject(actorId, chatId)).thenThrow(new IllegalStateException());

        // then
        assertThrows(IllegalStateException.class, () -> chatService.deleteChat(chat.getId(), actor));
        verify(memberService).getMemberDomainObject(actorId, chatId);
    }

    @ParameterizedTest
    @EnumSource(value = MemberRole.class, mode = EnumSource.Mode.INCLUDE, names = {"DEFAULT", "ADMIN"})
    void whenDeleteChat_givenMemberIsNotOwner_thenThrowException(MemberRole role) {
        // given
        String actorId = "1234-abcd";
        User actor = User.builder().id(actorId).email("owner@mail.com").build();

        String chatId = "4321-qwer";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        MemberId memberId = new MemberId(actor.getId(), chat.getId());
        Member member = Member.builder().id(memberId).user(actor).chat(chat).role(role).build();

        // when
        when(memberService.getMemberDomainObject(actorId, chatId)).thenReturn(member);

        // then
        assertThrows(IllegalStateException.class, () -> chatService.deleteChat(chat.getId(), actor));
        verify(memberService).getMemberDomainObject(actorId, chatId);
    }


    @Test
    void whenGetChat_givenValidRequest_thenReturnChat() {
        // given
        String actorId = "1234-abcd";
        User actor = User.builder().id(actorId).email("owner@mail.com").build();

        String chatId = "4321-qwer";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        MemberId memberId = new MemberId(actor.getId(), chat.getId());
        Member member = Member.builder().id(memberId).user(actor).chat(chat).role(MemberRole.OWNER).build();

        // when
        when(memberService.getMemberDomainObject(actorId, chatId)).thenReturn(member);

        ChatDto result = chatService.getChat(chat.getId(), actor);

        // then
        verify(memberService).getMemberDomainObject(actorId, chatId);

        assertThat(result.getId(), Matchers.is(chat.getId()));
        assertThat(result.getName(), Matchers.is(chat.getName()));
    }

    @Test
    void whenGetChat_givenIsNotAMember_thenThrowException() {
        // given
        String actorId = "1234-abcd";
        User actor = User.builder().id(actorId).email("owner@mail.com").build();

        String chatId = "4321-qwer";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        // when
        when(memberService.getMemberDomainObject(actorId, chatId)).thenThrow(new IllegalStateException());

        // then
        assertThrows(RuntimeException.class, () -> chatService.getChat(chat.getId(), actor));
        verify(memberService).getMemberDomainObject(actorId, chatId);
    }

    @Test
    void whenGetChatsOfActor_givenValidRequest_thenReturnChats() {
        // given
        User actor = User.builder().id("1234-abcd").email("owner@mail.com").build();

        Chat chat1 = Chat.builder().id("4321-qwer").name("Test").build();
        Chat chat2 = Chat.builder().id("qwer-4321").name("Test 1").build();

        List<Chat> chats = List.of(chat1, chat2);

        // when
        when(chatRepository.findByUserId(actor.getId())).thenReturn(chats);

        List<ChatDto> result = chatService.getChatsOfActor(actor);

        // then
        verify(chatRepository).findByUserId(actor.getId());

        assertThat(result, Matchers.hasSize(2));
    }
}