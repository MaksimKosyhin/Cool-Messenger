package com.example.end.service;

import com.example.end.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test-minio")
public class FileServiceImplTest {

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private FileServiceImpl fileService;

    @Autowired
    private MinioProperties minioProperties;

    @Container
    public static GenericContainer<?> container =
            new GenericContainer<>(DockerImageName.parse("quay.io/minio/minio"))
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ACCESS_KEY", "minioadmin")
                    .withEnv("MINIO_SECRET_KEY", "minioadmin")
                    .withCommand("server /data");

    @BeforeAll
    public static void open() {
        container.start();
        var mappedPort = container.getMappedPort(9000);
        System.setProperty("minio.container.port", String.valueOf(mappedPort));
    }

    @Test
    public void uploadFile() throws Exception {
        File file = new ClassPathResource("src/src/test/resources/profile.jpg").getFile();

        fileService.uploadFile(
                minioProperties.getBuckets().getApp(),
                file.getName(),
                new MockMultipartFile(file.getName(), Files.readAllBytes(file.toPath())));

        assertDoesNotThrow(() -> {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBuckets().getApp())
                    .object(file.getName()).build());
        });
    }

    @Test
    public void getFile() throws Exception {
        File file = new ClassPathResource("src/src/test/resources/profile.jpg").getFile();
        byte[] fileContent = Files.readAllBytes(file.toPath());

        fileService.uploadFile(
                minioProperties.getBuckets().getApp(),
                file.getName(),
                new MockMultipartFile(file.getName(), fileContent));

        byte[] result = fileService.getFile(minioProperties.getBuckets().getApp(), file.getName());
        assertThat(result).isEqualTo(fileContent);
    }

    @Test
    public void deleteFile() throws Exception {
        File file = new ClassPathResource("src/src/test/resources/profile.jpg").getFile();
        byte[] fileContent = Files.readAllBytes(file.toPath());

        fileService.uploadFile(
                minioProperties.getBuckets().getApp(),
                file.getName(),
                new MockMultipartFile(file.getName(), fileContent));

        assertDoesNotThrow(() -> {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBuckets().getApp())
                    .object(file.getName()).build());
        });

        fileService.deleteFile(minioProperties.getBuckets().getApp(), file.getName());

        Assertions.assertThrows(ErrorResponseException.class, () -> {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBuckets().getApp())
                    .object(file.getName()).build());
        });
    }
}
