package com.thm.app_server.payload.response;

import java.util.List;

public class JwtAuthenticationResponse {
    private String accessToken;

    private String role;

    private List<Long> favorites;

    public JwtAuthenticationResponse(String accessToken, String role, List<Long> favorites) {
        this.accessToken = accessToken;
        this.role = role;
        this.favorites = favorites;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Long> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Long> favorites) {
        this.favorites = favorites;
    }
}