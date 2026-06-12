package org.civlabs.civlabsschedulebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.civlabs.civlabsschedulebackend.entity.User;

@Getter
@Setter
@AllArgsConstructor
public class Player {
    Long id;
    String name;
    boolean canStop;
    int points;
    boolean isOnline;
    boolean ready;

    public Player(Long id, String name) {
        this.id = id;
        this.name = name;
        this.canStop = false;
        this.points = 0;
        this.ready = false;
        this.isOnline = true;
    }

    public static Player playerFromUser(User user) {
        return new Player(user.getId(), user.getUsername());
    }
}
