package com.example.chat.controller;

import com.example.chat.model.user.User;
import com.example.chat.payload.chat.ChatDto;
import com.example.chat.payload.chat.MemberDto;
import com.example.chat.payload.chat.MessageDto;
import com.example.chat.payload.chat.UserId;
import com.example.chat.payload.groups.CreateRequest;
import com.example.chat.service.ChatService;
import com.example.chat.service.MemberService;
import com.example.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
public class ChatController {
    private final ChatService chatService;
    private final MemberService memberService;
    private final MessageService messageService;

    @GetMapping
    List<ChatDto> getChatsByUserId(
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Get chats of current user");

        return chatService.getChatsOfActor(actor);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    ChatDto createChat(
            @RequestBody @Validated(CreateRequest.class) ChatDto chatDto,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Create new chat: {}", chatDto);

        return chatService.createChat(chatDto, actor);
    }

    @PutMapping("{chatId}")
    ChatDto createChat(
            @PathVariable String chatId,
            @RequestBody @Valid ChatDto chatDto,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Update chat {}. Update details: {}", chatId, chatDto);

        return chatService.updateChat(chatId, chatDto, actor);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{chatId}")
    void createChat(
            @PathVariable String chatId,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Delete chat {}", chatId);

        chatService.deleteChat(chatId, actor);
    }

    @GetMapping("{chatId}")
    ChatDto getChat(
            @PathVariable String chatId,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Get chat {}", chatId);

        return chatService.getChat(chatId, actor);
    }

    @GetMapping("{chatId}/members")
    List<MemberDto> getChatMembers(
            @PathVariable String chatId,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Get members of the chat {}", chatId);

        return memberService.getChatMembers(chatId, actor);
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

    @GetMapping("{chatId}/messages")
    Page<MessageDto> getChatMessages(
            @PathVariable String chatId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Get {} messages of the chat {} with id less than {}", limit, chatId, lastId);

        return messageService.getMessages(chatId, lastId, limit, actor);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("{chatId}/messages")
    MessageDto saveChatMessage(
            @PathVariable String chatId,
            @RequestBody @Valid MessageDto messageDto,
            @Parameter(hidden = true) @AuthenticationPrincipal User actor
    ) {
        log.debug("Save message {} sent to the chat {}", messageDto, chatId);

        return messageService.saveMessage(chatId, messageDto, actor);
    }
}
