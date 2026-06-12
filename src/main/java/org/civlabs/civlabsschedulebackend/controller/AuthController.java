package org.civlabs.civlabsschedulebackend.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.civlabs.civlabsschedulebackend.dto.AuthResponse;
import org.civlabs.civlabsschedulebackend.dto.GoogleAuthRequest;
import org.civlabs.civlabsschedulebackend.entity.Role;
import org.civlabs.civlabsschedulebackend.entity.User;
import org.civlabs.civlabsschedulebackend.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.web.util.WebUtils.getCookie;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;

    @PostMapping("refresh")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Void> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refresh = authService.getCookie(request, "REFRESH_TOKEN");

        if (!refreshTokenService.isValidRefreshToken(refresh)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String usernameFromToken = jwtService.getUsernameFromToken(refresh);

        String newAccess = jwtService.generateAccessToken(usernameFromToken);
        String newRefresh = jwtService.generateRefreshToken(usernameFromToken);


        authService.addAccessToken(response, newAccess);
        authService.addRefreshToken(response, newRefresh);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/google")
    @CrossOrigin(origins = {"http://localhost:4200", "http://192.168.1.66:4200"})
    public ResponseEntity<?> loginWithGoogle(
            @RequestBody GoogleAuthRequest request,
            HttpServletResponse response
    ) {
        try {
            GoogleIdToken.Payload googlePayload = googleAuthService.verifyByCode(request.getCode());

            String email = googlePayload.getEmail();

            User byName = userService.findByUsername((String) googlePayload.get("name"));

            if (byName != null) {

                String accessToken = jwtService.generateAccessToken((String) googlePayload.get("name"));
                String refreshToken = jwtService.generateRefreshToken((String) googlePayload.get("name"));

                refreshTokenService.saveRefreshToken(byName, refreshToken);

                authService.addAccessToken(response, accessToken);
                authService.addRefreshToken(response, refreshToken);

                return ResponseEntity.ok().build();
            }
            Set<String> roles = new HashSet<>();
            roles.add("USER");

            userService.createUserWithGoogle(
                    (String) googlePayload.get("name"),
                    email,
                    googlePayload.getSubject(),
                    roles
            );

            String accessToken = jwtService.generateAccessToken((String) googlePayload.get("name"));
            String refreshToken = jwtService.generateRefreshToken((String) googlePayload.get("name"));

            refreshTokenService.saveRefreshToken(userService.findByEmail(email), refreshToken);

            authService.addAccessToken(response, accessToken);
            authService.addRefreshToken(response, refreshToken);

            return ResponseEntity.status(401).body("Invalid Google Token");


        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.status(401).body("Invalid Google Token");
        }
    }

}
