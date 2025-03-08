package com.example.demo.model;

import javax.persistence.*;
import java.time.Instant;
import lombok.Data;

@Entity
@Table(name = "hive_dwd_wx_user_info")
@Data
public class HiveUserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscribe", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean subscribe;

    @Column(name = "openid", length = 64, nullable = false)
    private String openid;

    @Column(name = "language", length = 16)
    private String language;

    @Column(name = "subscribe_time", nullable = true)
    private Long subscribeTime;

    @Column(name = "unionid", length = 64)
    private String unionid;

    @Column(name = "pday", length = 8)
    private String pday;
} 