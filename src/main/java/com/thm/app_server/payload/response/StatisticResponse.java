package com.thm.app_server.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticResponse {
    private int parkingCount;
    private int userCount;
    private int invoiceCount;
    private int revenue;
    private int bookableCount;
    private int[] revenueData;

    public StatisticResponse(int parkingCount, int userCount, int invoiceCount, int revenue, int bookableCount, int[] revenueData) {
        this.parkingCount = parkingCount;
        this.userCount = userCount;
        this.invoiceCount = invoiceCount;
        this.revenue = revenue;
        this.bookableCount = bookableCount;
        this.revenueData = revenueData;
    }
}
