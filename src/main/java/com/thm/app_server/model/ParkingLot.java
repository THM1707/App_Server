package com.thm.app_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

    private int current;

    private float star;

    private int pending;

    private int price;

    @JsonIgnore
    private int sum;

    @Column(name = "review_count", columnDefinition = "default '0'")
    private int reviewCount;

    @Column(name = "open_time")
    private String openTime;

    @Column(name = "close_time")
    private String closeTime;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image image;

    @JsonIgnore
    @OneToOne(mappedBy = "property")
    private User owner;

    /*1: realtime update
     * 0: not realtime */
    private int type;

    @JsonIgnore
    @ManyToMany(mappedBy="favorites")
    private Set<User> users = new HashSet<>();

    @PreRemove
    private void removeGroupsFromUsers() {
        for (User u : users) {
            u.getFavorites().remove(this);
        }
    }

    public ParkingLot(String name, String address, double latitude, double longitude, int capacity, String openTime,
                      String closeTime, int price) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacity = capacity;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.price = price;
    }
}
