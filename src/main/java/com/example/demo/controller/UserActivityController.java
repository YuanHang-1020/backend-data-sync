package com.example.demo.controller;

import com.example.demo.model.UserActivityRequest;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.model.UserActivity;
import com.example.demo.repository.UserActivityRepository;
import com.example.demo.service.RedisService;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;
import java.time.Instant;
import java.util.Collections;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/activities")
public class UserActivityController {
    
    private static final Logger log = LoggerFactory.getLogger(UserActivityController.class);
    private final UserActivityRepository userActivityRepository;
    private final RedisService redisService;

    public UserActivityController(UserActivityRepository userActivityRepository, RedisService redisService) {
        this.userActivityRepository = userActivityRepository;
        this.redisService = redisService;
    }

    @PostMapping
    public ResponseEntity<String> recordActivity(@Valid @RequestBody UserActivityRequest request) {
        log.info("Received activity request: {}", request);

        // 参数校验
        if (request.getOpenid() == null || request.getOpenid().isEmpty()) {
            return ResponseEntity.badRequest().body("openid cannot be null or empty");
        }

        // 将数据保存到MySQL
        UserActivity userActivity = new UserActivity();
        userActivity.setOpenid(request.getOpenid());
        userActivity.setLanguage(request.getLanguage());
        userActivity.setSubscribe(request.getSubscribe());
        userActivity.setSubscribeTime(request.getSubscribeTime() != null ? request.getSubscribeTime() : Instant.now());
        userActivity.setUnionid(request.getUnionid());
        userActivity.setProcessed(request.getProcessed());
        userActivityRepository.save(userActivity);
        log.info("Saved user activity to MySQL: {}", userActivity);

        // 将数据保存到Redis
        try {
            redisService.cacheUserActivity(request.getOpenid(), userActivity);
            log.info("Cached user activity in Redis: {}", userActivity);
        } catch (Exception e) {
            log.error("Failed to cache user activity in Redis", e);
        }

        return ResponseEntity.ok("Activity received successfully");
    }

    @PostMapping("/get")
    public ResponseEntity<UserActivity> getActivity(@RequestBody Map<String, String> request) {
        String openid = request.get("openid");
        if (openid == null || openid.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 1. 从Redis中查询
        UserActivity cachedActivity = (UserActivity) redisService.getCachedActivity(openid);
        if (cachedActivity != null) {
            log.info("Retrieved user activity from Redis: {}", cachedActivity);
            return ResponseEntity.ok(cachedActivity);
        }

        // 2. 从MySQL中查询
        List<UserActivity> userActivities = userActivityRepository.findByOpenid(openid);
        if (!userActivities.isEmpty()) {
            UserActivity userActivity = userActivities.get(0); // 返回第一条记录
            // 3. 将数据缓存到Redis
            try {
                redisService.cacheUserActivity(openid, userActivity);
                log.info("Retrieved user activity from MySQL and cached in Redis: {}", userActivity);
            } catch (Exception e) {
                log.error("Failed to cache user activity in Redis", e);
            }
            return ResponseEntity.ok(userActivity);
        }

        // 4. 返回 404
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateActivity(@Valid @RequestBody UserActivityRequest request) {
        String openid = request.getOpenid();
        if (openid == null || openid.isEmpty()) {
            return ResponseEntity.badRequest().body("openid cannot be null or empty");
        }

        // 1. 从MySQL中查询
        List<UserActivity> userActivities = userActivityRepository.findByOpenid(openid);
        if (userActivities.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No activity found for openid: " + openid);
        }
        UserActivity userActivity = userActivities.get(0); // 返回第一条记录

        // 2. 更新数据
        userActivity.setLanguage(request.getLanguage());
        userActivity.setSubscribe(request.getSubscribe());
        userActivity.setSubscribeTime(request.getSubscribeTime() != null ? request.getSubscribeTime() : Instant.now());
        userActivity.setUnionid(request.getUnionid());
        userActivity.setProcessed(request.getProcessed());
        userActivityRepository.save(userActivity);
        log.info("Updated user activity in MySQL: {}", userActivity);

        // 3. 更新Redis缓存
        try {
            redisService.cacheUserActivity(openid, userActivity);
            log.info("Updated user activity in Redis: {}", userActivity);
        } catch (Exception e) {
            log.error("Failed to cache user activity in Redis", e);
            return ResponseEntity.status(500).body("Failed to update Redis cache");
        }

        return ResponseEntity.ok("Activity updated successfully");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteActivity(@RequestBody Map<String, String> request) {
        String openid = request.get("openid");
        if (openid == null || openid.isEmpty()) {
            return ResponseEntity.badRequest().body("openid cannot be null or empty");
        }

        // 1. 从MySQL中删除
        List<UserActivity> userActivities = userActivityRepository.findByOpenid(openid);
        if (userActivities.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No activity found for openid: " + openid);
        }
        UserActivity userActivity = userActivities.get(0); // 返回第一条记录

        userActivityRepository.delete(userActivity);
        log.info("Deleted user activity from MySQL: {}", userActivity);

        // 2. 从Redis中删除
        redisService.deleteCachedActivity(openid);
        log.info("Deleted user activity from Redis: {}", openid);

        return ResponseEntity.ok("Activity deleted successfully");
    }

    @GetMapping("/{openid}")
    public ResponseEntity<List<UserActivity>> getUserActivity(@PathVariable String openid) {
        if (openid == null || openid.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<UserActivity> userActivities = userActivityRepository.findByOpenid(openid);
        return ResponseEntity.ok(userActivities);
    }
} 