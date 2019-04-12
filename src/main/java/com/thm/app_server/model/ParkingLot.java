package com.thm.app_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "parking_lot")
public class ParkingLot extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    private double latitude;

    private double longitude;

    private int capacity;

    private int current = 0;

    private float star = 0f;

    @JsonIgnore
    private int sum = 0;

    @Column(name = "review_count", columnDefinition = "default '0'")
    private int reviewCount = 0;

    @Column(name = "open_time")
    private String openTime;

    @Column(name = "close_time")
    private String closeTime;

    @OneToOne
    @JoinColumn(name = "image_id")
    private Image image;

    public ParkingLot(String name, String address, double latitude, double longitude, int capacity, String openTime, String closeTime) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacity = capacity;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }
}
