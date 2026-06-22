package com.example.finatebackend.schedulers;

import com.example.finatebackend.dao.UserRepository;
import com.example.finatebackend.model.User;
import com.example.finatebackend.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyInsightScheduler {

    private final UserRepository userRepository;
    private final AiService aiService;

    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Kolkata")
    public void generateWeeklyInsightsForAllUsers() {
        List<User> users = userRepository.findAll();
        log.info("Weekly insight scheduler started for {} users", users.size());

        for (User user : users) {
            try {
                aiService.generateInsightForUser(user);
                log.info("Weekly insight generated for user {}", user.getUserId());
            } catch (Exception e) {
                log.error("Failed to generate weekly insight for user {}", user.getUserId(), e);
            }
        }

        log.info("Weekly insight scheduler completed");
    }
}