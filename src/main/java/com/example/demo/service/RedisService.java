package com.example.demo.service;

import com.example.demo.model.UserActivity;
import com.example.demo.model.UserActivityRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${redis.cache.expiration:600}") // 默认10分钟
    private long expirationTime;

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    public RedisService(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
    }

    public void cacheUserActivity(String openid, UserActivity userActivity) {
        String key = "user_activity:" + openid;
        redisTemplate.opsForValue().set(key, userActivity, expirationTime, TimeUnit.SECONDS);
    }

    public Object getCachedActivity(String openid) {
        return redisTemplate.opsForValue().get(openid);
    }

    public void saveUserActivity(String openid, UserActivityRequest activity) {
        String key = "user_activity:" + openid;
        stringRedisTemplate.opsForValue().set(key, activity.toString());
    }

    public void deleteCachedActivity(String openid) {
        String key = "user_activity:" + openid;
        redisTemplate.delete(key);
        log.info("Deleted user activity from Redis with key: {}", key);
    }
} 