package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.EmployerUser;
import com.example.demo.model.PersonUser;
import com.example.demo.repository.EmployerUserRepository;
import com.example.demo.repository.PersonUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String JOB_SEEKER = "Job Seeker";
    private static final String RECRUITER = "Recruiter";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9 ().-]{7,20}$");

    private final PersonUserRepository personUsers;
    private final EmployerUserRepository employerUsers;

    public AuthController(PersonUserRepository personUsers, EmployerUserRepository employerUsers) {
        this.personUsers = personUsers;
        this.employerUsers = employerUsers;
    }

    // A form field together with the column size it has to fit in
    private record Field(String label, String value, int maxLength) {
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        String role = request.role() == null ? JOB_SEEKER : request.role();

        if (!role.equals(JOB_SEEKER) && !role.equals(RECRUITER)) {
            return badRequest("Role must be Job Seeker or Recruiter");
        }

        List<Field> fields = new ArrayList<>(List.of(
                new Field("First name", request.name(), 255),
                new Field("Last name", request.surname(), 255),
                new Field("Email", request.email(), 255),
                new Field("Password", request.password(), 255),
                new Field("Phone", request.phone(), 20),
                new Field("Address", request.address(), 50),
                new Field("City", request.city(), 50),
                new Field("Province/State", request.provinceState(), 50),
                new Field("Country", request.country(), 50),
                new Field("Postal/ZIP code", request.postalCodeZip(), 15)
        ));
        if (role.equals(RECRUITER)) {
            fields.add(new Field("Company name", request.companyName(), 255));
        }

        List<String> missing = fields.stream()
                .filter(field -> isBlank(field.value()))
                .map(Field::label)
                .toList();

        if (!missing.isEmpty()) {
            return badRequest("Missing required fields: " + String.join(", ", missing));
        }

        String email = request.email().toLowerCase().trim();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return badRequest("Email address is not valid");
        }

        for (Field field : fields) {
            if (field.value().length() > field.maxLength()) {
                return badRequest(field.label() + " must be at most " + field.maxLength() + " characters");
            }
        }

        String phone = request.phone().trim();

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return badRequest("Phone number is not valid");
        }

        // Job seekers and recruiters live in separate tables, but an email may only
        // belong to one account so that login knows which one to use
        if (personUsers.existsByEmailIgnoreCase(email) || employerUsers.existsByEmailIgnoreCase(email)) {
            return userAlreadyExists();
        }

        // Passwords are stored as plain text to keep this school project simple
        String password = request.password();

        // The unique constraint on email also covers concurrent registrations
        try {
            if (role.equals(RECRUITER)) {
                employerUsers.saveAndFlush(new EmployerUser(
                        email,
                        request.name().trim(),
                        request.surname().trim(),
                        request.companyName().trim(),
                        request.country().trim(),
                        request.provinceState().trim(),
                        request.city().trim(),
                        request.address().trim(),
                        request.postalCodeZip().trim(),
                        phone,
                        password
                ));
            } else {
                personUsers.saveAndFlush(new PersonUser(
                        email,
                        request.name().trim(),
                        request.surname().trim(),
                        request.country().trim(),
                        request.provinceState().trim(),
                        request.city().trim(),
                        request.address().trim(),
                        request.postalCodeZip().trim(),
                        phone,
                        password
                ));
            }
        } catch (DataIntegrityViolationException e) {
            return userAlreadyExists();
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Account created successfully",
                        "email", email
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (isBlank(request.email()) || isBlank(request.password())) {
            return badRequest("Email and password are required");
        }

        String email = request.email().toLowerCase().trim();

        Optional<PersonUser> person = personUsers.findByEmailIgnoreCase(email);
        if (person.isPresent()) {
            PersonUser user = person.get();
            if (!user.getPassword().equals(request.password())) {
                return invalidCredentials();
            }
            return loginSuccess(user.getName(), user.getSurname(), user.getEmail(), JOB_SEEKER);
        }

        Optional<EmployerUser> employer = employerUsers.findByEmailIgnoreCase(email);
        if (employer.isPresent()) {
            EmployerUser user = employer.get();
            if (!user.getPassword().equals(request.password())) {
                return invalidCredentials();
            }
            return loginSuccess(user.getRecruiterName(), user.getRecruiterSurname(), user.getEmail(), RECRUITER);
        }

        return invalidCredentials();
    }

    private static ResponseEntity<?> loginSuccess(String name, String surname, String email, String role) {
        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "name", name,
                        "surname", surname,
                        "email", email,
                        "role", role
                )
        );
    }

    private static ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    private static ResponseEntity<?> userAlreadyExists() {
        return ResponseEntity.status(409).body(Map.of("message", "User already exists"));
    }

    private static ResponseEntity<?> invalidCredentials() {
        return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
