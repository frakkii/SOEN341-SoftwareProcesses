package com.example.demo.controller;

import com.example.demo.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Set<String> ALLOWED_ROLES = Set.of("Job Seeker", "Recruiter");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MAX_PASSWORD_BYTES = 72;

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public AuthController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (isBlank(user.getName()) ||
            isBlank(user.getEmail()) ||
            isBlank(user.getPassword())) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Name, email and password are required")
            );
        }

        String role = user.getRole() == null ? "Job Seeker" : user.getRole();

        if (!ALLOWED_ROLES.contains(role)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Role must be Job Seeker or Recruiter")
            );
        }

        String email = user.getEmail().toLowerCase().trim();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email address is not valid")
            );
        }

        if (isTooLong(user.getPassword())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Password is too long")
            );
        }

        String hashedPassword =
                passwordEncoder.encode(user.getPassword());

        User storedUser = new User(
                user.getName().trim(),
                email,
                hashedPassword,
                role
        );

        if (users.putIfAbsent(email, storedUser) != null) {
            return ResponseEntity.status(409).body(
                    Map.of("message", "User already exists")
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Account created successfully",
                        "email", email
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {

        if (isBlank(loginUser.getEmail()) ||
            isBlank(loginUser.getPassword())) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email and password are required")
            );
        }

        String email = loginUser.getEmail().toLowerCase().trim();

        User existingUser = users.get(email);

        if (existingUser == null || isTooLong(loginUser.getPassword())) {
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
                        "email", existingUser.getEmail(),
                        "role", existingUser.getRole()
                )
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // BCrypt only supports passwords up to 72 bytes and throws beyond that
    private static boolean isTooLong(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES;
    }
}