package com.thm.app_server.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyRequest {
    private String name;
    private String address;
    private String openTime;
    private String closeTime;
    private int capacity;
    private int price;
    private double latitude;
    private double longitude;
    private String image;
}
