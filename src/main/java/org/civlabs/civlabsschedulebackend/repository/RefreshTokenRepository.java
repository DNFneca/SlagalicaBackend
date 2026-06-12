package org.civlabs.civlabsschedulebackend.repository;

import org.civlabs.civlabsschedulebackend.entity.RefreshToken;
import org.civlabs.civlabsschedulebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUser(User user);
    void deleteByExpiresAtBefore(LocalDateTime date);
    void deleteByUser(User user);
}