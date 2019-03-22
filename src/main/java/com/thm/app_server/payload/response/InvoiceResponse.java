package com.thm.app_server.payload.response;

import com.thm.app_server.model.Invoice;
import com.thm.app_server.model.ParkingLot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class InvoiceResponse {
    private String message;
    private Invoice invoice;
    private ParkingLot parkingLot;

    public InvoiceResponse(String message, Invoice invoice, ParkingLot parkingLot) {
        this.message = message;
        this.invoice = invoice;
        this.parkingLot = parkingLot;
    }
}
