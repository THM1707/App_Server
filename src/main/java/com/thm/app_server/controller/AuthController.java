package com.thm.app_server.controller;

import com.thm.app_server.exception.AppException;
import com.thm.app_server.model.*;
import com.thm.app_server.payload.request.ManagerSignUpRequest;
import com.thm.app_server.payload.request.SignUpRequest;
import com.thm.app_server.payload.response.ApiResponse;
import com.thm.app_server.payload.response.JwtAuthenticationResponse;
import com.thm.app_server.payload.response.MessageResponse;
import com.thm.app_server.repository.ImageRepository;
import com.thm.app_server.repository.RoleRepository;
import com.thm.app_server.repository.SignUpFormRepository;
import com.thm.app_server.repository.UserRepository;
import com.thm.app_server.security.JwtTokenProvider;
import com.thm.app_server.security.UserPrincipal;
import com.thm.app_server.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider tokenProvider;

    private SignUpFormRepository signUpFormRepository;

    private ImageRepository imageRepository;

    private EmailService emailService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider, SignUpFormRepository signUpFormRepository, ImageRepository imageRepository, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.signUpFormRepository = signUpFormRepository;
        this.imageRepository = imageRepository;
        this.emailService = emailService;
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
                response.setEmail(u.getEmail());
                response.setName(u.getName());
                response.setGender(u.getGender());
                if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(RoleName.ROLE_MANAGER.toString()))) {
                    response.setRole(RoleName.ROLE_MANAGER.toString());
                    response.setProperty(u.getProperty());
                }
            }
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/signIn")
    public ResponseEntity<?> authenticateAdmin(@RequestParam String usernameOrEmail, @RequestParam String password) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(jwt, RoleName.ROLE_ADMIN.toString());

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            Long userId = ((UserPrincipal) principal).getId();
            User u = userRepository.findById(userId).orElse(null);
            if (u != null) {
                boolean isAdmin = false;
                for (Role role : u.getRoles()) {
                    if (role.getName().equals(RoleName.ROLE_ADMIN)) {
                        isAdmin = true;
                    }
                }
                if (!isAdmin) {
                    return new ResponseEntity<>(new MessageResponse("You are not an Admin!"), HttpStatus.UNAUTHORIZED);
                } else {
                    response.setGender(u.getGender());
                    response.setEmail(u.getEmail());
                    response.setName(u.getName());
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
                signUpRequest.getEmail(), signUpRequest.getPassword(),
                signUpRequest.getName(), signUpRequest.getPhone(), signUpRequest.getGender());

        user.setConfirmationToken(UUID.randomUUID().toString());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        String appUrl = "http://localhost:3000";

        SimpleMailMessage registrationEmail = new SimpleMailMessage();
        registrationEmail.setTo(user.getEmail());
        registrationEmail.setSubject("Registration Confirmation");
        registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
                + appUrl + "/confirmation?token=" + user.getConfirmationToken());
        registrationEmail.setFrom("noreply@domain.com");

        emailService.sendEmail(registrationEmail);

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{userId}")
                .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    public ResponseEntity<?> confirm(@RequestParam("token") String token) {

        User user = userRepository.findByConfirmationToken(token);

        if (user == null) {
            return new ResponseEntity<>(new MessageResponse("Invalid"), HttpStatus.BAD_REQUEST);
        } else {
            user.setEnabled(true);
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("OK"));
        }
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

    @PostMapping("/manager/signUp")
    public ResponseEntity<?> createManager(@Valid @RequestBody ManagerSignUpRequest request, Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseEntity<>(new MessageResponse("Validation error"), HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return new ResponseEntity<>(new MessageResponse("Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return new ResponseEntity<>(new MessageResponse("Email is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }
        Image image = new Image(request.getImage());
        imageRepository.save(image);
        SignUpForm form = new SignUpForm(request);
        form.setPassword(passwordEncoder.encode(request.getPassword()));
        form.setImage(image);
        signUpFormRepository.save(form);
        return ResponseEntity.ok(new MessageResponse("OK"));
    }
}