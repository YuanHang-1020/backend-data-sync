package com.example.demo.model;

import lombok.Data;
import java.time.Instant;

@Data
public class UserActivityRequest {
    private String openid;
    private String language;
    private Boolean subscribe;
    private Instant subscribeTime;
    private String unionid;
    private Boolean processed;
    private String pday;
} 