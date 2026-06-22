package com.example.finatebackend.controller;

import com.example.finatebackend.dto.ai.ChatRequestDto;
import com.example.finatebackend.dto.ai.ChatResponseDto;
import com.example.finatebackend.dto.ai.WeeklyInsightResponseDto;
import com.example.finatebackend.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {


    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/get-weekly-insight")
    public ResponseEntity<WeeklyInsightResponseDto> getWeeklyInsight() {
        return ResponseEntity.ok(aiService.getWeeklyInsights());
    }

    @PostMapping("/generate-weekly-insight")
    public ResponseEntity<String> generateWeeklyInsight() {
        com.example.finatebackend.model.User user = com.example.finatebackend.security.SecurityUtils.getCurrentUser();
        aiService.generateInsightForUser(user);
        return ResponseEntity.ok("Weekly insight generated and emailed successfully.");
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> chat(@RequestBody ChatRequestDto chatRequestDto) {
        return ResponseEntity.ok(aiService.chat(chatRequestDto));
    }
}
