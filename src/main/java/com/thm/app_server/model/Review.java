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
@Table(name = "reviews")
public class Review extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int star;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "owner_email")
    private String ownerEmail;

    private String comment;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private ParkingLot target;

    public Review(int star, String ownerName, String ownerEmail, String comment, ParkingLot target) {
        this.star = star;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.comment = comment;
        this.target = target;
    }
}
