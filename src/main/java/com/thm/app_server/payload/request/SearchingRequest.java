package com.thm.app_server.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchingRequest {
    private double latitude;
    private double longitude;
    private double distance;
    private int duration;
    private int budget;
    private int option;
}
