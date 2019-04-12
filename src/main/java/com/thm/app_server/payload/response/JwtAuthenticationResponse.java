package com.thm.app_server.payload.response;

import com.thm.app_server.model.ParkingLot;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class JwtAuthenticationResponse {
    private String accessToken;

    private String role;

    private String name;

    private String email;

    private int gender;

    private ParkingLot property;

    public JwtAuthenticationResponse(String accessToken, String role) {
        this.accessToken = accessToken;
        this.role = role;
        this.property = null;
    }
}