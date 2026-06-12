package org.civlabs.civlabsschedulebackend.dto.event;

import org.civlabs.civlabsschedulebackend.dto.Player;
import org.civlabs.civlabsschedulebackend.dto.RoomUpdateType;

public record NewPlayerEvent(
        RoomUpdateType updateType,
        String roomId,
        Player player
) {}