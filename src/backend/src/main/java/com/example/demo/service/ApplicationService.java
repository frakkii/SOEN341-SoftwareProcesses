package com.example.demo.service;

import com.example.demo.dto.ApplicationResponse;
import com.example.demo.exception.ApplicationException;
import com.example.demo.model.Application;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ResumeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

// Submitting and tracking job applications. The controller works out who is
// logged in; this class only receives their id.
@Service
public class ApplicationService {

    private final ApplicationRepository applications;
    private final ResumeRepository resumes;

    public ApplicationService(ApplicationRepository applications, ResumeRepository resumes) {
        this.applications = applications;
        this.resumes = resumes;
    }

    public ApplicationResponse submitApplication(Integer personId, Integer postId) {
        if (postId == null) {
            throw new ApplicationException("Job posting id is required.");
        }
        if (applications.countPostings(postId) == 0) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Job posting not found.");
        }
        if (applications.existsByPersonIdAndPostId(personId, postId)) {
            throw alreadyApplied();
        }
        if (applications.countOpenPostings(postId) == 0) {
            throw new ApplicationException("This job posting is no longer accepting applications.");
        }
        if (!resumes.existsById(personId)) {
            throw new ApplicationException("Please upload your resume before applying.");
        }

        // The unique constraint on (person_id, post_id) also covers two requests at once
        try {
            return ApplicationResponse.from(applications.saveAndFlush(new Application(personId, postId)));
        } catch (DataIntegrityViolationException e) {
            throw alreadyApplied();
        }
    }

    public List<ApplicationResponse> getApplications(Integer personId) {
        return applications.findByPersonIdOrderByAppliedAtDesc(personId).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    public ApplicationResponse getApplication(Integer personId, Long applicationId) {
        // Another job seeker's application looks the same as a missing one
        return applications.findByIdAndPersonId(applicationId, personId)
                .map(ApplicationResponse::from)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Application not found."));
    }

    private static ApplicationException alreadyApplied() {
        return new ApplicationException(HttpStatus.CONFLICT, "You have already applied to this job.");
    }
}
