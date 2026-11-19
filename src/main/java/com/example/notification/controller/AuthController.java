package com.example.notification.controller;

import com.example.notification.dto.AuthRequest;
import com.example.notification.entity.Role;
import com.example.notification.entity.User;
import com.example.notification.repository.UserRepository;
import com.example.notification.security.TokenService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody AuthRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        userRepository.save(user);

        return Map.of("message", "Registered");
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody AuthRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String token = tokenService.generateToken(user);

        return Map.of("token", token);
    }
}