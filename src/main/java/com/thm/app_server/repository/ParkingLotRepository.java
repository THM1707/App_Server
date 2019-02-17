package com.thm.app_server.repository;

import com.thm.app_server.model.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    List<ParkingLot> findAll();
    Optional<ParkingLot> findById(Long id);
    List<ParkingLot> findByIdIn(List<Long> id);
}
