package org.civlabs.civlabsschedulebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Room {
    @Getter
    UUID id;
    List<Player> players = new ArrayList<>();

    public Room(UUID id) {
        this.id = id;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public void addPlayer(Player player) {
        if (players.contains(player) || players.size() >= 2) return;
        players.add(player);
    }
}
