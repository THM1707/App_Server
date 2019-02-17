package com.thm.app_server.payload.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BasicResourceResponse {
    private String message;
    private Object data;

    public BasicResourceResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }
}
