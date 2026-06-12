package org.civlabs.civlabsschedulebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.civlabs.civlabsschedulebackend.dto.UserDTO;
import org.civlabs.civlabsschedulebackend.entity.Role;
import org.civlabs.civlabsschedulebackend.entity.User;
import org.civlabs.civlabsschedulebackend.service.AuthService;
import org.civlabs.civlabsschedulebackend.service.JwtService;
import org.civlabs.civlabsschedulebackend.service.RefreshTokenService;
import org.civlabs.civlabsschedulebackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/")
class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("me")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<UserDTO> me(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User byUsername = userService.findByUsername(username);
        return ResponseEntity.ok(UserDTO.builder()
                .id(byUsername.getId())
                .email(byUsername.getEmail())
                .username(byUsername.getUsername())
                .ready(false)
                .active(true).build());
//                .isAdmin(byUsername.getRoles().contains(new Role(2L, "ADMIN", null)))
//                .photo(null).build());
    }

    @GetMapping("valid")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Boolean> valid(HttpServletRequest request) {
        return ResponseEntity.ok(refreshTokenService.isValidRefreshToken(authService.getCookie(request, "REFRESH_TOKEN")) &&
                jwtService.validateToken(authService.getCookie(request, "ACCESS_TOKEN")));
    }

}
