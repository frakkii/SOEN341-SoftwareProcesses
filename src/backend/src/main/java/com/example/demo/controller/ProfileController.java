package com.example.demo.controller;

import com.example.demo.exception.ApplicationException;
import com.example.demo.model.PersonUser;
import com.example.demo.repository.PersonUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

// The logged-in job seeker's profile. Like ResumeController, the job seeker comes
// from the session set up at login, never from the request, so users can only
// see and edit their own.
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final String JOB_SEEKER = "Job Seeker";
    private static final int MAX_SKILLS = 20;
    private static final int MAX_SKILL_LENGTH = 40;

    private final PersonUserRepository personUsers;

    public ProfileController(PersonUserRepository personUsers) {
        this.personUsers = personUsers;
    }

    // Body of PUT /api/profile. A location field left out (null) keeps its current value.
    public record ProfileRequest(String headline, String about, List<String> skills,
                                 String city, String provinceState, String country) {
    }

    @GetMapping
    public ResponseEntity<?> getProfile(HttpSession session) {
        return ResponseEntity.ok(toResponse(currentUser(session)));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody(required = false) ProfileRequest request, HttpSession session) {
        PersonUser user = currentUser(session);
        if (request == null) {
            throw new ApplicationException("Profile details are required.");
        }

        String headline = optional("Headline", request.headline(), 120);
        String about = optional("About", request.about(), 1200);
        String skills = joinSkills(request.skills());
        String city = location("City", request.city(), user.getCity());
        String provinceState = location("Province/State", request.provinceState(), user.getProvinceState());
        String country = location("Country", request.country(), user.getCountry());

        user.updateProfile(headline, about, skills, city, provinceState, country);
        PersonUser saved = personUsers.save(user);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Profile updated.");
        body.put("profile", toResponse(saved));
        return ResponseEntity.ok(body);
    }

    // LinkedHashMap rather than Map.of because headline, about and skills may be null
    private static Map<String, Object> toResponse(PersonUser user) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("name", user.getName());
        profile.put("surname", user.getSurname());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("city", user.getCity());
        profile.put("provinceState", user.getProvinceState());
        profile.put("country", user.getCountry());
        profile.put("headline", user.getHeadline());
        profile.put("about", user.getAbout());
        profile.put("skills", user.getSkills() == null || user.getSkills().isEmpty()
                ? List.of() : Arrays.asList(user.getSkills().split(",")));
        return profile;
    }

    // Trimmed text, or null if blank; 400 if it's too long
    private static String optional(String label, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new ApplicationException(label + " must be at most " + maxLength + " characters.");
        }
        return trimmed;
    }

    // Location fields are required at registration, so they can be changed but not cleared
    private static String location(String label, String value, String current) {
        if (value == null) {
            return current;
        }
        if (value.isBlank()) {
            throw new ApplicationException(label + " can't be empty.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > 50) {
            throw new ApplicationException(label + " must be at most 50 characters.");
        }
        return trimmed;
    }

    // Removes blanks and repeats (ignoring case) and joins with commas, which can't appear in a skill
    private static String joinSkills(List<String> skills) {
        if (skills == null) {
            return null;
        }
        List<String> cleaned = skills.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .filter(distinctIgnoringCase())
                .toList();
        if (cleaned.size() > MAX_SKILLS) {
            throw new ApplicationException("You can list at most " + MAX_SKILLS + " skills.");
        }
        for (String skill : cleaned) {
            if (skill.contains(",")) {
                throw new ApplicationException("Skills can't contain commas.");
            }
            if (skill.length() > MAX_SKILL_LENGTH) {
                throw new ApplicationException("Each skill must be at most " + MAX_SKILL_LENGTH + " characters.");
            }
        }
        return cleaned.isEmpty() ? null : String.join(",", cleaned);
    }

    private static Predicate<String> distinctIgnoringCase() {
        Set<String> seen = new HashSet<>();
        return s -> seen.add(s.toLowerCase(Locale.ROOT));
    }

    // The logged-in job seeker; 401 if nobody is logged in, 403 if it's a recruiter
    private PersonUser currentUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!JOB_SEEKER.equals(session.getAttribute(AuthController.SESSION_ROLE))) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Only job seekers have a profile.");
        }
        return personUsers.findById(userId).orElseThrow(() ->
                new ApplicationException(HttpStatus.UNAUTHORIZED, "Please sign in first."));
    }
}
