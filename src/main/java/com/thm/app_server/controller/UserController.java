package com.thm.app_server.controller;

import com.thm.app_server.exception.ResourceNotFoundException;
import com.thm.app_server.model.*;
import com.thm.app_server.payload.request.ChangeProfileRequest;
import com.thm.app_server.payload.request.ReviewRequest;
import com.thm.app_server.payload.request.SearchingRequest;
import com.thm.app_server.payload.response.BasicResourceResponse;
import com.thm.app_server.payload.response.MessageResponse;
import com.thm.app_server.payload.response.ProfileResponse;
import com.thm.app_server.repository.InvoiceRepository;
import com.thm.app_server.repository.ParkingLotRepository;
import com.thm.app_server.repository.StandardRepository;
import com.thm.app_server.repository.UserRepository;
import com.thm.app_server.security.UserPrincipal;
import com.thm.app_server.service.SearchingService;
import com.thm.app_server.service.impl.ReviewServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Handle users' relate action
@RestController
@RequestMapping("/api/user/")
public class UserController {
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private InvoiceRepository invoiceRepository;

    private ReviewServiceImpl reviewService;

    private ParkingLotRepository parkingLotRepository;

    private StandardRepository standardRepository;

    private SearchingService searchingService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, InvoiceRepository invoiceRepository, ReviewServiceImpl reviewService, ParkingLotRepository parkingLotRepository, StandardRepository standardRepository, SearchingService searchingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.invoiceRepository = invoiceRepository;
        this.reviewService = reviewService;
        this.parkingLotRepository = parkingLotRepository;
        this.standardRepository = standardRepository;
        this.searchingService = searchingService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId()).orElse(null);
        if (user != null) {
            int bookCount = invoiceRepository.countAllByOwner(user);
            int cancelCount = invoiceRepository.countAllByOwnerAndStatus(user, InvoiceStatus.STATUS_CANCELED);
            return ResponseEntity.ok(new ProfileResponse(user.getName(), user.getUsername(), user.getEmail(), user.getPhone(), user.getGender(), bookCount, cancelCount));
        } else {
            return new ResponseEntity<>(new MessageResponse("Fail"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/changeProfile")
    public ResponseEntity<MessageResponse> changePassword(@RequestBody ChangeProfileRequest request) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (passwordEncoder.matches(request.getOldPassword(), principal.getPassword())) {
            Long userId = principal.getId();
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                if (request.getNewPassword() != null) {
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                }
                user.setName(request.getName());
                user.setGender(request.getGender());
                user.setPhone(request.getPhone());
                userRepository.save(user);
                return ResponseEntity.ok(new MessageResponse("Success"));
            }
        }
        return new ResponseEntity<>(new MessageResponse("Fail"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/review")
    public ResponseEntity<?> submitReview(@RequestBody ReviewRequest request) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ParkingLot target = parkingLotRepository.findById(request.getTargetId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot", "Id", request.getTargetId()));
        User user = userRepository.findById(principal.getId()).orElse(null);
        if (user != null) {
            Review review = new Review(request.getStar(), user.getName(), user.getEmail(), request.getComment(), target);
            reviewService.addReview(review);
            return ResponseEntity.ok(new BasicResourceResponse("OK", review));
        } else {
            return new ResponseEntity<>(new MessageResponse("Fail"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/review/{id}")
    public ResponseEntity<?> editReview(@PathVariable Long id, @RequestParam int star, @RequestParam String comment) {
        return ResponseEntity.ok(new BasicResourceResponse("EDITED", reviewService.editReview(id, star, comment)));
    }

    @DeleteMapping("review/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/smartSearching")
    public ResponseEntity<?> smartSearching(@RequestBody SearchingRequest request) {
        Standard standard;
        switch (request.getOption()) {
            case 0:
                standard = standardRepository.findByType(StandardType.STANDARD_DISTANCE);
                break;
            case 1:
                standard = standardRepository.findByType(StandardType.STANDARD_PRICE);
                break;
            default:
                return new ResponseEntity<>(new MessageResponse("Invalid request"), HttpStatus.BAD_REQUEST);
        }
        Map<Integer, Long> result = searchingService.getSortedValues(request.getLatitude(), request.getLongitude(),
                request.getDistance(), request.getBudget(), request.getDuration(), standard);
        return ResponseEntity.ok(new BasicResourceResponse("OK", result));
    }
}
