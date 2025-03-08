package com.example.demo.service;

import com.example.demo.model.UserActivityRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, UserActivityRequest> kafkaTemplate;
    private final Random random = new Random();

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers; // 使用配置文件中的地址

    @Value("${spring.kafka.topics.user-activities}")
    private String topic;

    @Value("${kafka.producer.interval}") // 从配置文件中读取间隔时间
    private long interval;

    public KafkaProducerService(KafkaTemplate<String, UserActivityRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRateString = "${kafka.producer.interval}") // 使用配置的间隔时间
    public void produceUserActivity() {
        UserActivityRequest userActivityRequest = new UserActivityRequest();
        userActivityRequest.setOpenid(generateRandomOpenid()); // 随机生成openid
        userActivityRequest.setLanguage(generateRandomLanguage()); // 随机生成语言
        userActivityRequest.setSubscribe(random.nextBoolean()); // 随机生成订阅状态
        userActivityRequest.setSubscribeTime(generateRandomSubscribeTime()); // 随机生成subscribe_time
        userActivityRequest.setUnionid(generateRandomUnionid()); // 随机生成unionid
        userActivityRequest.setProcessed(false); // 默认未处理
        userActivityRequest.setPday(generatePday()); // 生成pday

        if (userActivityRequest.getSubscribeTime() == null) {
            userActivityRequest.setSubscribeTime(Instant.now()); // 赋予当前时间作为默认值
        }

        kafkaTemplate.send("etl_data_sync_topic", userActivityRequest);
        System.out.println("Produced user activity: " + userActivityRequest);
    }

    // 生成随机的openid
    private String generateRandomOpenid() {
        return "o" + UUID.randomUUID().toString().replace("-", "").substring(0, 27);
    }

    // 生成随机的语言
    private String generateRandomLanguage() {
        String[] languages = {"zh_CN", "en_US", "ja_JP", "ko_KR"};
        return languages[random.nextInt(languages.length)];
    }

    // 生成随机的unionid
    private String generateRandomUnionid() {
        return "o" + UUID.randomUUID().toString().replace("-", "").substring(0, 27);
    }

    // 生成随机subscribe_time时间戳
    private Instant generateRandomSubscribeTime() {
        Random random = new Random();
        int type = random.nextInt(100);
        if (type < 10) { // 10% 的概率生成空值
            return null;
        } else if (type < 80) { // 70% 生成当前时间
            return Instant.now();
        } else if (type < 95) { // 15% 生成过去的时间
            return Instant.now().minusSeconds(random.nextInt(86400 * 365)); // 随机减去 0 到 365 天的秒数
        } else { // 5% 生成未来的时间
            return Instant.now().plusSeconds(random.nextInt(86400 * 365)); // 随机加上 0 到 365 天的秒数
        }
    }

    // 生成pday字段的值
    private String generatePday() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
} 