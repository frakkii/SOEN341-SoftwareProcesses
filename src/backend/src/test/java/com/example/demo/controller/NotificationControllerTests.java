package com.example.demo.controller;

import com.example.demo.service.NotificationService;
import com.example.demo.service.DeadlineReminderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTests {

    private static final AtomicInteger NEXT_POST_ID = new AtomicInteger(9000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notifications;

    @Autowired
    private DeadlineReminderService deadlineReminders;

    @Autowired
    private JdbcTemplate jdbc;

    private MockHttpSession loggedInJobSeeker() throws Exception {
        String email = "notify-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"Job Seeker\",\"name\":\"Jane\",\"surname\":\"Doe\","
                                + "\"email\":\"" + email + "\",\"password\":\"secret123\","
                                + "\"phone\":\"514-555-1234\",\"address\":\"1455 De Maisonneuve Blvd W\","
                                + "\"city\":\"Montreal\",\"provinceState\":\"Quebec\",\"country\":\"Canada\","
                                + "\"postalCodeZip\":\"H3G 1M8\"}"))
                .andExpect(status().isOk());

        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();
    }

    private MockHttpSession loggedInRecruiter() throws Exception {
        String email = "recruiter-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"Recruiter\",\"name\":\"Riley\",\"surname\":\"Recruiter\","
                                + "\"companyName\":\"Acme\",\"email\":\"" + email + "\","
                                + "\"password\":\"secret123\",\"phone\":\"514-555-1234\","
                                + "\"address\":\"1455 De Maisonneuve Blvd W\",\"city\":\"Montreal\","
                                + "\"provinceState\":\"Quebec\",\"country\":\"Canada\","
                                + "\"postalCodeZip\":\"H3G 1M8\"}"))
                .andExpect(status().isOk());

        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();
    }

    private int insertSavedPosting(Integer personId, Integer employerId, LocalDate deadline) {
        int postId = NEXT_POST_ID.getAndIncrement();
        jdbc.update("INSERT INTO job_postings (employer_id, post_id, title, description, department, "
                        + "application_deadline, salary, required_skills, status) "
                        + "VALUES (?, ?, 'Saved posting', 'Description', 'Engineering', ?, '60000', 'Java', 'Open')",
                employerId, postId, Date.valueOf(deadline));
        jdbc.update("INSERT INTO fav_postings (person_id, post_id, status) VALUES (?, ?, TRUE)",
                personId, postId);
        return postId;
    }

    @Test
    void userCanListFilterAndMarkOnlyTheirOwnNotificationsRead() throws Exception {
        MockHttpSession session = loggedInJobSeeker();
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        notifications.create("Job Seeker", userId, "Interview update", "Your application changed.",
                "APPLICATION_STATUS", "/applications.html", null);

        String listResponse = mockMvc.perform(get("/api/notifications").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Interview update"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/notifications").param("unreadOnly", "true").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/notifications/unread-count").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
        Number notificationId = com.jayway.jsonpath.JsonPath.read(listResponse, "$[0].id");
        long id = notificationId.longValue();

        mockMvc.perform(patch("/api/notifications/{id}/read", id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt").isNotEmpty());

        mockMvc.perform(get("/api/notifications/unread-count").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void notificationEndpointsRequireLogin() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotMarkAnotherUsersNotificationRead() throws Exception {
        MockHttpSession ownerSession = loggedInJobSeeker();
        Integer ownerId = (Integer) ownerSession.getAttribute(AuthController.SESSION_USER_ID);
        Long notificationId = notifications.create("Job Seeker", ownerId, "Private", "Private message.",
                "APPLICATION_STATUS", null, null).getId();
        MockHttpSession anotherSession = loggedInJobSeeker();

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId).session(anotherSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void savedPostingNearDeadlineGetsOneReminderEvenWhenSchedulerRunsTwice() throws Exception {
        MockHttpSession seeker = loggedInJobSeeker();
        Integer personId = (Integer) seeker.getAttribute(AuthController.SESSION_USER_ID);
        MockHttpSession recruiter = loggedInRecruiter();
        Integer employerId = (Integer) recruiter.getAttribute(AuthController.SESSION_USER_ID);
        insertSavedPosting(personId, employerId, LocalDate.now().plusDays(1));

        deadlineReminders.createUpcomingDeadlineReminders();
        deadlineReminders.createUpcomingDeadlineReminders();

        var result = notifications.list("Job Seeker", personId, false);
        org.assertj.core.api.Assertions.assertThat(result)
                .filteredOn(notification -> notification.type().equals("JOB_DEADLINE"))
                .hasSize(1);
    }

    @Test
    void expiredDeadlinesAreIgnoredAndRemindersStayWithTheSavedJobOwner() throws Exception {
        MockHttpSession ownerSession = loggedInJobSeeker();
        Integer ownerId = (Integer) ownerSession.getAttribute(AuthController.SESSION_USER_ID);
        MockHttpSession anotherSession = loggedInJobSeeker();
        Integer anotherUserId = (Integer) anotherSession.getAttribute(AuthController.SESSION_USER_ID);
        MockHttpSession recruiter = loggedInRecruiter();
        Integer employerId = (Integer) recruiter.getAttribute(AuthController.SESSION_USER_ID);

        insertSavedPosting(ownerId, employerId, LocalDate.now().plusDays(1));
        insertSavedPosting(ownerId, employerId, LocalDate.now().minusDays(1));

        deadlineReminders.createUpcomingDeadlineReminders();

        var ownerNotifications = notifications.list("Job Seeker", ownerId, false);
        org.assertj.core.api.Assertions.assertThat(ownerNotifications)
                .filteredOn(notification -> notification.type().equals("JOB_DEADLINE"))
                .hasSize(1)
                .allSatisfy(notification ->
                        org.assertj.core.api.Assertions.assertThat(notification.message())
                                .contains("Saved posting")
                                .contains(LocalDate.now().plusDays(1).toString()));
        org.assertj.core.api.Assertions.assertThat(notifications.list("Job Seeker", anotherUserId, false))
                .noneMatch(notification -> notification.type().equals("JOB_DEADLINE"));
    }
}
