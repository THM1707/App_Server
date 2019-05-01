package com.thm.app_server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "standard")
public class Standard {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @Column(name = "distance_weight")
    private float distanceWeight;

    @Column(name = "price_weight")
    private float priceWeight;

    @Enumerated(EnumType.STRING)
    private StandardType type;
}
