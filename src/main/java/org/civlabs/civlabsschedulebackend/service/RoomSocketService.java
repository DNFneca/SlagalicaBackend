package org.civlabs.civlabsschedulebackend.service;

import org.civlabs.civlabsschedulebackend.dto.PlayerDisconnectedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    private final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUsername = new ConcurrentHashMap<>();

    public RoomSocketService(
            SimpMessagingTemplate messagingTemplate,
            RoomService roomService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
    }

    public void registerSession(String sessionId, String username, String roomId) {
        sessionToRoom.put(sessionId, roomId);
        sessionToUsername.put(sessionId, username);
    }

    public void handleDisconnect(String sessionId) {
        String roomId = sessionToRoom.remove(sessionId);
        String username = sessionToUsername.remove(sessionId);

        if (roomId == null || username == null) {
            return;
        }

        System.out.println(username + " disconnected from room " + roomId);

        PlayerDisconnectedEvent event = new PlayerDisconnectedEvent(
                "PLAYER_DISCONNECTED",
                roomId,
                username
        );

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/createRoom",
                event
        );
    }
}