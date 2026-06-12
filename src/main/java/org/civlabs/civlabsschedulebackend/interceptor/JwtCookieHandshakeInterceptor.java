package org.civlabs.civlabsschedulebackend.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.civlabs.civlabsschedulebackend.entity.User;
import org.civlabs.civlabsschedulebackend.service.AuthService;
import org.civlabs.civlabsschedulebackend.service.JwtService;
import org.civlabs.civlabsschedulebackend.service.RefreshTokenService;
import org.civlabs.civlabsschedulebackend.service.UserService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class JwtCookieHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            String accessToken = authService.getCookie(httpRequest, "ACCESS_TOKEN");
            String refreshToken = authService.getCookie(httpRequest, "REFRESH_TOKEN");

            if (accessToken == null || refreshToken == null) {
                return false; // reject WS connect
            }

            // Optional: validate refresh token first (your logic)
            if (!refreshTokenService.isValidRefreshToken(refreshToken)) {
                return false;
            }

            // Validate access token or regenerate logic
            if (!jwtService.validateToken(accessToken)) {
                accessToken =
                        jwtService.generateAccessToken(
                                jwtService.getUsernameFromToken(refreshToken)
                        );
            }

            String username = jwtService.getUsernameFromToken(accessToken);


            User user = userService
                    .findByUsername(username);

            if (user == null) {
                return false;
            }

            List<SimpleGrantedAuthority> authorities =
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                            .toList();

            Authentication auth =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

            // 🔥 THIS is what you pass forward to STOMP
            attributes.put("auth", auth);

            return true;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
                               WebSocketHandler handler, Exception ex) {}
}