package com.skillhire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SkillHire AI - Main Application
 *
 * @EnableAsync  → Allows @Async methods (email sending is non-blocking)
 * @EnableScheduling → Enables @Scheduled tasks (weekly digest, job notifications)
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SkillHireAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillHireAiApplication.class, args);
    }
}
