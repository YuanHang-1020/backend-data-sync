package com.example.demo.model;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import lombok.Data;

@Entity
@Table(name = "dwd_wx_user_info")
@Data
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "openid", length = 64, nullable = false)
    private String openid;

    @Column(name = "language", length = 16)
    private String language;

    @Column(name = "subscribe", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean subscribe;

    @Column(name = "subscribe_time", nullable = true)
    private Instant subscribeTime;

    @Column(name = "unionid", length = 64)
    private String unionid;

    @Column(name = "processed", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean processed;

    @Column(name = "create_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime updateTime;

    // 生成随机subscribe_time时间戳
    public String generateRandomSubscribeTime() {
        Random random = new Random();
        int type = random.nextInt(100);
        if (type < 70) {
            // 70% 生成10位时间戳
            return String.valueOf(System.currentTimeMillis() / 1000);
        } else if (type < 85) {
            // 15% 生成大于10位的时间戳
            return String.valueOf(System.currentTimeMillis());
        } else {
            // 15% 生成小于10位的时间戳
            return String.valueOf(random.nextInt(1000000000));
        }
    }

    // Getters and setters
} 