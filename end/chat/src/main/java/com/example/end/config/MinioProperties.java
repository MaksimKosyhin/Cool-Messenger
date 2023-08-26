package com.example.end.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("spring.minio")
@Data
public class MinioProperties {
    private Container container;
    private Buckets buckets;

    @Data
    public static class Container{
        private int port;
    }

    @Data
    public static class Buckets{
        private String app;
    }
}
