package org.civlabs.civlabsschedulebackend.service;

import org.civlabs.civlabsschedulebackend.entity.RefreshToken;
import org.civlabs.civlabsschedulebackend.entity.User;
import org.civlabs.civlabsschedulebackend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration; // in milliseconds

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public RefreshToken saveRefreshToken(User user, String token) {

        // Optional: Revoke all previous refresh tokens for this user
        // revokeAllUserTokens(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public boolean isValidRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);

        if (refreshTokenOpt.isEmpty()) {
                return false;
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        return refreshToken.isValid();
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void revokeToken(RefreshToken refreshToken, RefreshToken newRefreshToken) {
        refreshToken.setRevoked(true);
        refreshToken.setReplacedBy(newRefreshToken);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);
        tokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    @Transactional
    public RefreshToken refreshToken(RefreshToken expiredRefreshToken) {
        User user = expiredRefreshToken.getUser();
        RefreshToken refreshToken = saveRefreshToken(user, jwtService.generateRefreshToken(user.getUsername()));
        revokeToken(expiredRefreshToken, refreshToken);
        return refreshToken;
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    @Transactional
    public void deleteAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}