package com.thm.app_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thm.app_server.payload.request.ManagerSignUpRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sign_up_forms")
public class SignUpForm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private Instant createdDate;

    private String username;

    private String email;

    private String name;

    private String phone;

    private int gender;

    @JsonIgnore
    private String password;

    private String propertyName;

    private String address;

    private double latitude;

    private double longitude;

    private int capacity;

    private int price;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SignUpFormStatus status = SignUpFormStatus.PENDING;

    @Column(name = "open_time")
    private String openTime;

    @Column(name = "close_time")
    private String closeTime;

    @OneToOne
    @JoinColumn(name = "image_id")
    private Image image;

    public SignUpForm(ManagerSignUpRequest request) {
        username = request.getUsername();
        email = request.getEmail();
        password = request.getPassword();
        name = request.getName();
        propertyName = request.getPropertyName();
        gender = request.getGender();
        phone = request.getPhone();
        address = request.getAddress();
        latitude = request.getLatitude();
        longitude = request.getLongitude();
        capacity = request.getCapacity();
        openTime = request.getOpenTime();
        closeTime = request.getCloseTime();
        price = request.getPrice();
    }
}
