package com.thm.app_server.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {
    public String message;

    public MessageResponse(String message) {
        this.message = message;
    }
}
