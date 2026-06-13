package org.civlabs.civlabsschedulebackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.civlabs.civlabsschedulebackend.entity.RefreshToken;
import org.civlabs.civlabsschedulebackend.entity.Role;
import org.civlabs.civlabsschedulebackend.entity.User;
import org.civlabs.civlabsschedulebackend.repository.UserRepository;
import org.civlabs.civlabsschedulebackend.service.AuthService;
import org.civlabs.civlabsschedulebackend.service.JwtService;
import org.civlabs.civlabsschedulebackend.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthService authService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(RefreshTokenService refreshTokenService, JwtService jwtService, AuthService authService, UserRepository userRepository) {
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessTokenString = authService.getCookie(request, "ACCESS_TOKEN");
        String refreshTokenString = authService.getCookie(request, "REFRESH_TOKEN");



        /*
            First we check refresh token validity then access token
         */
        if (accessTokenString == null || refreshTokenString == null) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }


        if (!refreshTokenService.isValidRefreshToken(refreshTokenString)) {
            Optional<RefreshToken> optionalRefreshToken = refreshTokenService.findByToken(refreshTokenString);
            if (optionalRefreshToken.isEmpty()) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            RefreshToken refreshToken = optionalRefreshToken.get();

            if (refreshToken.getRevoked()) {
                if (refreshToken.getReplacedBy() == null) {
                    refreshTokenService.revokeAllUserTokens(refreshToken.getUser());
                }
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (refreshToken.isExpired()) {
                RefreshToken newRefreshToken = refreshTokenService.refreshToken(refreshToken);
                authService.addRefreshToken(response, newRefreshToken.getToken());
                refreshTokenString = newRefreshToken.getToken();
            }
        }

        if (jwtService.validateToken(accessTokenString)) {
            setAuthentication(accessTokenString);
        } else {
            String newAccessToken = jwtService.generateAccessToken(jwtService.getUsernameFromToken(refreshTokenString));
            authService.addAccessToken(response, newAccessToken);
            setAuthentication(newAccessToken);
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String accessToken) {
        String username = jwtService.getUsernameFromToken(accessToken);

        // Extract roles - handle both List and String formats
        User byUsername = this.userRepository.findByUsername(username).orElse(null);


        if (byUsername == null) {
            return;
        }

        Collection<? extends GrantedAuthority> authorities = byUsername
                .getRoles()
                .stream()
                .map(Role::getName)
                .filter(role -> !role.isEmpty())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // Create Authentication Object with authorities
        if (username != null && !authorities.isEmpty()) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, authorities);


            // Set Context so DGS and Spring Security can see it
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod()) || path.startsWith("/ws");
    }
}