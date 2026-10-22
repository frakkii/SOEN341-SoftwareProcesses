package com.example.demo.dto;

// What the API shows for the logged-in user's own profile (job seeker or recruiter)
public record ProfileResponse(
        String name,
        String surname,
        String email,
        String phone,
        String city,
        String provinceState,
        String country,
        String role
) {
}
