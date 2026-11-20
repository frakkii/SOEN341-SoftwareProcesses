package com.example.demo.service;

import com.example.demo.repository.JobPostingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DeadlineReminderService {

    private static final String JOB_SEEKER = "Job Seeker";
    private static final int REMINDER_DAYS_BEFORE_DEADLINE = 1;

    private final JobPostingRepository postings;
    private final NotificationService notifications;

    public DeadlineReminderService(JobPostingRepository postings, NotificationService notifications) {
        this.postings = postings;
        this.notifications = notifications;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void createUpcomingDeadlineReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(REMINDER_DAYS_BEFORE_DEADLINE);
        postings.findSavedPostingsWithDeadline(reminderDate)
                .forEach(candidate -> notifications.create(
                        JOB_SEEKER,
                        candidate.getPersonId(),
                        "Saved job deadline approaching",
                        candidate.getTitle() + " closes on " + candidate.getApplicationDeadline() + ".",
                        NotificationService.DEADLINE_REMINDER,
                        "/job.html?id=" + candidate.getPostId(),
                        "JOB_DEADLINE:1D:" + candidate.getPersonId() + ":" + candidate.getPostId()
                                + ":" + candidate.getApplicationDeadline()));
    }
}
