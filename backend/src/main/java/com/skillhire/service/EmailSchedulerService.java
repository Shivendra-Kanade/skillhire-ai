package com.skillhire.service;

import com.skillhire.entity.Job;
import com.skillhire.entity.User;
import com.skillhire.repository.JobRepository;
import com.skillhire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Email Scheduler Service
 * Runs periodic tasks for email notifications.
 * Enable scheduling in main class with @EnableScheduling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSchedulerService {

    private final EmailNotificationService emailNotificationService;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    /**
     * Send weekly digest every Monday at 9:00 AM IST
     * Cron: sec min hour day month weekday
     */
    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Kolkata")
    public void sendWeeklyJobDigest() {
        log.info("Starting weekly job digest scheduler...");

        try {
            // Get all active jobs from the past 7 days
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            List<Job> recentJobs = jobRepository.findRecentActiveJobs(oneWeekAgo);

            if (recentJobs.isEmpty()) {
                log.info("No new jobs this week. Skipping weekly digest.");
                return;
            }

            // Get all candidates with notifications enabled
            List<User> candidates = userRepository.findByRoleAndEmailNotificationsEnabled(
                    User.Role.CANDIDATE, true);

            log.info("Sending weekly digest to {} candidates with {} jobs", candidates.size(), recentJobs.size());

            for (User candidate : candidates) {
                emailNotificationService.sendWeeklyJobDigest(candidate, recentJobs);
            }

            log.info("Weekly digest scheduler completed.");
        } catch (Exception e) {
            log.error("Error in weekly digest scheduler: {}", e.getMessage(), e);
        }
    }

    /**
     * Check for new job postings and notify candidates - runs every 6 hours
     */
    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000) // 6 hours in ms
    public void notifyNewJobPostings() {
        log.info("Checking for new job postings to notify...");

        try {
            LocalDateTime sixHoursAgo = LocalDateTime.now().minusHours(6);
            List<Job> newJobs = jobRepository.findRecentActiveJobs(sixHoursAgo);

            if (!newJobs.isEmpty()) {
                List<User> candidates = userRepository.findByRoleAndEmailNotificationsEnabled(
                        User.Role.CANDIDATE, true);
                log.info("Found {} new jobs, notifying {} candidates", newJobs.size(), candidates.size());

                // Batch: send max 3 job notifications per user per run
                for (User candidate : candidates) {
                    List<Job> jobsToNotify = newJobs.stream().limit(3).toList();
                    for (Job job : jobsToNotify) {
                        emailNotificationService.sendJobMatchNotification(candidate, job);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in new job notification scheduler: {}", e.getMessage(), e);
        }
    }
}
