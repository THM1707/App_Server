package com.thm.app_server.controller;

import com.thm.app_server.service.FirebaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoController {
    private FirebaseService firebaseService;

    public DemoController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @PostMapping("/changeAvailable")
    public ResponseEntity<?> changeAvailable(@RequestParam Long id, @RequestParam int available) {
        firebaseService.setAvailable(id, available);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/changeStar")
    public ResponseEntity<?> changeStar(@RequestParam Long id, @RequestParam float star) {
        firebaseService.setStar(id, star);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestParam Long id, @RequestParam String name, @RequestParam float star,
                                 @RequestParam double latitude, @RequestParam double longitude, @RequestParam int available) {
        firebaseService.addParkingLot(id, name, latitude, longitude, star, available);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/changePending")
    public ResponseEntity<?> changePending(@RequestParam Long id, @RequestParam int value) {
        firebaseService.setPending(id, value);
        return ResponseEntity.ok("OK");
    }
}