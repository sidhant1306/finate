package com.example.finatebackend.controller;

import com.example.finatebackend.dto.dashboarddto.DashboardResponseDto;
import com.example.finatebackend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/get-dashboard")
    public ResponseEntity<DashboardResponseDto> getDashboardInfo() {
        return ResponseEntity.ok(dashboardService.getDashboardInfo());
    }
}
