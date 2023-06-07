package com.example.end.service;

import com.example.end.exception.ApiException;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@ActiveProfiles("test")
public class FileServiceImplTest {

    private final FileServiceImpl fileService;
    private final String uploadsFolder;

    @Autowired
    public FileServiceImplTest(FileServiceImpl fileService,
                               @Value("cool-messenger.uploads-folder") String uploadsFolder) {
        this.fileService = fileService;
        this.uploadsFolder = uploadsFolder;
    }

    @AfterEach
    public void clean() throws IOException {
        FileUtils.cleanDirectory(new File(uploadsFolder));
    }

    @Test
    public void testValidFileIsUploaded() throws Exception {
        Path path = Paths.get("test");
        MockMultipartFile file = new MockMultipartFile("file", "test.txt",
                "text/plain", "Test file content".getBytes());

        Path savedPath = fileService.save(file, path);

        assertThat(Files.exists(savedPath)).isTrue();
    }

    @Test
    public void testValidFileIsUploadedWithExistingFile() throws Exception {
        Path path = Paths.get(uploadsFolder, "test", "test.txt");
        Files.createDirectories(path.getParent());
        Files.createFile(path);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt",
                "text/plain", "Test file content".getBytes());

        Path savedPath = fileService.save(file, path.getParent());

        assertThat(Files.exists(savedPath)).isTrue();
    }

    @Test
    public void testEmptyFileThrowsException() {
        Path path = Paths.get("test");
        String fileName = "text.txt";
        MockMultipartFile file = new MockMultipartFile("file", fileName,
                "text/plain", new byte[0]);

        ApiException expected = new ApiException(
                HttpStatus.BAD_REQUEST,
                String.format("file %s is empty", fileName));

        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> fileService.save(file, path))
                .satisfies(ex -> ex.equals(expected));
    }

    @Test
    public void testValidImageFileIsUploaded() throws Exception {
        Path path = Paths.get("test");
        File source = new ClassPathResource("profile.png").getFile();
        MockMultipartFile file = new MockMultipartFile("file", "image.png",
                "image/png", Files.readAllBytes(source.toPath()));

        Path savedPath = fileService.saveProfileImage(file, path);

        assertThat(Files.exists(savedPath)).isTrue();
    }

    @Test
    public void testInvalidImageFileThrowsException() {
        Path path = Paths.get("test");
        MockMultipartFile file = new MockMultipartFile("file", "image.txt",
                "text/plain", new byte[0]);

        ApiException expected = new ApiException(
                HttpStatus.BAD_REQUEST,
                "image files must have png or jpeg extension");

        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> fileService.saveProfileImage(file, path))
                .satisfies(ex -> ex.equals(expected));
    }

    @Test
    public void testValidFileIsDeleted() throws Exception {
        Path path = Paths.get(uploadsFolder, "test.txt");
        Files.createFile(path);

        fileService.delete(path);

        assertThat(Files.exists(path)).isFalse();
    }

    @Test
    public void testValidDirectoryIsDeleted() throws Exception {
        Path directoryPath = Paths.get(uploadsFolder, "directory");
        Files.createDirectory(directoryPath);

        fileService.delete(directoryPath);

        assertThat(Files.exists(directoryPath)).isFalse();
    }

    @Test
    public void testDeletingNonExistingFileThrowsException() {
        Path path = Paths.get(uploadsFolder, "non_existing_file.txt");

        ApiException expected = new ApiException(
                HttpStatus.BAD_REQUEST,
                String.format("resourse: %s doesn't exist", path));

        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> fileService.delete(path))
                .satisfies(ex -> ex.equals(expected));
    }
}

