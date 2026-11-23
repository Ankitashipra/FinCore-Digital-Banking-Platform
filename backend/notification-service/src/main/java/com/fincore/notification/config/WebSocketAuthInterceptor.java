package com.fincore.notification.config;

import com.fincore.notification.service.JwtService;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

        if (accessor != null
                && StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authorization =
                    accessor.getFirstNativeHeader("Authorization");

            if (authorization == null
                    || !authorization.startsWith("Bearer ")) {
                throw new IllegalArgumentException(
                        "WebSocket authentication is required");
            }

            UUID userId = jwtService.extractUserId(
                    authorization.substring(7));

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId.toString(),
                            null,
                            List.of(new SimpleGrantedAuthority(
                                    "ROLE_CUSTOMER"))
                    );

            accessor.setUser(authentication);
        }
        if (accessor != null
        && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

    String destination = accessor.getDestination();

    if (!"/user/queue/notifications".equals(destination)) {
        throw new IllegalArgumentException(
                "Subscription destination is not permitted");
    }

    if (accessor.getUser() == null) {
        throw new IllegalArgumentException(
                "Authentication is required");
    }
}

if (accessor != null
        && StompCommand.SEND.equals(accessor.getCommand())) {
    throw new IllegalArgumentException(
            "Client messages are not permitted");
}
        return message;
    }
}