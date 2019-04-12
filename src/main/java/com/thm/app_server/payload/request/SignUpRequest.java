package com.thm.app_server.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
public class SignUpRequest {

    @NotBlank
    @Size(min = 3, max = 15)
    private String username;

    @NotBlank
    @Size(max = 40)
    @Email(message = "Not a valid email")
    private String email;

    @NotBlank
    @Size(min = 6, max = 20)
    private String password;

    private String name;

    private String phone;

    private int gender;
}
