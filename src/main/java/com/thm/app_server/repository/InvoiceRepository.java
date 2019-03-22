package com.thm.app_server.repository;

import com.thm.app_server.model.Invoice;
import com.thm.app_server.model.InvoiceStatus;
import com.thm.app_server.model.ParkingLot;
import com.thm.app_server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findAll();

    List<Invoice> findAllByParkingLot(ParkingLot parkingLot);

    Invoice findByOwnerAndStatus(User user, InvoiceStatus status);

    Invoice findByParkingLotAndPlateAndStatusIn(ParkingLot parkingLot, String plate, List<InvoiceStatus> statusList);
}
