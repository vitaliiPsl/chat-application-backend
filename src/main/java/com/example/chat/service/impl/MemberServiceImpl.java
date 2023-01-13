package com.example.chat.service.impl;

import com.example.chat.model.chat.member.Member;
import com.example.chat.model.chat.member.MemberId;
import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MemberDto;
import com.example.chat.repository.MemberRepository;
import com.example.chat.service.MemberService;
import com.example.chat.utils.PayloadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PayloadMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public boolean isMemberOfTheChat(String userId, String chatId) {
        log.debug("Verify if user {} is a member of the chat {}", userId, chatId);

        MemberId id = new MemberId(userId, chatId);
        Optional<Member> member = memberRepository.findById(id);

        return member.isPresent();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MemberDto> getChatMembers(String chatId, User actor) {
        log.debug("Get members of the chat {} sorted by their role", chatId);

        if (!isMemberOfTheChat(actor.getId(), chatId)) {
            log.error("User {} is not a member of chat {}", actor.getId(), chatId);
            throw new IllegalStateException("Not a member of the chat");
        }

        List<Member> members = memberRepository.findByChat_Id(chatId);
        return members.stream()
                .sorted(Comparator.comparing(member -> member.getRole().ordinal()))
                .map(mapper::mapMemberToMemberDto).collect(Collectors.toList());
    }
}
