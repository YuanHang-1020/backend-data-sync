package com.example.demo.service;

import com.example.demo.model.UserActivity;
import com.example.demo.model.UserActivityRequest;
import com.example.demo.repository.UserActivityRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final UserActivityRepository userActivityRepository;
    private final RedisService redisService;

    public KafkaConsumerService(UserActivityRepository userActivityRepository, RedisService redisService) {
        this.userActivityRepository = userActivityRepository;
        this.redisService = redisService;
    }

    @KafkaListener(topics = "${spring.kafka.topics.user-activities}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserActivity(UserActivityRequest userActivityRequest) {
        log.info("####Consumed user activity####: {}", userActivityRequest);

        // 1. 将数据保存到MySQL (dwd_wx_user_info)
        UserActivity userActivity = new UserActivity();
        userActivity.setOpenid(userActivityRequest.getOpenid());
        userActivity.setLanguage(userActivityRequest.getLanguage());
        userActivity.setSubscribe(userActivityRequest.getSubscribe());
        if (userActivityRequest.getSubscribeTime() != null) {
            userActivity.setSubscribeTime(userActivityRequest.getSubscribeTime());
        } else {
            userActivity.setSubscribeTime(Instant.now()); // 赋予当前时间作为默认值
        }
        userActivity.setUnionid(userActivityRequest.getUnionid());
        userActivity.setProcessed(userActivityRequest.getProcessed());

        try {
            userActivityRepository.save(userActivity);
            log.info("Saved user activity to MySQL (dwd_wx_user_info): {}", userActivity);
        } catch (Exception e) {
            log.error("Failed to save user activity to dwd_wx_user_info", e);
        }

        // 2. 将数据保存到Redis
        try {
            redisService.cacheUserActivity(userActivityRequest.getOpenid(), userActivity);
            log.info("Cached user activity in Redis: {}", userActivity);
        } catch (Exception e) {
            log.error("Failed to cache user activity in Redis", e);
        }
    }

    @KafkaListener(id = "errorHandler", topics = "${spring.kafka.topics.user-activities}.DLT")
    public void handleError(Message<?> message) {
        log.error("Failed to process message: {}", message);
    }
} 