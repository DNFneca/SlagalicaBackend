package org.civlabs.civlabsschedulebackend.interceptor;

import lombok.SneakyThrows;
import org.civlabs.civlabsschedulebackend.service.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);


        if (accessor == null) return message;

        // Only act on CONNECT — this is where the principal is established
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Authentication auth = (Authentication) sessionAttributes.get("auth");
                if (auth != null) {
                    accessor.setUser(auth);   // ← this is the key line
                } else {
                    try {
                        throw new AccessDeniedException("No authentication found");
                    } catch (AccessDeniedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return message;
    }
}