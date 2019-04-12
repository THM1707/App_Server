package com.thm.app_server.repository;

import com.thm.app_server.model.ParkingLot;
import com.thm.app_server.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    int countAllByTarget(ParkingLot target);
    List<Review> findAllByTarget(ParkingLot target);
}
