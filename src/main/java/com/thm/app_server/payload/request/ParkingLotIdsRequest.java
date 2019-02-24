package com.thm.app_server.payload.request;

import java.util.List;

public class ParkingLotIdsRequest {
    private List<Long> idList;

    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> idList) {
        this.idList = idList;
    }
}
