package com.skillhire.service;

import com.skillhire.entity.Job;
import com.skillhire.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Email Notification Service
 * Sends professional HTML email notifications for job postings,
 * job matches, application updates, and weekly digests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // =====================================================
    // JOB POSTING NOTIFICATION
    // =====================================================

    /**
     * Send notification to a candidate about a new matching job
     */
    @Async
    public void sendJobMatchNotification(User candidate, Job job) {
        if (!candidate.isEmailNotificationsEnabled()) {
            log.debug("Email notifications disabled for user: {}", candidate.getEmail());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "SkillHire AI");
            helper.setTo(candidate.getEmail());
            helper.setSubject("🎯 New Job Match: " + job.getTitle() + " at " + job.getCompany());
            helper.setText(buildJobMatchEmail(candidate, job), true);

            mailSender.send(message);
            log.info("Job match email sent to: {} for job: {}", candidate.getEmail(), job.getTitle());
        } catch (Exception e) {
            log.error("Failed to send job match email to {}: {}", candidate.getEmail(), e.getMessage());
        }
    }

    /**
     * Send new job posting notification to all subscribed candidates
     */
    @Async
    public void sendNewJobPostingNotification(List<User> candidates, Job job) {
        for (User candidate : candidates) {
            sendJobMatchNotification(candidate, job);
        }
    }

    /**
     * Send application status update to a candidate
     */
    @Async
    public void sendApplicationStatusUpdate(User candidate, Job job, String status) {
        if (!candidate.isEmailNotificationsEnabled()) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "SkillHire AI");
            helper.setTo(candidate.getEmail());
            helper.setSubject(getStatusSubject(status) + ": " + job.getTitle() + " at " + job.getCompany());
            helper.setText(buildApplicationStatusEmail(candidate, job, status), true);

            mailSender.send(message);
            log.info("Application status email sent to: {}", candidate.getEmail());
        } catch (Exception e) {
            log.error("Failed to send application status email: {}", e.getMessage());
        }
    }

    /**
     * Send weekly job digest to all subscribed candidates
     */
    @Async
    public void sendWeeklyJobDigest(User candidate, List<Job> jobs) {
        if (!candidate.isEmailNotificationsEnabled() || jobs.isEmpty()) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "SkillHire AI");
            helper.setTo(candidate.getEmail());
            helper.setSubject("📋 Your Weekly Job Digest - " + jobs.size() + " New Opportunities");
            helper.setText(buildWeeklyDigestEmail(candidate, jobs), true);

            mailSender.send(message);
            log.info("Weekly digest sent to: {}", candidate.getEmail());
        } catch (Exception e) {
            log.error("Failed to send weekly digest: {}", e.getMessage());
        }
    }

    /**
     * Send welcome email after registration
     */
    @Async
    public void sendWelcomeEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "SkillHire AI");
            helper.setTo(user.getEmail());
            helper.setSubject("🚀 Welcome to SkillHire AI - Let's Find Your Dream Job!");
            helper.setText(buildWelcomeEmail(user), true);

            mailSender.send(message);
            log.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
    }

    // =====================================================
    // EMAIL TEMPLATE BUILDERS
    // =====================================================

    private String buildJobMatchEmail(User candidate, Job job) {
        String applyLink = frontendUrl + "/jobs/" + job.getId();
        String unsubscribeLink = frontendUrl + "/unsubscribe?email=" + candidate.getEmail();
        String salaryRange = job.getSalaryMin() != null
                ? "₹" + formatSalary(job.getSalaryMin()) + " - ₹" + formatSalary(job.getSalaryMax()) + " LPA"
                : "Competitive";

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Job Match Found!</title>
            </head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:'Inter',Arial,sans-serif;">
              <div style="max-width:600px;margin:32px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                
                <!-- Header -->
                <div style="background:linear-gradient(135deg,#2563eb,#4f46e5);padding:40px 32px;text-align:center;">
                  <div style="width:48px;height:48px;background:rgba(255,255,255,0.2);border-radius:12px;display:inline-flex;align-items:center;justify-content:center;margin-bottom:16px;">
                    <span style="font-size:24px;">SH</span>
                  </div>
                  <h1 style="color:#ffffff;font-size:22px;font-weight:700;margin:0 0 8px;">SkillHire AI</h1>
                  <p style="color:rgba(255,255,255,0.8);margin:0;font-size:14px;">AI-Powered Job Portal</p>
                </div>

                <!-- Greeting -->
                <div style="padding:32px 32px 0;">
                  <div style="display:inline-block;background:#fef3c7;color:#92400e;padding:6px 14px;border-radius:20px;font-size:12px;font-weight:600;margin-bottom:20px;">🎯 New Job Match</div>
                  <h2 style="color:#111827;font-size:20px;font-weight:700;margin:0 0 8px;">Hi %s,</h2>
                  <p style="color:#6b7280;font-size:15px;line-height:1.6;margin:0 0 24px;">
                    We found a job that matches your profile! Don't miss this opportunity.
                  </p>
                </div>

                <!-- Job Card -->
                <div style="margin:0 32px 24px;border:2px solid #e5e7eb;border-radius:12px;overflow:hidden;">
                  <div style="background:#f9fafb;padding:20px 24px;border-bottom:1px solid #e5e7eb;">
                    <h3 style="color:#111827;font-size:18px;font-weight:700;margin:0 0 4px;">%s</h3>
                    <p style="color:#2563eb;font-size:14px;font-weight:600;margin:0;">🏢 %s</p>
                  </div>
                  <div style="padding:20px 24px;">
                    <table style="width:100%;border-collapse:collapse;">
                      <tr>
                        <td style="padding:6px 0;color:#6b7280;font-size:13px;width:40%%;">📍 Location</td>
                        <td style="padding:6px 0;color:#111827;font-size:13px;font-weight:500;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:6px 0;color:#6b7280;font-size:13px;">💼 Job Type</td>
                        <td style="padding:6px 0;color:#111827;font-size:13px;font-weight:500;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:6px 0;color:#6b7280;font-size:13px;">💰 Salary</td>
                        <td style="padding:6px 0;color:#059669;font-size:13px;font-weight:600;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:6px 0;color:#6b7280;font-size:13px;">📊 Experience</td>
                        <td style="padding:6px 0;color:#111827;font-size:13px;font-weight:500;">%s</td>
                      </tr>
                    </table>
                  </div>
                </div>

                <!-- CTA Button -->
                <div style="padding:0 32px 32px;text-align:center;">
                  <a href="%s" style="display:inline-block;background:linear-gradient(135deg,#2563eb,#4f46e5);color:#ffffff;text-decoration:none;padding:14px 40px;border-radius:10px;font-size:16px;font-weight:600;letter-spacing:0.3px;">
                    View & Apply Now →
                  </a>
                  <p style="color:#9ca3af;font-size:12px;margin:16px 0 0;">
                    Apply before the deadline to increase your chances!
                  </p>
                </div>

                <!-- Footer -->
                <div style="background:#f9fafb;border-top:1px solid #e5e7eb;padding:20px 32px;text-align:center;">
                  <p style="color:#9ca3af;font-size:12px;margin:0 0 8px;">
                    You're receiving this because you have job notifications enabled on SkillHire AI.
                  </p>
                  <a href="%s" style="color:#6b7280;font-size:12px;text-decoration:underline;">Unsubscribe from job alerts</a>
                  <p style="color:#d1d5db;font-size:11px;margin:12px 0 0;">© 2024 SkillHire AI. All rights reserved.</p>
                </div>

              </div>
            </body>
            </html>
            """.formatted(
                candidate.getFullName(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation() != null ? job.getLocation() : "Remote",
                job.getJobType() != null ? job.getJobType().toString() : "Full-time",
                salaryRange,
                job.getExperienceLevel() != null ? job.getExperienceLevel().toString() : "Any",
                applyLink,
                unsubscribeLink
        );
    }

    private String buildApplicationStatusEmail(User candidate, Job job, String status) {
        String statusColor = switch (status.toUpperCase()) {
            case "SHORTLISTED" -> "#059669";
            case "REJECTED" -> "#dc2626";
            case "INTERVIEW_SCHEDULED" -> "#d97706";
            default -> "#2563eb";
        };

        String statusEmoji = switch (status.toUpperCase()) {
            case "SHORTLISTED" -> "🎉";
            case "REJECTED" -> "📋";
            case "INTERVIEW_SCHEDULED" -> "📅";
            default -> "📬";
        };

        String statusMessage = switch (status.toUpperCase()) {
            case "SHORTLISTED" -> "Congratulations! You've been shortlisted for an interview. The recruiter will contact you soon.";
            case "REJECTED" -> "Thank you for applying. After careful consideration, the team has decided to move forward with other candidates. Keep applying — the right opportunity is waiting!";
            case "INTERVIEW_SCHEDULED" -> "Your interview has been scheduled! Please check your email for details and prepare accordingly.";
            default -> "Your application status has been updated. Log in to view more details.";
        };

        String applyLink = frontendUrl + "/candidate/applied-jobs";
        String unsubscribeLink = frontendUrl + "/unsubscribe?email=" + candidate.getEmail();

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:'Inter',Arial,sans-serif;">
              <div style="max-width:600px;margin:32px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                <div style="background:linear-gradient(135deg,#2563eb,#4f46e5);padding:40px 32px;text-align:center;">
                  <h1 style="color:#ffffff;font-size:22px;font-weight:700;margin:0 0 4px;">SkillHire AI</h1>
                  <p style="color:rgba(255,255,255,0.8);margin:0;font-size:14px;">Application Update</p>
                </div>
                <div style="padding:32px;">
                  <div style="text-align:center;margin-bottom:24px;">
                    <span style="font-size:48px;">%s</span>
                    <h2 style="color:#111827;font-size:20px;font-weight:700;margin:12px 0 4px;">Application %s</h2>
                    <span style="display:inline-block;background:%s;color:#fff;padding:4px 16px;border-radius:20px;font-size:13px;font-weight:600;">%s</span>
                  </div>
                  <div style="background:#f9fafb;border-radius:12px;padding:20px;margin-bottom:24px;">
                    <p style="color:#374151;font-size:14px;font-weight:600;margin:0 0 4px;">Position Applied For:</p>
                    <p style="color:#111827;font-size:16px;font-weight:700;margin:0 0 4px;">%s</p>
                    <p style="color:#6b7280;font-size:14px;margin:0;">at %s</p>
                  </div>
                  <p style="color:#374151;font-size:15px;line-height:1.7;margin:0 0 24px;">Hi %s, %s</p>
                  <div style="text-align:center;">
                    <a href="%s" style="display:inline-block;background:linear-gradient(135deg,#2563eb,#4f46e5);color:#fff;text-decoration:none;padding:12px 32px;border-radius:10px;font-size:15px;font-weight:600;">View My Applications</a>
                  </div>
                </div>
                <div style="background:#f9fafb;border-top:1px solid #e5e7eb;padding:16px 32px;text-align:center;">
                  <a href="%s" style="color:#6b7280;font-size:12px;">Unsubscribe</a>
                  <p style="color:#d1d5db;font-size:11px;margin:8px 0 0;">© 2024 SkillHire AI</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(statusEmoji, status, statusColor, status,
                job.getTitle(), job.getCompany(),
                candidate.getFullName(), statusMessage,
                applyLink, unsubscribeLink);
    }

    private String buildWeeklyDigestEmail(User candidate, List<Job> jobs) {
        StringBuilder jobRows = new StringBuilder();
        for (Job job : jobs.stream().limit(5).toList()) {
            String applyLink = frontendUrl + "/jobs/" + job.getId();
            jobRows.append("""
                <div style="border:1px solid #e5e7eb;border-radius:10px;padding:16px;margin-bottom:12px;">
                  <div style="display:flex;justify-content:space-between;align-items:start;">
                    <div>
                      <h4 style="color:#111827;font-size:15px;font-weight:700;margin:0 0 4px;">%s</h4>
                      <p style="color:#2563eb;font-size:13px;margin:0 0 6px;">%s</p>
                      <p style="color:#6b7280;font-size:12px;margin:0;">📍 %s | 💼 %s</p>
                    </div>
                  </div>
                  <a href="%s" style="display:inline-block;margin-top:10px;background:#eff6ff;color:#2563eb;text-decoration:none;padding:6px 16px;border-radius:6px;font-size:13px;font-weight:600;">Apply Now →</a>
                </div>
                """.formatted(job.getTitle(), job.getCompany(),
                    job.getLocation() != null ? job.getLocation() : "Remote",
                    job.getJobType() != null ? job.getJobType().toString() : "Full-time",
                    applyLink));
        }

        String browseLink = frontendUrl + "/jobs";
        String unsubscribeLink = frontendUrl + "/unsubscribe?email=" + candidate.getEmail();

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:'Inter',Arial,sans-serif;">
              <div style="max-width:600px;margin:32px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                <div style="background:linear-gradient(135deg,#2563eb,#4f46e5);padding:40px 32px;text-align:center;">
                  <h1 style="color:#ffffff;font-size:22px;font-weight:700;margin:0 0 4px;">SkillHire AI</h1>
                  <p style="color:rgba(255,255,255,0.8);margin:0;font-size:14px;">Your Weekly Job Digest</p>
                </div>
                <div style="padding:32px;">
                  <h2 style="color:#111827;font-size:20px;font-weight:700;margin:0 0 8px;">Hi %s! 👋</h2>
                  <p style="color:#6b7280;margin:0 0 24px;font-size:15px;">Here are %d new opportunities that match your profile this week:</p>
                  %s
                  <div style="text-align:center;margin-top:24px;">
                    <a href="%s" style="display:inline-block;background:linear-gradient(135deg,#2563eb,#4f46e5);color:#fff;text-decoration:none;padding:12px 32px;border-radius:10px;font-size:15px;font-weight:600;">Browse All Jobs →</a>
                  </div>
                </div>
                <div style="background:#f9fafb;border-top:1px solid #e5e7eb;padding:16px 32px;text-align:center;">
                  <a href="%s" style="color:#6b7280;font-size:12px;">Unsubscribe from weekly digest</a>
                  <p style="color:#d1d5db;font-size:11px;margin:8px 0 0;">© 2024 SkillHire AI</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(candidate.getFullName(), jobs.size(), jobRows, browseLink, unsubscribeLink);
    }

    private String buildWelcomeEmail(User user) {
        String dashboardLink = frontendUrl + (user.getRole() != null && user.getRole().name().equals("RECRUITER")
                ? "/recruiter/dashboard" : "/candidate/dashboard");

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:'Inter',Arial,sans-serif;">
              <div style="max-width:600px;margin:32px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                <div style="background:linear-gradient(135deg,#2563eb,#4f46e5);padding:48px 32px;text-align:center;">
                  <div style="font-size:48px;margin-bottom:16px;">🚀</div>
                  <h1 style="color:#ffffff;font-size:26px;font-weight:700;margin:0 0 8px;">Welcome to SkillHire AI!</h1>
                  <p style="color:rgba(255,255,255,0.85);margin:0;font-size:15px;">Your AI-Powered Career Journey Starts Here</p>
                </div>
                <div style="padding:40px 32px;">
                  <h2 style="color:#111827;font-size:20px;font-weight:700;margin:0 0 8px;">Hi %s! 👋</h2>
                  <p style="color:#6b7280;font-size:15px;line-height:1.7;margin:0 0 24px;">
                    Welcome aboard! Your account has been created successfully. Here's what you can do on SkillHire AI:
                  </p>
                  <div style="background:#f9fafb;border-radius:12px;padding:24px;margin-bottom:24px;">
                    <div style="margin-bottom:14px;"><span style="font-size:18px;">🤖</span> <strong style="color:#111827;">AI Career Assistant</strong> — Get personalized career advice</div>
                    <div style="margin-bottom:14px;"><span style="font-size:18px;">📄</span> <strong style="color:#111827;">Resume Analyzer</strong> — ATS score and improvement tips</div>
                    <div style="margin-bottom:14px;"><span style="font-size:18px;">💼</span> <strong style="color:#111827;">Smart Job Matching</strong> — AI-powered job recommendations</div>
                    <div><span style="font-size:18px;">✉️</span> <strong style="color:#111827;">Cover Letter Generator</strong> — Tailored cover letters in seconds</div>
                  </div>
                  <div style="text-align:center;">
                    <a href="%s" style="display:inline-block;background:linear-gradient(135deg,#2563eb,#4f46e5);color:#fff;text-decoration:none;padding:14px 40px;border-radius:10px;font-size:16px;font-weight:600;">Go to Dashboard →</a>
                  </div>
                </div>
                <div style="background:#f9fafb;border-top:1px solid #e5e7eb;padding:20px 32px;text-align:center;">
                  <p style="color:#9ca3af;font-size:12px;margin:0;">© 2024 SkillHire AI. All rights reserved.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(user.getFullName(), dashboardLink);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String getStatusSubject(String status) {
        return switch (status.toUpperCase()) {
            case "SHORTLISTED" -> "🎉 You've been Shortlisted";
            case "REJECTED" -> "📋 Application Update";
            case "INTERVIEW_SCHEDULED" -> "📅 Interview Scheduled";
            default -> "📬 Application Update";
        };
    }

    private String formatSalary(Integer salary) {
        if (salary >= 100000) return (salary / 100000) + "L";
        if (salary >= 1000) return (salary / 1000) + "K";
        return salary.toString();
    }
}
