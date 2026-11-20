package com.example.demo.service;

import com.example.demo.exception.ApplicationException;
import com.example.demo.model.Application;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.EmployerUserRepository;
import com.example.demo.repository.JobPostingRepository;
import com.example.demo.repository.ResumeRepository;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// What applying says when saving the application fails at the database
class ApplicationServiceTest {

    private static final int PERSON = 7;
    private static final int POST = 42;

    private final ApplicationRepository applications = mock(ApplicationRepository.class);
    private final ResumeRepository resumes = mock(ResumeRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final ApplicationService service = new ApplicationService(applications, resumes,
            mock(JobPostingRepository.class), mock(EmployerUserRepository.class), notifications);

    // The service's log goes here instead of the console, so the expected error doesn't print a
    // stack trace in the test output, and the test can check what was logged
    private final Logger log = (Logger) LoggerFactory.getLogger(ApplicationService.class);
    private final ListAppender<ILoggingEvent> logged = new ListAppender<>();

    @BeforeEach
    void captureLog() {
        logged.start();
        log.addAppender(logged);
        log.setAdditive(false);
    }

    @AfterEach
    void restoreLog() {
        log.detachAppender(logged);
        log.setAdditive(true);
    }

    // Every check before the save passes (the posting exists and is open, the job seeker has a
    // resume), then saving fails at the database
    @BeforeEach
    void savingFails() {
        when(applications.countPostings(POST)).thenReturn(1L);
        when(applications.countOpenPostings(POST)).thenReturn(1L);
        when(resumes.existsByPersonIdAndCurrentTrue(PERSON)).thenReturn(true);
        when(applications.saveAndFlush(any(Application.class)))
                .thenThrow(new DataIntegrityViolationException("constraint failed"));
    }

    @Test
    void aDuplicateSavedByAnotherRequestStillSaysAlreadyApplied() {
        // Not applied when checked, but another request saved the same application first
        when(applications.existsByPersonIdAndPostId(PERSON, POST)).thenReturn(false, true);

        assertThatThrownBy(() -> service.submitApplication(PERSON, POST))
                .isInstanceOfSatisfying(ApplicationException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getMessage())
                            .isEqualTo("You have already applied to this job.");
                });
        verifyNoInteractions(notifications);
        assertThat(logged.list).isEmpty();   // a duplicate is normal, not an error
    }

    @Test
    void anyOtherDatabaseErrorIsNotReportedAsAlreadyApplied() {
        when(applications.existsByPersonIdAndPostId(PERSON, POST)).thenReturn(false);

        assertThatThrownBy(() -> service.submitApplication(PERSON, POST))
                .isInstanceOfSatisfying(ApplicationException.class, e -> {
                    assertThat(e.getStatus())
                            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(e.getMessage())
                            .isEqualTo("Your application couldn't be saved. Please try again.");
                });
        verifyNoInteractions(notifications);
        // The real cause is logged so the problem can be found
        assertThat(logged.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage()).isEqualTo("Could not save application of person 7 to posting 42");
            assertThat(event.getThrowableProxy().getClassName())
                    .isEqualTo(DataIntegrityViolationException.class.getName());
        });
    }
}
