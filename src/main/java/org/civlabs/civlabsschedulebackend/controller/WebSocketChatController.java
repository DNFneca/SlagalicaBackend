package org.civlabs.civlabsschedulebackend.controller;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.civlabs.civlabsschedulebackend.dto.*;
import org.civlabs.civlabsschedulebackend.dto.event.NewPlayerEvent;
import org.civlabs.civlabsschedulebackend.entity.User;
import org.civlabs.civlabsschedulebackend.service.RoomService;
import org.civlabs.civlabsschedulebackend.service.RoomSocketService;
import org.civlabs.civlabsschedulebackend.service.TaskService;
import org.civlabs.civlabsschedulebackend.service.UserService;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Controller
@AllArgsConstructor
public class WebSocketChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    TaskService taskService;
    private final UserService userService;
    RoomService roomService;
    RoomSocketService roomSocketService;

    @MessageMapping("/createRoom")
    @SendToUser("/queue/createRoom")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String createRoom(Principal principal,
                             @Header("simpSessionId") String sessionId,
                             @Header("simpSessionAttributes") Map<String, Object> attrs) {

        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();


        User byUsername = userService.findByUsername(username);


        Room room = roomService.newRoom(new Player(byUsername.getId(), byUsername.getUsername()));
        room.addPlayer(Player.playerFromUser(byUsername));
        RoomCreated roomCreated = new RoomCreated(room.getId().toString(), room);
        roomSocketService.registerSession(sessionId, username, room.getId().toString());


        return new Gson().toJson(roomCreated);
    }

    @MessageMapping("/joinRoom")
    @SendToUser("/queue/joinRoom")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String joinRoom(Principal principal,
                           @Header("simpSessionId") String sessionId,
                           @Header("simpSessionAttributes") Map<String, Object> attrs,
                           @Payload String roomId) {

        roomId = roomId.replace("\"", "");

        Room room = roomService.getRoom(UUID.fromString(roomId));


        String username = principal.getName();
        Player joiningPlayer = Player.playerFromUser(userService.findByUsername(username));
        room.addPlayer(joiningPlayer);
        List<Player> players = roomService.getRoom(UUID.fromString(roomId)).getPlayers();
        for (Player player : players) {
            if (player.getId().equals(joiningPlayer.getId())) continue;
            simpMessagingTemplate.convertAndSendToUser(player.getName(), "/queue/room", new NewPlayerEvent(RoomUpdateType.NEW_PLAYER, roomId, joiningPlayer));
        }

        roomSocketService.registerSession(sessionId, username, roomId);

        RoomCreated roomCreated = new RoomCreated(room.getId().toString(), room);
        return new Gson().toJson(roomCreated);
    }


    @MessageMapping("/ready")
    @SendToUser("/queue/room")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String readyToStart(Principal principal,
                           @Header("simpSessionId") String sessionId,
                           @Header("simpSessionAttributes") Map<String, Object> attrs) {

        String username = principal.getName();
        Player playerReadyingUp = Player.playerFromUser(userService.findByUsername(username));

        Room room = roomService.getRoomPlayerIsIn(playerReadyingUp.getId());
        List<Player> players = room.getPlayers();


        if (playerReadyingUp.isReady()) return "Already Ready";

        for (Player player : players) {
            if (player.getId().equals(playerReadyingUp.getId())) {
                player.setReady(true);
                continue;
            }
            simpMessagingTemplate.convertAndSendToUser(player.getName(), "/queue/room", new NewPlayerEvent(RoomUpdateType.PLAYER_READY, room.getId().toString(), playerReadyingUp));
        }


        return "Success";
    }

    @MessageMapping("/start")
    @SendToUser("/queue/room")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String start(Principal principal,
                               @Header("simpSessionId") String sessionId,
                               @Header("simpSessionAttributes") Map<String, Object> attrs) {

        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Player playerReadyingUp = Player.playerFromUser(userService.findByUsername(username));

        Room room = roomService.getRoomPlayerIsIn(playerReadyingUp.getId());
        List<Player> players = room.getPlayers();


        double random = Math.random();


        for (Player player : players) {
            if (!player.isReady()) return "Can't Start";
        }

        for (Player player : players) {
            if (player.getId().equals(playerReadyingUp.getId())) continue;
            simpMessagingTemplate.convertAndSendToUser(player.getName(), "/queue/room", new NewPlayerEvent(RoomUpdateType.START, room.getId().toString(), random > 0.5 ? players.getLast() : players.getFirst()));
        }

        return new Gson().toJson(new NewPlayerEvent(RoomUpdateType.START, room.getId().toString(), random > 0.5 ? players.getLast() : players.getFirst()));
    }




    @MessageMapping("/chat.adminCommand")
    @SendTo("/topic/admin")
    @PreAuthorize("hasAuthority('SCOPE_admin:write')") // Restricts access to specific OAuth scopes or authorities
    public String adminCommand(String command) {
        return "Admin executed: " + command;
    }
}
