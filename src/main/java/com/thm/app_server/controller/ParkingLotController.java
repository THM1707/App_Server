package com.thm.app_server.controller;

import com.thm.app_server.exception.ResourceNotFoundException;
import com.thm.app_server.model.ParkingLot;
import com.thm.app_server.payload.request.ParkingLotIdsRequest;
import com.thm.app_server.payload.response.BasicResourceResponse;
import com.thm.app_server.repository.ParkingLotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking_lot")
public class ParkingLotController {

    private final ParkingLotRepository parkingLotRepository;

    @Autowired
    public ParkingLotController(ParkingLotRepository parkingLotRepository) {
        this.parkingLotRepository = parkingLotRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<?> index() {
        return ResponseEntity.ok(new BasicResourceResponse("success", parkingLotRepository.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        ParkingLot p = parkingLotRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Parking lot", "Id", id));
        return ResponseEntity.ok(new BasicResourceResponse("success", p));
    }

    @PostMapping("/in")
    public ResponseEntity<?> in(@RequestBody ParkingLotIdsRequest request) {
        List<ParkingLot> result = parkingLotRepository.findByIdIn(request.getIdList());
        return ResponseEntity.ok(new BasicResourceResponse("success", result));
    }
}
