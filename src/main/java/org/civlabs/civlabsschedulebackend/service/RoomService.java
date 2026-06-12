package org.civlabs.civlabsschedulebackend.service;

import org.civlabs.civlabsschedulebackend.dto.Player;
import org.civlabs.civlabsschedulebackend.dto.Room;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoomService {

    private final Map<UUID, Room> rooms = new HashMap<>(0);

    public Room getRoom(UUID id) {
        return rooms.get(id);
    }

    public Room getRoomPlayerIsIn(Long id) {
        for (Room room : rooms.values()) {
            for (Player player : room.getPlayers()) {
                if (player.getId().equals(id)) {
                    return room;
                }
            }
        }
        return null;
    }

    public Room joinRoom(UUID id, Player player) {
        Room room = rooms.get(id);
        if (room == null) return null;
        room.addPlayer(player);
        return room;
    }

    public Room newRoom(Player player) {
        UUID uuid = UUID.randomUUID();
        Room room = new Room(uuid);
        rooms.put(uuid, room);
        return room;
    }
}
