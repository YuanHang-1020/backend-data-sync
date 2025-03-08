package com.example.demo.repository;

import com.example.demo.model.HiveUserActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HiveUserActivityRepository extends JpaRepository<HiveUserActivity, Long> {
} 