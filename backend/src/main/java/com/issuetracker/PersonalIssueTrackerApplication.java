package com.issuetracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PersonalIssueTrackerApplication {

    private static final Logger logger = LoggerFactory.getLogger(PersonalIssueTrackerApplication.class);

    public static void main(String[] args) {
        logger.info("🚀 Starting Personal Issue Tracker Application...");
        SpringApplication.run(PersonalIssueTrackerApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("✅ Personal Issue Tracker Application is ready and running!");
        logger.info("📊 API available at: /api");
        logger.info("🔐 Authentication endpoints: /api/auth/login, /api/auth/register");
    }

}