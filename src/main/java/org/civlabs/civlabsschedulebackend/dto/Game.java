package org.civlabs.civlabsschedulebackend.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Game {
    GameType currentGame;
    Player winner;
    GameType nextGame;
}
