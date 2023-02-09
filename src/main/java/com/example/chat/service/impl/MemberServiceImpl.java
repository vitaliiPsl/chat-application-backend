package com.example.chat.service.impl;

import com.example.chat.exception.ForbiddenException;
import com.example.chat.model.chat.member.Member;
import com.example.chat.model.chat.member.MemberId;
import com.example.chat.model.chat.member.MemberRole;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MemberDto;
import com.example.chat.payload.chat.UserId;
import com.example.chat.repository.MemberRepository;
import com.example.chat.service.MemberService;
import com.example.chat.service.UserService;
import com.example.chat.utils.PayloadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final UserService userService;
    private final PayloadMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public boolean isMemberOfTheChat(String userId, String chatId) {
        log.debug("Verify if user {} is a member of the chat {}", userId, chatId);

        MemberId id = new MemberId(userId, chatId);
        Optional<Member> member = memberRepository.findById(id);

        return member.isPresent();
    }

    @Override
    public Member getMemberDomainObject(String userId, String chatId) {
        log.debug("Get chat member as domain object. User id {}. Chat id {}", userId, chatId);

        MemberId id = new MemberId(userId, chatId);
        Optional<Member> member = memberRepository.findById(id);
        if (member.isEmpty()) {
            log.error("User {} is not a member of chat {}", userId, chatId);
            throw new ForbiddenException("Not a member of the chat");
        }

        return member.get();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MemberDto> getChatMembers(String chatId, User actor) {
        log.debug("Get members of the chat {} sorted by their role", chatId);

        if (!isMemberOfTheChat(actor.getId(), chatId)) {
            log.error("User {} is not a member of chat {}", actor.getId(), chatId);
            throw new ForbiddenException("Not a member of the chat");
        }

        List<Member> members = memberRepository.findByChat_Id(chatId);
        return members.stream()
                .sorted(Comparator.comparing(member -> member.getRole().ordinal()))
                .map(mapper::mapMemberToMemberDto).collect(Collectors.toList());
    }

    @Override
    public MemberDto addChatMember(String chatId, UserId userId, User actor) {
        log.debug("Add new user with id {} to the chat {}", userId, chatId);

        Member actorMember = getMemberDomainObject(actor.getId(), chatId);

        if (isMemberOfTheChat(userId.getId(), chatId)) {
            log.error("User {} is already a member of the chat {}", userId.getId(), chatId);
            throw new IllegalStateException("User is already a member of the chat");
        }

        User user = userService.getUserDomainObject(userId.getId());
        Member member = new Member(user, actorMember.getChat(), MemberRole.DEFAULT);

        member = memberRepository.save(member);
        return mapper.mapMemberToMemberDto(member);
    }

    @Override
    public MemberDto updateChatMember(String chatId, String userId, MemberDto memberDto, User actor) {
        log.debug("Update member {} of the chat {}. Update details: {}", userId, chatId, memberDto);

        Member actorMember = getMemberDomainObject(actor.getId(), chatId);
        if (actorMember.getRole() != MemberRole.OWNER) {
            log.error("Only the owner of the chat can update roles of other members");
            throw new ForbiddenException("Only the owner of the chat can update roles of other members");
        }

        // there must be an owner of the chat, so the owners cannot change their role
        if (actor.getId().equals(userId)) {
            log.error("Owners cannot change their own role");
            throw new IllegalStateException("Owners cannot change their role, as there must be an owner of the chat.");
        }

        Member member = getMemberDomainObject(userId, chatId);
        // if the owner is trying to give ownership over the chat to someone else,
        // then promote that member and demote current owner
        if (memberDto.getRole() == MemberRole.OWNER) {
            actorMember.setRole(MemberRole.ADMIN);
            actorMember.setUpdatedAt(LocalDateTime.now());
        }

        member.setRole(memberDto.getRole());
        member.setUpdatedAt(LocalDateTime.now());

        return mapper.mapMemberToMemberDto(member);
    }

    @Override
    public void removeChatMember(String chatId, String userId, User actor) {
        log.debug("Remove member {} from chat {}", userId, chatId);

        Member actorMember = getMemberDomainObject(actor.getId(), chatId);
        if (actor.getId().equals(userId)) {
            leaveChat(actorMember);
        } else {
            removeMember(chatId, userId, actorMember);
        }
    }

    @Override
    public MemberDto getChatMember(String chatId, String userId, User actor) {
        log.debug("Get member {} of the chat {}", userId, chatId);

        if (!isMemberOfTheChat(actor.getId(), chatId)) {
            log.error("User {} is not a member of chat {}", actor.getId(), chatId);
            throw new ForbiddenException("Not a member of the chat");
        }

        Member member = getMemberDomainObject(userId, chatId);
        return mapper.mapMemberToMemberDto(member);
    }

    private void leaveChat(Member member) {
        if (member.getRole() == MemberRole.OWNER) {
            log.error("Owner must assign ownership over the chat to another member before leaving");
            throw new IllegalStateException("Owner must assign ownership over the chat to another member before leaving");
        }

        memberRepository.delete(member);
    }

    private void removeMember(String chatId, String userId, Member actorMember) {
        if (actorMember.getRole() == MemberRole.DEFAULT) {
            log.error("Only the owner and admins can remove other users");
            throw new ForbiddenException("Only the owner and admins can remove other users");
        }

        Member member = getMemberDomainObject(userId, chatId);
        if (member.getRole() == MemberRole.OWNER) {
            log.error("No one can remove the owner of the chat");
            throw new ForbiddenException("No one can remove the owner of the chat");
        }

        if (member.getRole() == MemberRole.ADMIN && actorMember.getRole() != MemberRole.OWNER) {
            log.error("Only owner can remove admin");
            throw new ForbiddenException("Only owner can remove admin");
        }

        memberRepository.delete(member);
    }
}
