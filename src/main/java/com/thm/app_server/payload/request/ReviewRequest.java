package com.thm.app_server.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {
    private Long targetId;

    private String comment;

    private int star;
}
