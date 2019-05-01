package com.thm.app_server.controller;

import com.thm.app_server.exception.ResourceNotFoundException;
import com.thm.app_server.model.*;
import com.thm.app_server.payload.response.BasicResourceResponse;
import com.thm.app_server.payload.response.IndexResponse;
import com.thm.app_server.payload.response.InvoiceResponse;
import com.thm.app_server.payload.response.MessageResponse;
import com.thm.app_server.repository.InvoiceRepository;
import com.thm.app_server.repository.ParkingLotRepository;
import com.thm.app_server.repository.UserRepository;
import com.thm.app_server.security.UserPrincipal;
import com.thm.app_server.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {
    private InvoiceRepository invoiceRepository;

    private UserRepository userRepository;

    private ParkingLotRepository parkingLotRepository;

    private FirebaseService firebaseService;

    @Autowired
    public InvoiceController(InvoiceRepository invoiceRepository, UserRepository userRepository,
                             ParkingLotRepository parkingLotRepository, FirebaseService firebaseService) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.firebaseService = firebaseService;
    }

    @Secured("ROLE_MANAGER")
    @PostMapping("/manager/create")
    public ResponseEntity<?> createInvoice(@RequestParam String plate) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", principal.getId()));
        ParkingLot parkingLot = user.getProperty();
        if (parkingLot.getCurrent() == parkingLot.getCapacity()) {
            return new ResponseEntity<>(new MessageResponse(("Full")), HttpStatus.BAD_REQUEST);
        }
        List<InvoiceStatus> statusList = getActiveStatusList();
        Invoice pending = invoiceRepository.findByParkingLotAndPlateAndStatusIn(parkingLot, plate, statusList);
        if (pending != null) {
            if (pending.getStatus().equals(InvoiceStatus.STATUS_ACTIVE)) {
                return new ResponseEntity<>(new MessageResponse("Existed"), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(new MessageResponse("Pending"), HttpStatus.BAD_REQUEST);
            }
        }
        parkingLot.setCurrent(parkingLot.getCurrent() + 1);
        parkingLotRepository.save(parkingLot);
        firebaseService.setAvailable(parkingLot.getId(), parkingLot.getCapacity() - parkingLot.getCurrent());
        Invoice invoice = new Invoice(null, parkingLot, plate);
        invoice.setStatus(InvoiceStatus.STATUS_ACTIVE);
        invoiceRepository.save(invoice);
        return ResponseEntity.ok(new InvoiceResponse("Success", invoice, parkingLot));
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestInvoice(@RequestParam Long parkingLotId, @RequestParam String plate,
                                            @RequestParam int duration) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId()).orElse(null);
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot", "ID", parkingLotId));
        Invoice pending = invoiceRepository.findByOwnerAndStatus(user, InvoiceStatus.STATUS_PENDING);
        if (pending != null) {
            return new ResponseEntity<>(new MessageResponse(ReserveStatus.PENDING.toString()), HttpStatus.BAD_REQUEST);
        }
        if (parkingLot.getCurrent() == parkingLot.getCapacity()) {
            return new ResponseEntity<>(new MessageResponse(ReserveStatus.FULL.toString()), HttpStatus.BAD_REQUEST);
        }
        List<InvoiceStatus> statusList = getActiveStatusList();
        Invoice exist = invoiceRepository.findByParkingLotAndPlateAndStatusIn(parkingLot, plate, statusList);
        if (exist != null) {
            return new ResponseEntity<>(new MessageResponse(ReserveStatus.EXIST.toString()), HttpStatus.BAD_REQUEST);
        }
        parkingLot.setCurrent(parkingLot.getCurrent() + 1);
        parkingLot.setPending(parkingLot.getPending() + 1);
        parkingLotRepository.save(parkingLot);
        firebaseService.setAvailable(parkingLot.getId(), parkingLot.getCapacity() - parkingLot.getCurrent());
        firebaseService.setPending(parkingLot.getId(), parkingLot.getPending());
        Invoice invoice = new Invoice(user, parkingLot, plate);
        invoice.setDuration(duration);
        invoice.setIncome(duration * parkingLot.getPrice());
        invoice.setBooked(true);
        invoiceRepository.save(invoice);
        return ResponseEntity.ok(new InvoiceResponse("OK", invoice, invoice.getParkingLot()));
    }

    @PostMapping("/change/{id}")
    public ResponseEntity<MessageResponse> changeReservePlate(@PathVariable Long id, @RequestParam String plate,
                                                              @RequestParam int duration) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", "ID", id));
        ParkingLot parkingLot = invoice.getParkingLot();
        Invoice exist = invoiceRepository.findByParkingLotAndPlateAndStatusIn(parkingLot, plate, getActiveStatusList());
        if (exist != null) {
            if (!exist.getId().equals(id)) {
                return new ResponseEntity<>(new MessageResponse(ReserveStatus.EXIST.toString()), HttpStatus.BAD_REQUEST);
            }
        }
        invoice.setPlate(plate);
        invoice.setDuration(duration);
        invoice.setIncome(duration * parkingLot.getPrice());
        invoiceRepository.save(invoice);
        return ResponseEntity.ok(new MessageResponse("OK"));
    }

    @Secured("ROLE_MANAGER")
    @PostMapping("/manager/accept/{id}")
    public ResponseEntity<?> acceptInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", "ID", id));
        ParkingLot parkingLot = invoice.getParkingLot();
        parkingLot.setPending(parkingLot.getPending() - 1);
        parkingLotRepository.save(parkingLot);
        invoice.setStatus(InvoiceStatus.STATUS_ACTIVE);
        invoiceRepository.save(invoice);
        firebaseService.setPending(parkingLot.getId(), parkingLot.getPending());
        return ResponseEntity.ok(new InvoiceResponse("OK", invoice, invoice.getParkingLot()));
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", "ID", id));
        invoice.setStatus(InvoiceStatus.STATUS_CANCELED);
        invoice.setEndDate(Instant.now());
        invoiceRepository.save(invoice);
        ParkingLot parkingLot = invoice.getParkingLot();
        parkingLot.setCurrent(parkingLot.getCurrent() - 1);
        parkingLot.setPending(parkingLot.getPending() - 1);
        parkingLotRepository.save(parkingLot);
        firebaseService.setPending(parkingLot.getId(), parkingLot.getPending());
        firebaseService.setAvailable(parkingLot.getId(), parkingLot.getCapacity() - parkingLot.getCurrent());
        return ResponseEntity.ok(new MessageResponse("Canceled successfully"));
    }

    @Secured("ROLE_MANAGER")
    @PostMapping("/manager/withdraw/{id}")
    public ResponseEntity<?> withdraw(@PathVariable Long id) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", "ID", id));
        ParkingLot parkingLot = invoice.getParkingLot();
        invoice.setEndDate(Instant.now());
        invoice.setStatus(InvoiceStatus.STATUS_DONE);
        Duration duration = Duration.between(invoice.getCreatedDate(), invoice.getEndDate());
        long hour = duration.getSeconds() / 3600;
        if (hour == 0) {
            hour = 1;
        }
        invoice.setDuration((int) hour);
        invoice.setIncome((int) (hour * parkingLot.getPrice()));
        invoiceRepository.save(invoice);
        parkingLot.setCurrent(parkingLot.getCurrent() - 1);
        parkingLotRepository.save(parkingLot);
        firebaseService.setAvailable(parkingLot.getId(), parkingLot.getCapacity() - parkingLot.getCurrent());
        return ResponseEntity.ok(new InvoiceResponse("OK", invoice, parkingLot));
    }

    @Secured("ROLE_MANAGER")
    @GetMapping("/manager/index")
    public ResponseEntity<?> index() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", principal.getId()));
        List<Invoice> allList = invoiceRepository.findAllByParkingLot(user.getProperty());
        List<Invoice> activeList = allList.stream().filter(i -> i.getStatus().equals(InvoiceStatus.STATUS_PENDING) || i.getStatus().equals(InvoiceStatus.STATUS_ACTIVE))
                .collect(Collectors.toList());
        List<Invoice> endedList = new ArrayList<>(allList);
        endedList.removeAll(activeList);
        return ResponseEntity.ok(new IndexResponse(activeList, endedList, allList));
    }

    @Secured("ROLE_MANAGER")
    @GetMapping("/manager/active")
    public ResponseEntity<?> getActive() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElse(null);
        if (user != null) {
            List<Invoice> activeList = invoiceRepository.findAllByParkingLotAndStatusIn(user.getProperty(), getActiveStatusList());
            return ResponseEntity.ok(new BasicResourceResponse("OK", activeList));
        } else {
            return new ResponseEntity<>(new MessageResponse("Fail"), HttpStatus.BAD_REQUEST);
        }
    }

    @Secured("ROLE_MANAGER")
    @GetMapping("/manager/done")
    public ResponseEntity<?> getDone() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElse(null);
        if (user != null) {
            List<Invoice> doneList = invoiceRepository.findAllByParkingLotAndStatusIn(user.getProperty(), getDoneStatusList());
            return ResponseEntity.ok(new BasicResourceResponse("OK", doneList));
        } else {
            return new ResponseEntity<>(new MessageResponse("Fail"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user_pending")
    public ResponseEntity<?> pendingListForUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", principal.getId()));
        System.out.println(principal.getId());
        Invoice invoice = invoiceRepository.findByOwnerAndStatus(user, InvoiceStatus.STATUS_PENDING);
        if (invoice == null) {
            return ResponseEntity.ok(new InvoiceResponse("NULL", null, null));
        }
        return ResponseEntity.ok(new InvoiceResponse("OK", invoice, invoice.getParkingLot()));
    }

    private List<InvoiceStatus> getActiveStatusList() {
        List<InvoiceStatus> result = new ArrayList<>();
        result.add(InvoiceStatus.STATUS_ACTIVE);
        result.add(InvoiceStatus.STATUS_PENDING);
        return result;
    }

    private List<InvoiceStatus> getDoneStatusList() {
        List<InvoiceStatus> result = new ArrayList<>();
        result.add(InvoiceStatus.STATUS_DONE);
        result.add(InvoiceStatus.STATUS_CANCELED);
        return result;
    }
}
