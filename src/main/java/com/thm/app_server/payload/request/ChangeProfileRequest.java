package com.thm.app_server.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeProfileRequest {
    private String name;
    private String newPassword;
    private String oldPassword;
    private String phone;
    private int gender;
}
