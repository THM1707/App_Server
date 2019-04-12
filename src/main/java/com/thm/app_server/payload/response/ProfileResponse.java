package com.thm.app_server.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponse {
    private String name;
    private String username;
    private String email;
    private String phone;
    private int gender;
    private int bookCount;
    private int cancelCount;

    public ProfileResponse(String name, String username, String email, String phone, int gender, int bookCount, int cancelCount) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.bookCount = bookCount;
        this.cancelCount = cancelCount;
    }
}
