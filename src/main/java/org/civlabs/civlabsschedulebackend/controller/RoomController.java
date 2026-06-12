package org.civlabs.civlabsschedulebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.civlabs.civlabsschedulebackend.dto.Player;
import org.civlabs.civlabsschedulebackend.dto.Room;
import org.civlabs.civlabsschedulebackend.dto.RoomUpdateType;
import org.civlabs.civlabsschedulebackend.dto.UserDTO;
import org.civlabs.civlabsschedulebackend.dto.event.NewPlayerEvent;
import org.civlabs.civlabsschedulebackend.service.RoomService;
import org.civlabs.civlabsschedulebackend.service.RoomSocketService;
import org.civlabs.civlabsschedulebackend.service.TaskService;
import org.civlabs.civlabsschedulebackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/room")
public class RoomController {

    private final UserService userService;
    RoomService roomService;

    @GetMapping("getOpponent")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Player> getOpponent(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Player playerReadyingUp = Player.playerFromUser(userService.findByUsername(username));

        Room room = roomService.getRoomPlayerIsIn(playerReadyingUp.getId());
        List<Player> players = room.getPlayers();

        
        return ResponseEntity.ok(players.getFirst() == playerReadyingUp ? players.getLast() : players.getFirst());

    }
}
