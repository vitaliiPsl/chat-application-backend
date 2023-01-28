package com.example.chat.controller;

import com.example.chat.model.user.User;
import com.example.chat.payload.chat.MemberDto;
import com.example.chat.payload.chat.UserId;
import com.example.chat.service.MemberService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chats")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("{chatId}/members")
    List<MemberDto> getChatMembers(
            @PathVariable String chatId,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Get members of the chat {}", chatId);

        return memberService.getChatMembers(chatId, actor);
    }

    @GetMapping("{chatId}/members/{userId}")
    MemberDto getChatMember(
            @PathVariable String chatId,
            @PathVariable String userId,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Get member {} of the chat {}", userId, chatId);

        return memberService.getChatMember(chatId, userId, actor);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("{chatId}/members")
    MemberDto addChatMember(
            @PathVariable String chatId,
            @RequestBody @Validated UserId userId,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Add new member {} to the chat {}", userId, chatId);

        return memberService.addChatMember(chatId, userId, actor);
    }

    @PutMapping("{chatId}/members/{memberId}")
    MemberDto updateChatMember(
            @PathVariable String chatId,
            @PathVariable String memberId,
            @RequestBody @Valid MemberDto memberDto,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Update member {} of the chat {}. Update details: {}", memberId, chatId, memberDto);

        return memberService.updateChatMember(chatId, memberId, memberDto, actor);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{chatId}/members/{memberId}")
    void updateChatMember(
            @PathVariable String chatId,
            @PathVariable String memberId,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Remove member {} of the chat {}", memberId, chatId);

        memberService.removeChatMember(chatId, memberId, actor);
    }
}
