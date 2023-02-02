package com.example.chat.service.impl;

import com.example.chat.exception.ResourceNotFoundException;
import com.example.chat.model.chat.Chat;
import com.example.chat.model.chat.member.Member;
import com.example.chat.model.chat.member.MemberId;
import com.example.chat.model.chat.member.MemberRole;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MemberDto;
import com.example.chat.payload.chat.UserId;
import com.example.chat.repository.MemberRepository;
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
    UserService userService;
    PayloadMapper mapper;
    MemberServiceImpl memberService;

    @BeforeEach
    void init() {
        // payload mapper requires model mapper
        ModelMapper modelMapper = Mockito.spy(ModelMapper.class);
        mapper = Mockito.spy(new PayloadMapper(modelMapper));

        memberService = new MemberServiceImpl(memberRepository, userService, mapper);
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
        when(userService.getUserDomainObject(userId)).thenReturn(user);
        when(memberRepository.save(any(Member.class))).then(returnsFirstArg());

        MemberDto result = memberService.addChatMember(chatId, userIdDto, actor);

        // then
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);
        verify(userService).getUserDomainObject(userId);
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
        when(userService.getUserDomainObject(userId)).thenThrow(new ResourceNotFoundException(userId, User.class));

        // then
        assertThrows(RuntimeException.class, () -> memberService.addChatMember(chatId, userIdDto, actor));
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);
        verify(userService).getUserDomainObject(userId);
    }

    @ParameterizedTest
    @EnumSource(value = MemberRole.class, names = {"DEFAULT", "ADMIN"})
    void whenUpdateChatMember_givenValidRequest_thenAddNewChatMember(MemberRole role) {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(MemberRole.OWNER).build();

        String userId = "qwre-1234";
        User user = User.builder().id(userId).build();

        MemberId userMemberId = new MemberId(userId, chatId);
        Member userMember = Member.builder().id(userMemberId).user(user).chat(chat).role(MemberRole.DEFAULT).build();

        MemberDto memberDto = MemberDto.builder().role(role).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));
        when(memberRepository.findById(userMemberId)).thenReturn(Optional.of(userMember));

        MemberDto result = memberService.updateChatMember(chatId, userId, memberDto, actor);

        // then
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);

        assertThat(result.getRole(), Matchers.is(role));
        assertThat(result.getUpdatedAt(), Matchers.notNullValue());
    }

    @Test
    void whenUpdateChatMember_givenUpdateUserToOwner_thenDemoteCurrentOwner() {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(MemberRole.OWNER).build();

        String userId = "qwre-1234";
        User user = User.builder().id(userId).build();

        MemberId userMemberId = new MemberId(userId, chatId);
        Member userMember = Member.builder().id(userMemberId).user(user).chat(chat).role(MemberRole.DEFAULT).build();

        MemberDto memberDto = MemberDto.builder().role(MemberRole.OWNER).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));
        when(memberRepository.findById(userMemberId)).thenReturn(Optional.of(userMember));

        MemberDto result = memberService.updateChatMember(chatId, userId, memberDto, actor);

        // then
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);

        assertThat(actorMember.getRole(), Matchers.is(MemberRole.ADMIN));
        assertThat(userMember.getRole(), Matchers.is(MemberRole.OWNER));
        assertThat(result.getRole(), Matchers.is(MemberRole.OWNER));
        assertThat(result.getUpdatedAt(), Matchers.notNullValue());
    }

    @Test
    void whenUpdateChatMember_givenActorIsNotChatMember_thenThrowException() {
        // given
        String chatId = "2134-abcd";

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);

        String userId = "qwre-1234";
        MemberDto memberDto = MemberDto.builder().role(MemberRole.ADMIN).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.empty());

        // then
        assertThrows(RuntimeException.class, () -> memberService.updateChatMember(chatId, userId, memberDto, actor));
        verify(memberRepository).findById(actorMemberId);
    }

    @Test
    void whenUpdateChatMember_givenActorIsNotTheOwner_thenThrowException() {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(MemberRole.ADMIN).build();

        String userId = "qwre-1234";

        MemberDto memberDto = MemberDto.builder().role(MemberRole.ADMIN).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));

        // then
        assertThrows(RuntimeException.class, () -> memberService.updateChatMember(chatId, userId, memberDto, actor));
        verify(memberRepository).findById(actorMemberId);
    }

    @Test
    void whenUpdateChatMember_givenOwnerTriesToUpdateOwnRole_thenThrowException() {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(MemberRole.OWNER).build();

        MemberDto memberDto = MemberDto.builder().role(MemberRole.ADMIN).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));

        // then
        assertThrows(RuntimeException.class, () -> memberService.updateChatMember(chatId, actorId, memberDto, actor));
        verify(memberRepository).findById(actorMemberId);
    }

    @ParameterizedTest
    @EnumSource(value = MemberRole.class, names = {"ADMIN", "OWNER"})
    void whenRemoveChatMember_givenUserIsDefaultMemberAndActorIsTheOwnerOrAdmin_thenRemoveMember(MemberRole role) {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(role).build();

        String userId = "qwre-1234";
        User user = User.builder().id(userId).build();

        MemberId userMemberId = new MemberId(userId, chatId);
        Member userMember = Member.builder().id(userMemberId).user(user).chat(chat).role(MemberRole.DEFAULT).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));
        when(memberRepository.findById(userMemberId)).thenReturn(Optional.of(userMember));

        memberService.removeChatMember(chatId, userId, actor);

        // then
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);
        verify(memberRepository).delete(userMember);
    }

    @ParameterizedTest
    @EnumSource(value = MemberRole.class, names = {"ADMIN", "DEFAULT"})
    void whenRemoveChatMember_givenUserToRemoveIsActorAndIsNotOwner_thenRemoveChat(MemberRole role) {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(role).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));

        memberService.removeChatMember(chatId, actorId, actor);

        // then
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).delete(actorMember);
    }

    @Test
    void whenRemoveChatMember_givenUserToRemoveIsActorWithRoleOwner_thenThrowException() {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(MemberRole.OWNER).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));

        // then
        assertThrows(RuntimeException.class, () -> memberService.removeChatMember(chatId, actorId, actor));
        verify(memberRepository).findById(actorMemberId);
    }

    @Test
    void whenRemoveChatMember_givenActorIsNotMemberOfChat_thenThrowException() {
        // given
        String chatId = "2134-abcd";

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();
        MemberId actorMemberId = new MemberId(actorId, chatId);

        String userId = "qwre-1234";

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.empty());

        // then
        assertThrows(RuntimeException.class, () -> memberService.removeChatMember(chatId, userId, actor));
        verify(memberRepository).findById(actorMemberId);
    }

    @Test
    void whenRemoveChatMember_givenActorIsDefaultUser_thenThrowException() {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(MemberRole.DEFAULT).build();

        String userId = "qwre-1234";

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));

        // then
        assertThrows(RuntimeException.class, () -> memberService.removeChatMember(chatId, userId, actor));
        verify(memberRepository).findById(actorMemberId);
    }

    @Test
    void whenRemoveChatMember_givenUserIsNotChatMember_thenThrowException() {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(MemberRole.OWNER).build();

        String userId = "qwre-1234";

        MemberId userMemberId = new MemberId(userId, chatId);

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));
        when(memberRepository.findById(userMemberId)).thenReturn(Optional.empty());

        // then
        assertThrows(RuntimeException.class, () -> memberService.removeChatMember(chatId, userId, actor));
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);
    }

    @Test
    void whenRemoveChatMember_givenUserMemberIsTheOwner_thenThrowException() {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(MemberRole.ADMIN).build();

        String userId = "qwre-1234";
        User user = User.builder().id(userId).build();

        MemberId userMemberId = new MemberId(userId, chatId);
        Member userMember = Member.builder().id(userMemberId).user(user).chat(chat).role(MemberRole.OWNER).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));
        when(memberRepository.findById(userMemberId)).thenReturn(Optional.of(userMember));

        // then
        assertThrows(RuntimeException.class, () -> memberService.removeChatMember(chatId, userId, actor));
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);
    }

    @Test
    void whenRemoveChatMember_givenUserMemberIsAdminAndActorMemberIsNotTheOwner_thenThrowException() {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(MemberRole.ADMIN).build();

        String userId = "qwre-1234";
        User user = User.builder().id(userId).build();

        MemberId userMemberId = new MemberId(userId, chatId);
        Member userMember = Member.builder().id(userMemberId).user(user).chat(chat).role(MemberRole.ADMIN).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));
        when(memberRepository.findById(userMemberId)).thenReturn(Optional.of(userMember));

        // then
        assertThrows(RuntimeException.class, () -> memberService.removeChatMember(chatId, userId, actor));
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);
    }

    @Test
    void whenGetChatMember_givenValidRequest_thenReturnMember() {
        // given
        String chatId = "2134-abcd";
        Chat chat = Chat.builder().id(chatId).name("Test").build();

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);
        Member actorMember = Member.builder().id(actorMemberId).user(actor).chat(chat).role(MemberRole.OWNER).build();

        String userId = "qwre-1234";
        User user = User.builder().id(userId).build();

        MemberId userMemberId = new MemberId(userId, chatId);
        Member userMember = Member.builder().id(userMemberId).user(user).chat(chat).role(MemberRole.DEFAULT).build();

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.of(actorMember));
        when(memberRepository.findById(userMemberId)).thenReturn(Optional.of(userMember));

        MemberDto result = memberService.getChatMember(chatId, userId, actor);

        // then
        verify(memberRepository).findById(actorMemberId);
        verify(memberRepository).findById(userMemberId);

        assertThat(result.getUser().getId(), Matchers.is(userId));
    }


    @Test
    void whenGetChatMember_givenActorIsNotChatMember_thenThrowError() {
        // given
        String chatId = "2134-abcd";

        String actorId = "1234-qwer";
        User actor = User.builder().id(actorId).build();

        MemberId actorMemberId = new MemberId(actorId, chatId);

        String userId = "qwre-1234";

        // when
        when(memberRepository.findById(actorMemberId)).thenReturn(Optional.empty());

        // then
        assertThrows(RuntimeException.class, () -> memberService.getChatMember(chatId, userId, actor));
        verify(memberRepository).findById(actorMemberId);
    }
}