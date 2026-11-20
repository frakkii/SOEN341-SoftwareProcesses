package com.example.demo.service;

import com.example.demo.dto.ApplicantResponse;
import com.example.demo.dto.PostingUpdateRequest;
import com.example.demo.exception.ApplicationException;
import com.example.demo.model.Application;
import com.example.demo.model.ApplicationStatus;
import com.example.demo.model.JobPosting;
import com.example.demo.model.PersonUser;
import com.example.demo.model.Resume;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostingRepository;
import com.example.demo.repository.PersonUserRepository;
import com.example.demo.repository.ResumeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// A recruiter reviewing the applications to their own postings. The controller
// works out who is logged in; this class only receives their id. Postings and
// applications that belong to another recruiter look the same as missing ones.
@Service
public class RecruiterService {

    private final JobPostingRepository postings;
    private final ApplicationRepository applications;
    private final PersonUserRepository personUsers;
    private final ResumeRepository resumes;
    private final NotificationService notifications;

    public RecruiterService(JobPostingRepository postings, ApplicationRepository applications,
                            PersonUserRepository personUsers, ResumeRepository resumes,
                            NotificationService notifications) {
        this.postings = postings;
        this.applications = applications;
        this.personUsers = personUsers;
        this.resumes = resumes;
        this.notifications = notifications;
    }

    public List<ApplicantResponse> getApplicants(Integer employerId, Integer postId) {
        ownPosting(employerId, postId);

        List<Application> received = applications.findByPostIdOrderByAppliedAtDesc(postId);
        List<Integer> personIds = received.stream().map(Application::getPersonId).toList();
        Map<Integer, PersonUser> people = personUsers.findAllById(personIds).stream()
                .collect(Collectors.toMap(PersonUser::getId, Function.identity()));

        return received.stream()
                .map(a -> ApplicantResponse.from(a, people.get(a.getPersonId()), resumes.existsByPersonIdAndCurrentTrue(a.getPersonId())))
                .toList();
    }

    public ApplicantResponse updateStatus(Integer employerId, Long applicationId, String newStatus) {
        ApplicationStatus next = ApplicationStatus.parse(newStatus).orElseThrow(() -> new ApplicationException(
                "Status must be one of: Applied, Under Review, Interview, Offered, Rejected."));

        Application application = ownApplication(employerId, applicationId);
        ApplicationStatus current = application.getStatus();
        if (current.isFinal()) {
            throw new ApplicationException(HttpStatus.CONFLICT,
                    "This application is already " + current.getLabel() + " and can't be changed.");
        }
        if (!current.canMoveTo(next)) {
            throw new ApplicationException(HttpStatus.CONFLICT,
                    "An application can't move from " + current.getLabel() + " back to " + next.getLabel() + ".");
        }

        application.updateStatus(next);
        Application saved = applications.save(application);
        PersonUser person = personUsers.findById(saved.getPersonId()).orElseThrow();
        JobPosting posting = postings.findById(saved.getPostId()).orElseThrow();
        boolean interview = next == ApplicationStatus.INTERVIEW;
        notifications.create(
                "Job Seeker",
                saved.getPersonId(),
                interview ? "Interview invitation" : "Application status updated",
                interview
                        ? "You have been invited to an interview for " + posting.getTitle() + "."
                        : "Your application for " + posting.getTitle() + " is now " + next.getLabel() + ".",
                interview ? NotificationService.INTERVIEW_INVITATION : NotificationService.APPLICATION_STATUS_UPDATED,
                "/applications.html",
                "APPLICATION_STATUS:" + saved.getId() + ":" + next.name());
        return ApplicantResponse.from(saved, person, resumes.existsByPersonIdAndCurrentTrue(saved.getPersonId()));
    }

    public Resume getApplicantResume(Integer employerId, Long applicationId) {
        Application application = ownApplication(employerId, applicationId);
        return resumes.findFirstByPersonIdAndCurrentTrueOrderByUploadedAtDesc(application.getPersonId()).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "This applicant has no resume on file."));
    }

    // Open, Closed or Archived. Archiving also closes the posting; reopening an archived one restores it
    public JobPosting setPostingStatus(Integer employerId, Integer postId, String status) {
        String normalized = status == null ? "" : status.trim();
        String value = Stream.of(JobPosting.OPEN, JobPosting.CLOSED, JobPosting.ARCHIVED)
                .filter(s -> s.equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new ApplicationException("Posting status must be Open, Closed or Archived."));

        JobPosting posting = ownPosting(employerId, postId);
        posting.setStatus(value);
        return postings.save(posting);
    }

    // Replaces a posting's details with the same rules as creating one. The status is unchanged.
    public JobPosting editPosting(Integer employerId, Integer postId, PostingUpdateRequest request) {
        if (request == null) {
            throw new ApplicationException("Posting details are required.");
        }
        String title = trimmed(request.title());
        if (title.isEmpty()) {
            throw new ApplicationException("Job title is required.");
        }
        maxLength("Job title", title, 255);
        if (request.applicationDeadline() == null) {
            throw new ApplicationException("Application deadline is required.");
        }
        if (request.applicationDeadline().isBefore(LocalDate.now())) {
            throw new ApplicationException("Application deadline can't be in the past.");
        }
        String department = maxLength("Department", trimmed(request.department()), 255);
        String salary = maxLength("Salary", trimmed(request.salary()), 255);
        String location = maxLength("Location", trimmed(request.location()), 255);
        String industry = maxLength("Industry", trimmed(request.industry()), 255);
        String jobType = maxLength("Job type", trimmed(request.jobType()), 100);

        JobPosting posting = ownPosting(employerId, postId);
        posting.edit(title, department, salary, trimmed(request.requiredSkills()), trimmed(request.description()),
                request.applicationDeadline(),
                location.isEmpty() ? "Not specified" : location,     // same default as creating a posting
                industry.isEmpty() ? null : industry,
                jobType.isEmpty() ? null : jobType);
        return postings.save(posting);
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private static String maxLength(String field, String value, int max) {
        if (value.length() > max) {
            throw new ApplicationException(field + " must be at most " + max + " characters.");
        }
        return value;
    }

    // Number of applications for each posting id; postings with none are left out
    public Map<Integer, Long> countApplicants(Collection<Integer> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        return applications.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(ApplicationRepository.PostingCount::getPostId,
                        ApplicationRepository.PostingCount::getTotal));
    }

    private JobPosting ownPosting(Integer employerId, Integer postId) {
        return postings.findByIdAndEmployerId(postId, employerId).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, "Job posting not found."));
    }

    private Application ownApplication(Integer employerId, Long applicationId) {
        Application application = applications.findById(applicationId).orElseThrow(RecruiterService::applicationNotFound);
        if (postings.findByIdAndEmployerId(application.getPostId(), employerId).isEmpty()) {
            throw applicationNotFound();
        }
        return application;
    }

    private static ApplicationException applicationNotFound() {
        return new ApplicationException(HttpStatus.NOT_FOUND, "Application not found.");
    }
}
