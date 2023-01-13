package com.example.chat.service.impl;

import com.example.chat.model.chat.Chat;
import com.example.chat.model.chat.member.Member;
import com.example.chat.model.chat.member.MemberId;
import com.example.chat.model.chat.member.MemberRole;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MemberDto;
import com.example.chat.payload.chat.UserId;
import com.example.chat.repository.ChatRepository;
import com.example.chat.repository.MemberRepository;
import com.example.chat.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    ChatRepository chatRepository;
    @Mock
    UserRepository userRepository;
    PayloadMapper mapper;
    MemberServiceImpl memberService;

    @BeforeEach
    void init() {
        // payload mapper requires model mapper
        ModelMapper modelMapper = Mockito.spy(ModelMapper.class);
        mapper = Mockito.spy(new PayloadMapper(modelMapper));

        memberService = new MemberServiceImpl(memberRepository, chatRepository, userRepository, mapper);
    }

    @Test
    void whenIsMemberOfTheChat_givenUserIsAMemberOfTheChat_thenReturnTrue() {
        // given
        String userId = "1234-qwer";
        String chatId = "qwer-1234";

        MemberId memberId = new MemberId(userId, chatId);
        Member member = Member.builder().id(memberId).build();

        // when
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        boolean result = memberService.isMemberOfTheChat(userId, chatId);

        // then
        verify(memberRepository).findById(memberId);
        assertThat(result, Matchers.is(true));
    }

    @Test
    void whenIsMemberOfTheChat_givenUserIsNotAMemberOfTheChat_thenReturnFalse() {
        // given
        String userId = "1234-qwer";
        String chatId = "qwer-1234";

        MemberId memberId = new MemberId(userId, chatId);

        // when
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        boolean result = memberService.isMemberOfTheChat(userId, chatId);

        // then
        verify(memberRepository).findById(memberId);
        assertThat(result, Matchers.is(false));
    }

    @Test
    void whenGetChatMembers_givenValidRequest_thenReturnChatMembersSortedByTheirRole() {
        // given
        String chatId = "qwer-1234";
        String actorId = "1234-qwer";

        User actor = User.builder().id(actorId).build();
        Member actorMember = Member.builder().id(new MemberId(actorId, chatId)).role(MemberRole.DEFAULT).build();

        List<Member> members = List.of(
                actorMember,
                Member.builder().id(new MemberId("1234-abcd", chatId)).role(MemberRole.OWNER).build(),
                Member.builder().id(new MemberId("abcd-1234", chatId)).role(MemberRole.ADMIN).build()
        );

        // when
        when(memberRepository.findById(actorMember.getId())).thenReturn(Optional.of(actorMember));
        when(memberRepository.findByChat_Id(chatId)).thenReturn(members);

        List<MemberDto> result = memberService.getChatMembers(chatId, actor);

        // then
        verify(memberRepository).findById(actorMember.getId());
        verify(memberRepository).findByChat_Id(chatId);

        assertThat(result, Matchers.hasSize(3));
        assertAll(() -> {
            assertThat(result.get(0).getRole(), Matchers.is(MemberRole.OWNER));
            assertThat(result.get(1).getRole(), Matchers.is(MemberRole.ADMIN));
            assertThat(result.get(2).getRole(), Matchers.is(MemberRole.DEFAULT));
        });
    }

    @Test
    void whenGetChatMembers_givenActorIsNotChatMember_thenThrowException() {
        // given
        String chatId = "qwer-1234";
        String actorId = "1234-qwer";

        User actor = User.builder().id(actorId).build();
        MemberId memberId = new MemberId(actorId, chatId);

        // when
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // then
        assertThrows(RuntimeException.class, () -> memberService.getChatMembers(chatId, actor));
        verify(memberRepository).findById(memberId);
    }

    @Test
    void whenAddChatMember_givenValidRequest_thenAddNewChatMember() {
        // given
        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String userId = "qwre-1234";
        UserId userIdDto = new UserId(userId);

        User user = User.builder().id(userId).build();
        MemberId userMemberId = new MemberId(userId, chatId);

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));
        when(memberRepository.findById(userMemberId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(memberRepository.save(any(Member.class))).then(returnsFirstArg());

        MemberDto result = memberService.addChatMember(chatId, userIdDto, actor);

        // then
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);
        verify(userRepository).findById(userId);
        verify(memberRepository).save(any(Member.class));

        assertThat(result.getUser().getId(), Matchers.is(user.getId()));
        assertThat(result.getRole(), Matchers.is(MemberRole.DEFAULT));
        assertThat(result.getJoinedAt(), Matchers.notNullValue());
    }

    @Test
    void whenAddChatMember_givenActorIsNotMemberOfChat_thenThrowException() {
        // given
        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        String chatId = "2134-abcd";

        String userId = "qwre-1234";
        UserId userIdDto = new UserId(userId);

        MemberId actorMemberId = new MemberId(actorId, chatId);

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.empty());

        // then
        assertThrows(RuntimeException.class, () -> memberService.addChatMember(chatId, userIdDto, actor));
        verify(memberRepository).findById(actorMemberId);
    }

    @Test
    void whenAddChatMember_givenUserIsAlreadyMemberOfChat_thenThrowException() {
        // given
        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String userId = "qwre-1234";
        UserId userIdDto = new UserId(userId);

        User user = User.builder().id(userId).build();
        MemberId userMemberId = new MemberId(userId, chatId);
        Member userMember = Member.builder().id(userMemberId).user(user).chat(chat).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));
        when(memberRepository.findById(userMemberId)).thenReturn(Optional.of(userMember));

        // then
        assertThrows(RuntimeException.class, () -> memberService.addChatMember(chatId, userIdDto, actor));
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);
    }

    @Test
    void whenAddChatMember_givenUserDoesntExist_thenThrowException() {
        // given
        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String userId = "qwre-1234";
        UserId userIdDto = new UserId(userId);
        MemberId userMemberId = new MemberId(userId, chatId);

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));
        when(memberRepository.findById(userMemberId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // then
        assertThrows(RuntimeException.class, () -> memberService.addChatMember(chatId, userIdDto, actor));
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);
        verify(userRepository).findById(userId);
    }
}