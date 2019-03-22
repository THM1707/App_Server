package com.thm.app_server.payload.response;

import com.thm.app_server.model.Invoice;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class IndexResponse {
    List<Invoice> active;

    List<Invoice> ended;

    List<Invoice> all;

    public IndexResponse(List<Invoice> active, List<Invoice> ended, List<Invoice> all) {
        this.active = active;
        this.ended = ended;
        this.all = all;
    }
}
