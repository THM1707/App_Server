package com.thm.app_server.controller;

import com.thm.app_server.exception.ResourceNotFoundException;
import com.thm.app_server.model.ParkingLot;
import com.thm.app_server.model.Review;
import com.thm.app_server.model.User;
import com.thm.app_server.payload.response.BasicResourceResponse;
import com.thm.app_server.repository.ParkingLotRepository;
import com.thm.app_server.repository.ReviewRepository;
import com.thm.app_server.repository.UserRepository;
import com.thm.app_server.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking_lot")
public class ParkingLotController {

    private ParkingLotRepository parkingLotRepository;
    private UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public ParkingLotController(ParkingLotRepository parkingLotRepository, UserRepository userRepository, ReviewRepository reviewRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<?> index() {
        return ResponseEntity.ok(new BasicResourceResponse("success", parkingLotRepository.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId()).orElse(null);
        boolean isFavorite = false;
        if (user != null) {
            for (ParkingLot p : user.getFavorites()) {
                if (id.equals(p.getId())) {
                    isFavorite = true;
                    break;
                }
            }
        }
        ParkingLot p = parkingLotRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Parking lot", "Id", id));
        return ResponseEntity.ok(new BasicResourceResponse(isFavorite ? "Is Favorite" : "Not Favorite", p));
    }


    @GetMapping("/{id}/reviews")
    public ResponseEntity<?> getReviews(@PathVariable Long id) {
        ParkingLot parkingLot = parkingLotRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Parking lot", "ID", id));
        List<Review> reviewList = reviewRepository.findAllByTarget(parkingLot);
        return ResponseEntity.ok(new BasicResourceResponse("OK", reviewList));
    }
}
