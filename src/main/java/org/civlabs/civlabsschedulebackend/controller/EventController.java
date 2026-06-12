package org.civlabs.civlabsschedulebackend.controller;

import lombok.AllArgsConstructor;
import org.civlabs.civlabsschedulebackend.dto.PageRequest;
import org.civlabs.civlabsschedulebackend.dto.SimplePageRequest;
import org.civlabs.civlabsschedulebackend.entity.Event;
import org.civlabs.civlabsschedulebackend.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/events")
class EventController {

    private final EventService eventService;


    @GetMapping("test")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> test() {
        System.out.println("test");
        return ResponseEntity.ok().body("why are you here??");
    }

    @PostMapping("/latest")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Page<Event> getLatestEvents(
            @RequestBody PageRequest pageRequest
    ) {
        return eventService.getLatestEvents(pageRequest);
    }

    @PostMapping("/live")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Page<Event> getLiveEvents(
            @RequestBody SimplePageRequest pageRequest
    ) {
        return eventService.getLiveEvents(pageRequest);
    }

    @PostMapping("/future")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Page<Event> getFutureEvents(
            @RequestBody SimplePageRequest pageRequest
    ) {
        return eventService.getFutureEvents(pageRequest);
    }

    @PostMapping("/past")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Page<Event> getPastEvents(
            @RequestBody SimplePageRequest pageRequest
    ) {
        return eventService.getPastEvents(pageRequest);
    }
}
