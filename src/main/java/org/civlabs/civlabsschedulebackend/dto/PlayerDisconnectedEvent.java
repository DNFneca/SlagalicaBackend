package org.civlabs.civlabsschedulebackend.dto;

public record PlayerDisconnectedEvent(
        String type,
        String roomId,
        String username
) {}