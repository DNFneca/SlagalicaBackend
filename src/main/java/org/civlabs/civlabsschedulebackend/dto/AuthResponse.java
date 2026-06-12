package org.civlabs.civlabsschedulebackend.dto;

public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    // getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}
