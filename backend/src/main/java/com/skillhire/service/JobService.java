package com.skillhire.service;

import com.skillhire.dto.request.JobRequest;
import com.skillhire.entity.Job;
import com.skillhire.entity.Skill;
import com.skillhire.entity.User;
import com.skillhire.exception.ResourceNotFoundException;
import com.skillhire.repository.JobRepository;
import com.skillhire.repository.SkillRepository;
import com.skillhire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Job Service - Business logic for job management
 * Triggers email notifications when a new job is posted.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * Create a new job posting + trigger email notifications
     */
    @Transactional
    public Job createJob(JobRequest jobRequest, User recruiter) {
        Set<Skill> skills = resolveSkills(jobRequest.getSkills());

        Job job = Job.builder()
                .title(jobRequest.getTitle())
                .description(jobRequest.getDescription())
                .requirements(jobRequest.getRequirements())
                .company(jobRequest.getCompany())
                .location(jobRequest.getLocation())
                .jobType(jobRequest.getJobType())
                .experienceLevel(jobRequest.getExperienceLevel())
                .salaryMin(jobRequest.getSalaryMin())
                .salaryMax(jobRequest.getSalaryMax())
                .salaryCurrency(jobRequest.getSalaryCurrency())
                .experienceYearsMin(jobRequest.getExperienceYearsMin())
                .experienceYearsMax(jobRequest.getExperienceYearsMax())
                .applicationDeadline(jobRequest.getApplicationDeadline())
                .openings(jobRequest.getOpenings())
                .recruiter(recruiter)
                .skills(skills)
                .status(Job.JobStatus.ACTIVE)
                .build();

        Job savedJob = jobRepository.save(job);
        log.info("Job created: {} by recruiter: {}", savedJob.getTitle(), recruiter.getEmail());

        // Trigger async email notifications to all subscribed candidates
        triggerJobPostingNotifications(savedJob);

        return savedJob;
    }

    /**
     * Trigger email notifications for new job posting (async)
     */
    private void triggerJobPostingNotifications(Job job) {
        try {
            List<User> subscribedCandidates = userRepository
                    .findByRoleAndEmailNotificationsEnabled(User.Role.CANDIDATE, true);
            log.info("Sending job notifications to {} subscribers for job: {}", subscribedCandidates.size(), job.getTitle());
            emailNotificationService.sendNewJobPostingNotification(subscribedCandidates, job);
        } catch (Exception e) {
            // Never fail job creation because of email failure
            log.error("Failed to trigger job posting notifications: {}", e.getMessage());
        }
    }

    /**
     * Update a job posting
     */
    @Transactional
    public Job updateJob(Long jobId, JobRequest jobRequest, User recruiter) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new RuntimeException("You are not authorized to update this job");
        }

        job.setTitle(jobRequest.getTitle());
        job.setDescription(jobRequest.getDescription());
        job.setRequirements(jobRequest.getRequirements());
        job.setCompany(jobRequest.getCompany());
        job.setLocation(jobRequest.getLocation());
        job.setJobType(jobRequest.getJobType());
        job.setExperienceLevel(jobRequest.getExperienceLevel());
        job.setSalaryMin(jobRequest.getSalaryMin());
        job.setSalaryMax(jobRequest.getSalaryMax());
        job.setExperienceYearsMin(jobRequest.getExperienceYearsMin());
        job.setExperienceYearsMax(jobRequest.getExperienceYearsMax());
        job.setApplicationDeadline(jobRequest.getApplicationDeadline());
        job.setOpenings(jobRequest.getOpenings());
        job.setSkills(resolveSkills(jobRequest.getSkills()));

        return jobRepository.save(job);
    }

    /** Get all active jobs with pagination */
    public Page<Job> getAllActiveJobs(Pageable pageable) {
        return jobRepository.findByStatus(Job.JobStatus.ACTIVE, pageable);
    }

    /** Search jobs with filters */
    public Page<Job> searchJobs(String title, String location,
                                Job.ExperienceLevel experienceLevel, Pageable pageable) {
        return jobRepository.searchJobs(location, title, experienceLevel, pageable);
    }

    /** Get job by ID */
    public Job getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        job.setViewCount(job.getViewCount() + 1);
        return jobRepository.save(job);
    }

    /** Get recruiter's jobs */
    public Page<Job> getRecruiterJobs(Long recruiterId, Pageable pageable) {
        return jobRepository.findByRecruiterId(recruiterId, pageable);
    }

    /** Delete (close) a job */
    @Transactional
    public void deleteJob(Long jobId, User recruiter) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new RuntimeException("You are not authorized to delete this job");
        }

        job.setStatus(Job.JobStatus.CLOSED);
        jobRepository.save(job);
        log.info("Job closed: {}", jobId);
    }

    /** Resolve skill names to Skill entities (create if not exists) */
    private Set<Skill> resolveSkills(java.util.List<String> skillNames) {
        Set<Skill> skills = new HashSet<>();
        if (skillNames != null) {
            for (String skillName : skillNames) {
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> skillRepository.save(
                                Skill.builder().name(skillName).build()
                        ));
                skills.add(skill);
            }
        }
        return skills;
    }
}
