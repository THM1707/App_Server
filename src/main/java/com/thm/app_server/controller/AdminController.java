package com.thm.app_server.controller;

import com.thm.app_server.exception.ResourceNotFoundException;
import com.thm.app_server.model.*;
import com.thm.app_server.payload.response.BasicResourceResponse;
import com.thm.app_server.payload.response.MessageResponse;
import com.thm.app_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private SignUpFormRepository signUpFormRepository;

    private ParkingLotRepository parkingLotRepository;

    @Autowired
    public AdminController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, SignUpFormRepository signUpFormRepository, ParkingLotRepository parkingLotRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.signUpFormRepository = signUpFormRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/signUp/accept/{id}")
    public ResponseEntity<?> acceptRegister(@PathVariable Long id) {
        SignUpForm form = signUpFormRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Form", "ID", id));
        User user = new User(form.getUsername(), form.getEmail(), form.getPassword());
        RoleName[] roleNameArray = {RoleName.ROLE_MANAGER, RoleName.ROLE_USER};
        List<Role> roleList = roleRepository.findByNameIn(roleNameArray);
        user.setRoles(new HashSet<>(roleList));
        ParkingLot parkingLot = new ParkingLot(form.getName(), form.getAddress(), form.getLatitude(), form.getLongitude(), form.getCapacity(), form.getOpenTime(), form.getCloseTime());
        Image image = form.getImage();
        parkingLot.setImage(image);
        user.setProperty(parkingLot);
        form.setStatus(SignUpFormStatus.ACCEPTED);
        signUpFormRepository.save(form);
        parkingLotRepository.save(parkingLot);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Create Success"));
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/signUp/index")
    public ResponseEntity<?> formIndex() {
        List<SignUpForm> formList = signUpFormRepository.findAllByStatus(SignUpFormStatus.PENDING);
        return ResponseEntity.ok(new BasicResourceResponse("OK", formList));
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/signUp/deny/{id}")
    public ResponseEntity<?> denyRegister(@PathVariable Long id) {
        SignUpForm form = signUpFormRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Form", "ID", id));
        form.setStatus(SignUpFormStatus.DENIED);
        signUpFormRepository.save(form);
        return ResponseEntity.ok(new MessageResponse("Deny Success"));
    }
}
