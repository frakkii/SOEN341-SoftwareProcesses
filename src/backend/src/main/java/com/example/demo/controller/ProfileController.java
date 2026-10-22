package com.example.demo.controller;

import com.example.demo.dto.ProfileResponse;
import com.example.demo.model.EmployerUser;
import com.example.demo.model.PersonUser;
import com.example.demo.repository.EmployerUserRepository;
import com.example.demo.repository.PersonUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

// The logged-in user's own profile details. Unlike ResumeController/ApplicationController,
// this is open to both job seekers and recruiters, since both have a profile to view.
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final String RECRUITER = "Recruiter";

    private final PersonUserRepository personUsers;
    private final EmployerUserRepository employerUsers;

    public ProfileController(PersonUserRepository personUsers, EmployerUserRepository employerUsers) {
        this.personUsers = personUsers;
        this.employerUsers = employerUsers;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Please sign in to view your profile."));
        }
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);

        if (RECRUITER.equals(role)) {
            Optional<EmployerUser> employer = employerUsers.findById(userId);
            if (employer.isEmpty()) {
                return notFound();
            }
            EmployerUser user = employer.get();
            return ResponseEntity.ok(new ProfileResponse(
                    user.getRecruiterName(), user.getRecruiterSurname(), user.getEmail(), user.getPhone(),
                    user.getCity(), user.getProvinceState(), user.getCountry(), role
            ));
        }

        Optional<PersonUser> person = personUsers.findById(userId);
        if (person.isEmpty()) {
            return notFound();
        }
        PersonUser user = person.get();
        return ResponseEntity.ok(new ProfileResponse(
                user.getName(), user.getSurname(), user.getEmail(), user.getPhone(),
                user.getCity(), user.getProvinceState(), user.getCountry(), role
        ));
    }

    private static ResponseEntity<?> notFound() {
        return ResponseEntity.status(404).body(Map.of("message", "Profile not found."));
    }
}
