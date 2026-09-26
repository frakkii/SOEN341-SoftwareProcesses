package com.example.demo.dto;

// companyName is only used (and required) for recruiters
public record RegisterRequest(
        String role,
        String name,
        String surname,
        String email,
        String password,
        String phone,
        String address,
        String city,
        String provinceState,
        String country,
        String postalCodeZip,
        String companyName
) {
}
