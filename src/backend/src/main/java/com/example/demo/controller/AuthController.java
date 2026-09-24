package com.example.demo.controller;

import com.example.demo.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final Map<String, User> users = new HashMap<>();
    private final PasswordEncoder passwordEncoder;

    public AuthController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (user.getName() == null ||
            user.getEmail() == null ||
            user.getPassword() == null) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Name, email and password are required")
            );
        }

        String email = user.getEmail().toLowerCase().trim();

        if (users.containsKey(email)) {
            return ResponseEntity.status(409).body(
                    Map.of("message", "User already exists")
            );
        }

        String hashedPassword =
                passwordEncoder.encode(user.getPassword());

        User storedUser = new User(
                user.getName(),
                email,
                hashedPassword
        );

        users.put(email, storedUser);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Account created successfully",
                        "email", email
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {

        if (loginUser.getEmail() == null ||
            loginUser.getPassword() == null) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email and password are required")
            );
        }

        String email = loginUser.getEmail().toLowerCase().trim();

        User existingUser = users.get(email);

        if (existingUser == null) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Invalid email or password")
            );
        }

        boolean passwordMatches =
                passwordEncoder.matches(
                        loginUser.getPassword(),
                        existingUser.getPassword()
                );

        if (!passwordMatches) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Invalid email or password")
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "name", existingUser.getName(),
                        "email", existingUser.getEmail()
                )
        );
    }
}