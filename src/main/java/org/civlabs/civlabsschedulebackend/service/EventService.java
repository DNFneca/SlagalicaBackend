package org.civlabs.civlabsschedulebackend.service;

import org.civlabs.civlabsschedulebackend.dto.PageRequest;
import org.civlabs.civlabsschedulebackend.dto.SimplePageRequest;
import org.civlabs.civlabsschedulebackend.entity.Event;
import org.civlabs.civlabsschedulebackend.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class EventService {

    private final EventRepository eventRepository;

    EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Page<Event> getLatestEvents(PageRequest pageRequest) {
        Sort sort = pageRequest.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(pageRequest.getSortBy()).ascending()
                : Sort.by(pageRequest.getSortBy()).descending();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                sort
        );

        return eventRepository.findAll(pageable);
    }

    public Page<Event> getLiveEvents(SimplePageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "startDateTime")
        );

        return eventRepository.findAllByStartDateTimeBeforeAndEndDateTimeAfter(LocalDateTime.now(), LocalDateTime.now(), pageable);
    }

    public Page<Event> getFutureEvents(SimplePageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "startDateTime")
        );

        return eventRepository.findAllByStartDateTimeAfter(LocalDateTime.now(), pageable);
    }

    public Page<Event> getPastEvents(SimplePageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "startDateTime")
        );

        return eventRepository.findAllByStartDateTimeBeforeAndEndDateTimeBefore(LocalDateTime.now(), LocalDateTime.now(), pageable);
    }
}
