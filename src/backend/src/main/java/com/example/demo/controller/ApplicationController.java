package com.example.demo.controller;

import com.example.demo.service.ApplicationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }
    @PostMapping("/{id}/resume")
    public ResponseEntity<String> uploadResume(@PathVariable Long id, @RequestParam("resume") MultipartFile resume) {
        applicationService.uploadResume(id, resume);
        return ResponseEntity.ok("Resume uploaded successfully.");
    }
    @PostMapping("/{id}/submit")
    public ResponseEntity<String> submitApplication(@PathVariable Long id) {
        applicationService.submitApplication(id);
        return ResponseEntity.ok("Application submitted successfully.");
    }
    

    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approveApplication(@PathVariable Long id) {
        applicationService.approveApplication(id);
        return ResponseEntity.ok("Application approved successfully.");
    }
    @PostMapping("/{id}/reject")
    public ResponseEntity<String> rejectApplication(@PathVariable Long id) {
        applicationService.rejectApplication(id);
        return ResponseEntity.ok("Application rejected successfully.");
    }
    
    
    
} 
