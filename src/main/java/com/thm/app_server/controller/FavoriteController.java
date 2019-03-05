package com.thm.app_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thm.app_server.exception.ResourceNotFoundException;
import com.thm.app_server.model.ParkingLot;
import com.thm.app_server.model.User;
import com.thm.app_server.payload.response.BasicResourceResponse;
import com.thm.app_server.repository.ParkingLotRepository;
import com.thm.app_server.repository.UserRepository;
import com.thm.app_server.security.UserPrincipal;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    private UserRepository userRepository;

    private ParkingLotRepository parkingLotRepository;

    @Autowired
    public FavoriteController(UserRepository userRepository, ParkingLotRepository parkingLotRepository) {
        this.userRepository = userRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    @GetMapping("")
    public ResponseEntity<?> getFavorite() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            Long userId = ((UserPrincipal) principal).getId();
            User u = userRepository.findById(userId).orElse(null);
            if (u != null) {
                return ResponseEntity.ok(new BasicResourceResponse("OK", u.getFavorites()));
            }
        }
        return new ResponseEntity<>("Server error", HttpStatus.BAD_GATEWAY);
    }

    @PutMapping("/remove/{id}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isHit = false;
        if (principal instanceof UserPrincipal) {
            Long userId = ((UserPrincipal) principal).getId();
            User u = userRepository.findById(userId).orElse(null);
            if (u != null) {
                isHit = u.getFavorites().removeIf(p -> p.getId().equals(id));
                userRepository.save(u);
            }
        }
        return ResponseEntity.ok(isHit);
    }

    @PutMapping("/add/{id}")
    public ResponseEntity<?> addFavorite(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isHit = false;
        if (principal instanceof UserPrincipal) {
            Long userId = ((UserPrincipal) principal).getId();
            User u = userRepository.findById(userId).orElse(null);
            if (u != null) {
                ParkingLot p = parkingLotRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Parking lot", "Id", id));
                isHit = u.getFavorites().add(p);
                userRepository.save(u);
            }
        }
        return ResponseEntity.ok(isHit);
    }

}