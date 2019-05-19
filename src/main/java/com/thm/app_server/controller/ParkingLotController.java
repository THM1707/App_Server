package com.thm.app_server.controller;

import com.thm.app_server.exception.ResourceNotFoundException;
import com.thm.app_server.model.Image;
import com.thm.app_server.model.ParkingLot;
import com.thm.app_server.model.Review;
import com.thm.app_server.model.User;
import com.thm.app_server.payload.request.PropertyRequest;
import com.thm.app_server.payload.response.BasicResourceResponse;
import com.thm.app_server.repository.ImageRepository;
import com.thm.app_server.repository.ParkingLotRepository;
import com.thm.app_server.repository.ReviewRepository;
import com.thm.app_server.repository.UserRepository;
import com.thm.app_server.security.UserPrincipal;
import com.thm.app_server.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking_lot")
public class ParkingLotController {

    private ParkingLotRepository parkingLotRepository;
    private UserRepository userRepository;
    private ReviewRepository reviewRepository;
    private FirebaseService firebaseService;
    private ImageRepository imageRepository;

    @Autowired
    public ParkingLotController(ParkingLotRepository parkingLotRepository, UserRepository userRepository,
                                ReviewRepository reviewRepository, FirebaseService firebaseService,
                                ImageRepository imageRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.firebaseService = firebaseService;
        this.imageRepository = imageRepository;
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

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    public void deleteReviews(@PathVariable Long id) {
        ParkingLot parkingLot = parkingLotRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Parking lot", "ID", id));
        User owner = parkingLot.getOwner();
        if (owner != null) {
            userRepository.delete(owner);
            firebaseService.deleteParkingLot(id);
            firebaseService.deletePending(id);
        } else {
            parkingLotRepository.delete(parkingLot);
            firebaseService.deleteParkingLot(id);
        }
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/create")
    public ResponseEntity<?> createProperty(@RequestBody PropertyRequest request) {
        ParkingLot parkingLot = new ParkingLot(request.getName(), request.getAddress(), request.getLatitude(),
                request.getLongitude(), request.getCapacity(), request.getOpenTime(), request.getCloseTime(),
                request.getPrice());
        Image image = new Image(request.getImage());
        imageRepository.save(image);
        parkingLot.setImage(image);
        ParkingLot saved = parkingLotRepository.save(parkingLot);
        firebaseService.addOrEditParkingLot(saved.getId(), saved.getName(), saved.getLatitude(), saved.getLongitude(),
                saved.getStar(), saved.getCapacity(), saved.getPrice(), saved.getType());
        return ResponseEntity.ok(new BasicResourceResponse("OK", saved));
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editProperty(@PathVariable Long id, @RequestBody PropertyRequest request) {
        ParkingLot parkingLot = parkingLotRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Parking lot", "ID", id));
        parkingLot.setName(request.getName());
        parkingLot.setAddress(request.getAddress());
        parkingLot.setOpenTime(request.getOpenTime());
        parkingLot.setCloseTime(request.getCloseTime());
        parkingLot.setCapacity(request.getCapacity());
        parkingLot.setLatitude(request.getLatitude());
        parkingLot.setLongitude(request.getLongitude());
        parkingLot.setPrice(request.getPrice());
        ParkingLot saved = parkingLotRepository.save(parkingLot);
        firebaseService.addOrEditParkingLot(id, parkingLot.getName(), parkingLot.getLatitude(),
                parkingLot.getLongitude(), parkingLot.getStar(), parkingLot.getCapacity(),
                parkingLot.getPrice(), parkingLot.getType());
        return ResponseEntity.ok(new BasicResourceResponse("OK", saved));
    }
}
