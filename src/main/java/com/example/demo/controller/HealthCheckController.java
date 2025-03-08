package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class HealthCheckController {
    private final DataSource dataSource;

    public HealthCheckController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public String check() {
        try (Connection conn = dataSource.getConnection()) {
            return "Database connection OK";
        } catch (Exception e) {
            return "Database connection FAILED: " + e.getMessage();
        }
    }
} 