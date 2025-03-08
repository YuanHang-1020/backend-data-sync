package com.example.demo.controller;

import com.example.demo.model.UserActivity;
import com.example.demo.model.UserActivityRequest;
import com.example.demo.repository.UserActivityRepository;
import com.example.demo.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.hamcrest.Matchers.hasSize;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class UserActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserActivityRepository userActivityRepository;

    @MockBean
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserActivityRequest userActivityRequest;
    private UserActivity userActivity;

    @BeforeEach
    public void setUp() {
        userActivityRequest = new UserActivityRequest();
        userActivityRequest.setOpenid("test123");
        userActivityRequest.setLanguage("zh-CN");
        userActivityRequest.setSubscribe(true);
        userActivityRequest.setSubscribeTime(Instant.now());
        userActivityRequest.setUnionid("union123");
        userActivityRequest.setProcessed(false);

        userActivity = new UserActivity();
        userActivity.setId(1L);
        userActivity.setOpenid("test123");
        userActivity.setLanguage("zh-CN");
        userActivity.setSubscribe(true);
        userActivity.setSubscribeTime(userActivityRequest.getSubscribeTime());
        userActivity.setUnionid("union123");
        userActivity.setProcessed(false);
        userActivity.setCreateTime(LocalDateTime.now());
        userActivity.setUpdateTime(LocalDateTime.now());
    }

    @Test
    public void testRecordActivity() throws Exception {
        UserActivityRequest request = new UserActivityRequest();
        request.setOpenid("test123");
        request.setLanguage("zh-CN");
        request.setSubscribe(true);
        request.setSubscribeTime(Instant.now());
        request.setUnionid("union123");
        request.setProcessed(false);

        UserActivity userActivity = new UserActivity();
        userActivity.setOpenid(request.getOpenid());
        userActivity.setLanguage(request.getLanguage());
        userActivity.setSubscribe(request.getSubscribe());
        userActivity.setSubscribeTime(request.getSubscribeTime());
        userActivity.setUnionid(request.getUnionid());
        userActivity.setProcessed(request.getProcessed());

        when(userActivityRepository.save(any(UserActivity.class))).thenReturn(userActivity);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Activity received successfully"));
    }

    @Test
    public void testGetActivity_FromRedis() throws Exception {
        when(redisService.getCachedActivity(anyString())).thenReturn(userActivity);

        Map<String, String> request = new HashMap<>();
        request.put("openid", "test123");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/activities/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openid").value("test123"))
                .andExpect(jsonPath("$.language").value("zh-CN"))
                .andExpect(jsonPath("$.subscribe").value(true))
                .andExpect(jsonPath("$.unionid").value("union123"))
                .andExpect(jsonPath("$.processed").value(false));
    }

    @Test
    public void testGetActivity_FromMySQL() throws Exception {
        when(redisService.getCachedActivity(anyString())).thenReturn(null);
        when(userActivityRepository.findByOpenid(anyString())).thenReturn(Collections.singletonList(userActivity));

        Map<String, String> request = new HashMap<>();
        request.put("openid", "test123");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/activities/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openid").value("test123"))
                .andExpect(jsonPath("$.language").value("zh-CN"))
                .andExpect(jsonPath("$.subscribe").value(true))
                .andExpect(jsonPath("$.unionid").value("union123"))
                .andExpect(jsonPath("$.processed").value(false));
    }

    @Test
    public void testGetActivity_NotFound() throws Exception {
        when(redisService.getCachedActivity(anyString())).thenReturn(null);
        when(userActivityRepository.findByOpenid(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/activities/test123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testGetActivity_InvalidOpenid() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("openid", "");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/activities/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateActivity() throws Exception {
        UserActivityRequest request = new UserActivityRequest();
        request.setOpenid("test123");
        request.setLanguage("zh-CN");
        request.setSubscribe(true);
        request.setSubscribeTime(Instant.now());
        request.setUnionid("union123");
        request.setProcessed(false);

        UserActivity userActivity = new UserActivity();
        userActivity.setOpenid("test123");
        userActivity.setLanguage("zh-CN");
        userActivity.setSubscribe(true);
        userActivity.setSubscribeTime(Instant.now());
        userActivity.setUnionid("union123");
        userActivity.setProcessed(false);

        when(userActivityRepository.findByOpenid(anyString())).thenReturn(Collections.singletonList(userActivity));
        when(userActivityRepository.save(any(UserActivity.class))).thenReturn(userActivity);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/activities/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Activity updated successfully"));
    }

    @Test
    public void testUpdateActivity_NotFound() throws Exception {
        when(userActivityRepository.findByOpenid(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/activities/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userActivityRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateActivity_RedisCacheUpdated() throws Exception {
        when(userActivityRepository.findByOpenid(anyString())).thenReturn(Collections.singletonList(userActivity));
        when(userActivityRepository.save(any(UserActivity.class))).thenReturn(userActivity);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/activities/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userActivityRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Activity updated successfully"));

        // 验证Redis缓存是否更新
        verify(redisService, times(1)).cacheUserActivity(anyString(), any());
    }

    @Test
    public void testDeleteActivity() throws Exception {
        UserActivity userActivity = new UserActivity();
        userActivity.setOpenid("test123");
        userActivity.setLanguage("zh-CN");
        userActivity.setSubscribe(true);
        userActivity.setSubscribeTime(Instant.now());
        userActivity.setUnionid("union123");
        userActivity.setProcessed(false);

        when(userActivityRepository.findByOpenid(anyString())).thenReturn(Collections.singletonList(userActivity));

        Map<String, String> request = new HashMap<>();
        request.put("openid", "test123");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/activities/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Activity deleted successfully"));
    }

    @Test
    public void testDeleteActivity_NotFound() throws Exception {
        when(userActivityRepository.findByOpenid(anyString())).thenReturn(Collections.emptyList());

        Map<String, String> request = new HashMap<>();
        request.put("openid", "test123");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/activities/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRecordActivity_RedisError() throws Exception {
        when(userActivityRepository.save(any(UserActivity.class))).thenReturn(userActivity);
        doThrow(new RuntimeException("Redis error")).when(redisService).cacheUserActivity(anyString(), any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userActivityRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Activity received successfully"));
    }
} 