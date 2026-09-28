package com.example.demo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationTest {

    @Test
    void submitThenApproveShouldSetStatusToApproved() {
        Application application = new Application("Test Application");

        application.submit();
        application.approve();

        assertEquals("Approved", application.getStatus());
    }

    @Test
    void submitThenRejectShouldSetStatusToRejected() {
        Application application = new Application("Test Application");

        application.submit();
        application.reject();

        assertEquals("Rejected", application.getStatus());
    }
    @Test 
    void approveWithoutSubmit() {
        Application application = new Application("Test Application");

        application.approve();

        assertEquals("Pending", application.getStatus());
    }
    @Test
    void rejectWithoutSubmit() {
        Application application = new Application("Test Application");

        application.reject();

        assertEquals("Pending", application.getStatus());
    }
    @Test 
    void approveAfterReject() {
        Application application = new Application("Test Application");

        application.submit();
        application.reject();
        application.approve();

        assertEquals("Rejected", application.getStatus());
    }

}
