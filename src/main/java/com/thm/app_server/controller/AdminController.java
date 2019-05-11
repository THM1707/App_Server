package com.thm.app_server.controller;

import com.thm.app_server.exception.ResourceNotFoundException;
import com.thm.app_server.model.*;
import com.thm.app_server.payload.response.BasicResourceResponse;
import com.thm.app_server.payload.response.MessageResponse;
import com.thm.app_server.repository.ParkingLotRepository;
import com.thm.app_server.repository.RoleRepository;
import com.thm.app_server.repository.SignUpFormRepository;
import com.thm.app_server.repository.UserRepository;
import com.thm.app_server.service.EmailService;
import com.thm.app_server.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private UserRepository userRepository;

    private RoleRepository roleRepository;


    private SignUpFormRepository signUpFormRepository;

    private ParkingLotRepository parkingLotRepository;

    private EmailService emailService;

    private final FirebaseService firebaseService;


    @Autowired
    public AdminController(UserRepository userRepository, RoleRepository roleRepository, SignUpFormRepository signUpFormRepository, ParkingLotRepository parkingLotRepository, EmailService emailService, FirebaseService firebaseService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.signUpFormRepository = signUpFormRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.emailService = emailService;
        this.firebaseService = firebaseService;
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/signUp/accept/{id}")
    public ResponseEntity<?> acceptRegister(@PathVariable Long id) {
        SignUpForm form = signUpFormRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Form", "ID", id));
        User user = new User(form.getUsername(), form.getEmail(), form.getPassword(), form.getName(), form.getPhone(), form.getGender());
        user.setEnabled(true);
        RoleName[] roleNameArray = {RoleName.ROLE_MANAGER, RoleName.ROLE_USER};
        List<Role> roleList = roleRepository.findByNameIn(roleNameArray);
        user.setRoles(new HashSet<>(roleList));
        ParkingLot parkingLot = new ParkingLot(form.getPropertyName(), form.getAddress(), form.getLatitude(), form.getLongitude(), form.getCapacity(), form.getOpenTime(), form.getCloseTime(), form.getPrice());
        Image image = form.getImage();
        parkingLot.setImage(image);
        user.setProperty(parkingLot);
        form.setStatus(SignUpFormStatus.ACCEPTED);
        signUpFormRepository.save(form);
        ParkingLot savedParkingLot = parkingLotRepository.save(parkingLot);
        userRepository.save(user);
        firebaseService.addParkingLot(savedParkingLot.getId(), savedParkingLot.getName(), savedParkingLot.getLatitude(),
                savedParkingLot.getLongitude(), savedParkingLot.getStar(), savedParkingLot.getCapacity() - savedParkingLot.getCurrent()
                , savedParkingLot.getPrice());
        firebaseService.setPending(savedParkingLot.getId(), 0);
        SimpleMailMessage registrationEmail = new SimpleMailMessage();
        registrationEmail.setTo(user.getEmail());
        registrationEmail.setSubject("Registration Success");
        registrationEmail.setText("Your registration for account " + user.getUsername() + " has been accepted. Download our app to begin now!");
        registrationEmail.setFrom("noreply@domain.com");
        emailService.sendEmail(registrationEmail);
        return ResponseEntity.ok(new MessageResponse("Create Success"));
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/signUp/index")
    public ResponseEntity<?> formIndex() {
        List<SignUpForm> formList = signUpFormRepository.findAllByStatus(SignUpFormStatus.PENDING);
        return ResponseEntity.ok(new BasicResourceResponse("OK", formList));
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/signUp/{id}")
    public ResponseEntity<?> getForm(@PathVariable Long id) {
        SignUpForm form = signUpFormRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Form", "ID", id));
        return ResponseEntity.ok(new BasicResourceResponse("OK", form));
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/signUp/deny/{id}")
    public ResponseEntity<?> denyRegister(@PathVariable Long id) {
        SignUpForm form = signUpFormRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Form", "ID", id));
        form.setStatus(SignUpFormStatus.DENIED);
        signUpFormRepository.save(form);
        SimpleMailMessage registrationEmail = new SimpleMailMessage();
        registrationEmail.setTo(form.getEmail());
        registrationEmail.setSubject("Registration Confirmation");
        registrationEmail.setText("Your registration for new Parking Lot have been denied. Sorry for the inconvenience");
        registrationEmail.setFrom("noreply@domain.com");
        emailService.sendEmail(registrationEmail);
        return ResponseEntity.ok(new MessageResponse("Deny Success"));
    }
}
