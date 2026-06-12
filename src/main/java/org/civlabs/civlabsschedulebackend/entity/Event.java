package org.civlabs.civlabsschedulebackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @CreationTimestamp
    private LocalDateTime createdAt;
    private Date updatedAt;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private String description;

    // mappedBy refers to the 'events' field in the User class
    @ManyToMany(mappedBy = "events", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>(0);


}
