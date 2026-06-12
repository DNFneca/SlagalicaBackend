package org.civlabs.civlabsschedulebackend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final SimpMessagingTemplate messagingTemplate;

    // Spring automatically injects this template
    public TaskService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void updateTaskStatus(Long taskId, String newStatus) {
        // 1. Perform your actual backend business logic (e.g., save to Database)
        System.out.println("Updating task " + taskId + " to " + newStatus);

        // 2. Create the payload you want to send to the frontend
        String payload = String.format("{\"taskId\": %d, \"status\": \"%s\"}", taskId, newStatus);

        // 3. Push it directly to the WebSocket broker lane
        // Your Angular app is already subscribed to /topic/public, so it will instantly catch this!
        messagingTemplate.convertAndSend("/room/public", payload);
    }
}