package com.example.chat.config.websocket;

import com.example.chat.model.user.User;
import com.example.chat.service.JwtService;
import com.example.chat.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final MemberService memberService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            log.error("Stomp header accessor is null");
            throw new IllegalArgumentException("Stomp header accessor is null");
        }

        message = verifyAuthorization(message, accessor);
        return message;
    }

    private Message<?> verifyAuthorization(Message<?> message, StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        log.debug(authorization);

        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            log.error("Unauthorized");
            throw new IllegalStateException("Unauthorized");
        }

        String token = authorization.replace("Bearer ", "");
        Authentication authentication = jwtService.verifyToken(token);
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            verifyAccessToTopic((User) authentication.getPrincipal(), accessor.getDestination());
        }

        accessor.setUser(authentication);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    private void verifyAccessToTopic(User user, String destination) {
        String chatTopicPrefix = "/topic/chats/";

        // check if the user tries to subscribe to a chat topic
        if (!destination.startsWith(chatTopicPrefix)) {
            return;
        }

        // get id of the chat
        destination = destination.replace(chatTopicPrefix, "");
        String[] resources = destination.split("/");
        if (resources.length == 0) {
            return;
        }

        // verify that the user is a member of the chat
        String chatId = resources[0];
        if(!memberService.isMemberOfTheChat(user.getId(), chatId)) {
            log.error("User {} is not a member of the chat {}", user.getId(), chatId);
            throw new IllegalStateException("Not a member of the chat");
        }
    }
}
