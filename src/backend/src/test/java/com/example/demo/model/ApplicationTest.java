package com.example.demo.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ApplicationTest {

    @Test
    void newApplicationStartsAsApplied() {
        Application application = new Application(1, 2);

        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
        assertEquals(1, application.getPersonId().intValue());
        assertEquals(2, application.getPostId().intValue());
        assertEquals(application.getAppliedAt(), application.getUpdatedAt());
    }

    @Test
    void updateStatusChangesStatusAndUpdatedTime() {
        Application application = new Application(1, 2);

        application.updateStatus(ApplicationStatus.INTERVIEW);

        assertEquals(ApplicationStatus.INTERVIEW, application.getStatus());
        assertFalse(application.getUpdatedAt().isBefore(application.getAppliedAt()));
    }

    @Test
    void allFiveRequiredStatusesExist() {
        List<String> labels = Arrays.stream(ApplicationStatus.values()).map(ApplicationStatus::getLabel).toList();

        assertEquals(List.of("Applied", "Under Review", "Interview", "Offered", "Rejected"), labels);
    }
}
