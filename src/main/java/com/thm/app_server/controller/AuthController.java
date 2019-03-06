package com.thm.app_server.controller;

import com.thm.app_server.exception.AppException;
import com.thm.app_server.model.ParkingLot;
import com.thm.app_server.model.Role;
import com.thm.app_server.model.RoleName;
import com.thm.app_server.model.User;
import com.thm.app_server.payload.request.SignUpRequest;
import com.thm.app_server.payload.response.ApiResponse;
import com.thm.app_server.payload.response.JwtAuthenticationResponse;
import com.thm.app_server.payload.response.MessageResponse;
import com.thm.app_server.repository.RoleRepository;
import com.thm.app_server.repository.UserRepository;
import com.thm.app_server.security.JwtTokenProvider;
import com.thm.app_server.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider tokenProvider;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> authenticateUser(@RequestParam String usernameOrEmail, @RequestParam String password) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(jwt, RoleName.ROLE_USER.toString());

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            Long userId = ((UserPrincipal) principal).getId();
            User u = userRepository.findById(userId).orElse(null);
            if (u != null) {
                response.setFavorites(u.getFavorites().stream().map(ParkingLot::getId).collect(Collectors.toList()));
                response.setUsername(u.getUsername());
                response.setEmail(u.getEmail());
                if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(RoleName.ROLE_MANAGER.toString()))) {
                    response.setRole(RoleName.ROLE_MANAGER.toString());
                    response.setProperty(u.getProperty());
                }
            }
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest, Errors errors) {

        if (errors.hasErrors()) {
            return new ResponseEntity<>(new ApiResponse(false, errors.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(","))), HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{userId}")
                .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (passwordEncoder.matches(oldPassword, principal.getPassword())) {
            Long userId = principal.getId();
            User u = userRepository.findById(userId).orElse(null);
            if (u != null) {
                u.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(u);
                return ResponseEntity.ok(new MessageResponse("Password changed"));
            }
        }
        return new ResponseEntity<>(new MessageResponse("Password not correct"), HttpStatus.BAD_REQUEST);
    }
}