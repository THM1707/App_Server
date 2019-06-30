package com.thm.app_server.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ChangeProfileRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Size(min = 6)
    private String newPassword;
    private String oldPassword;
    @NotBlank
    private String phone;
    @NotNull
    private int gender;
}
