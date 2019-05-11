package com.thm.app_server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "username"
        }),
        @UniqueConstraint(columnNames = {
                "email"
        })
})
public class User extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /*
    0: Male
    1: Female
     */
    private int gender;

    private String phone;

    @Size(max = 15)
    private String username;

    @NaturalId
    @NotBlank
    @Size(max = 40)
    @Email
    private String email;

    @NotBlank
    @Size(max = 100)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_favorite",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "parking_lot_id"))
    private Set<ParkingLot> favorites = new HashSet<>();

    @OneToOne(fetch = FetchType.EAGER)
    @JoinTable(name = "manager_property", joinColumns = @JoinColumn(name = "manager_id"), inverseJoinColumns = @JoinColumn(name = "parking_lot_id"))
    private ParkingLot property;

    @Column(name = "enabled")
    private boolean enabled = false;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    private int budget = 0;

    @Column(name = "firebase_token")
    private String firebaseToken;

    public User(String username, String email, String password, String name, String phone, int gender) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.gender = gender;
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
