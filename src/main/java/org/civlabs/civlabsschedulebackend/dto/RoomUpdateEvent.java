package org.civlabs.civlabsschedulebackend.dto;

public record RoomUpdateEvent(
    RoomUpdateType updateType,
    Room room
) {}
