package com.example.end.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
@RequiredArgsConstructor
public class MinioConfig {
    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        log.info(String.format("starting minio at port: %s", minioProperties.getContainer().getPort()));
        return MinioClient.builder()
                .endpoint("http://localhost:" + minioProperties.getContainer().getPort())
                .credentials("minioadmin", "minioadmin")
                .build();
    }

    @Bean
    public CommandLineRunner createAppBucket() {
        var minioClient = minioClient();

        return args -> {
            createBucket(minioClient, minioProperties.getBuckets().getApp());
        };
    }

    private void createBucket(MinioClient minioClient, String bucketName) {
        try {
            if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                log.info(String.format("%s bucket already exists", bucketName));
            } else {
                minioClient.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket(bucketName)
                                .build());
            }
        } catch (Exception e) {
            log.error(String.format("exception in creating %s bucket", bucketName), e);
        }
    }

}
