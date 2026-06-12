package org.civlabs.civlabsschedulebackend.dto;

public record PlayerJoinedEvent(
        String type,
        String roomId,
        String username,
        Room room
) {}