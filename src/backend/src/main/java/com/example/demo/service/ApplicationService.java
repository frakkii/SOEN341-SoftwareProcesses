package com.example.demo.service;

import com.example.demo.dto.ApplicationResponse;
import com.example.demo.exception.ApplicationException;
import com.example.demo.model.Application;
import com.example.demo.model.EmployerUser;
import com.example.demo.model.JobPosting;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.EmployerUserRepository;
import com.example.demo.repository.JobPostingRepository;
import com.example.demo.repository.ResumeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Submitting and tracking job applications. The controller works out who is
// logged in; this class only receives their id.
@Service
public class ApplicationService {

    private final ApplicationRepository applications;
    private final ResumeRepository resumes;
    private final JobPostingRepository postings;
    private final EmployerUserRepository employers;

    public ApplicationService(ApplicationRepository applications, ResumeRepository resumes,
                              JobPostingRepository postings, EmployerUserRepository employers) {
        this.applications = applications;
        this.resumes = resumes;
        this.postings = postings;
        this.employers = employers;
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
            return describe(List.of(applications.saveAndFlush(new Application(personId, postId)))).get(0);
        } catch (DataIntegrityViolationException e) {
            throw alreadyApplied();
        }
    }

    public List<ApplicationResponse> getApplications(Integer personId) {
        return describe(applications.findByPersonIdOrderByAppliedAtDesc(personId));
    }

    public ApplicationResponse getApplication(Integer personId, Long applicationId) {
        // Another job seeker's application looks the same as a missing one
        Application application = applications.findByIdAndPersonId(applicationId, personId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Application not found."));
        return describe(List.of(application)).get(0);
    }

    // Adds each application's job title and company, loading the postings and employers in two queries
    private List<ApplicationResponse> describe(List<Application> list) {
        Map<Integer, JobPosting> postingsById = postings.findAllById(
                        list.stream().map(Application::getPostId).distinct().toList()).stream()
                .collect(Collectors.toMap(JobPosting::getId, Function.identity()));
        Map<Integer, String> companies = employers.findAllById(
                        postingsById.values().stream().map(JobPosting::getEmployerId).distinct().toList()).stream()
                .collect(Collectors.toMap(EmployerUser::getId, EmployerUser::getCompanyName));

        return list.stream().map(a -> {
            JobPosting posting = postingsById.get(a.getPostId());
            return ApplicationResponse.from(a,
                    posting == null ? null : posting.getTitle(),
                    posting == null ? null : companies.get(posting.getEmployerId()));
        }).toList();
    }

    private static ApplicationException alreadyApplied() {
        return new ApplicationException(HttpStatus.CONFLICT, "You have already applied to this job.");
    }
}
