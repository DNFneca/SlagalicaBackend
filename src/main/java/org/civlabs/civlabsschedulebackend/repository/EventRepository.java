package org.civlabs.civlabsschedulebackend.repository;

import org.civlabs.civlabsschedulebackend.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    // Find all, sorted by createdAt descending
    Page<Event> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Or with a specific field
    Page<Event> findAllByOrderByIdDesc(Pageable pageable);

    Page<Event> findAllByStartDateTimeAfter(LocalDateTime startDateTimeAfter, Pageable pageable);

    Page<Event> findAllByStartDateTimeBeforeAndEndDateTimeBefore(LocalDateTime startDateTimeBefore, LocalDateTime endDateTimeBefore, Pageable pageable);

    Page<Event> findAllByStartDateTimeBeforeAndEndDateTimeAfter(LocalDateTime startDateTimeBefore, LocalDateTime endDateTimeAfter, Pageable pageable);
}
