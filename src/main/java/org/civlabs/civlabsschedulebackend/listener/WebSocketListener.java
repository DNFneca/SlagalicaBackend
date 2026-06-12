package org.civlabs.civlabsschedulebackend.listener;

import org.civlabs.civlabsschedulebackend.service.RoomSocketService;
import org.civlabs.civlabsschedulebackend.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketListener {

    private final RoomSocketService roomSocketService;

    public WebSocketListener(RoomSocketService roomSocketService) {
        this.roomSocketService = roomSocketService;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            return;
        }

        roomSocketService.handleDisconnect(sessionId);
    }
}