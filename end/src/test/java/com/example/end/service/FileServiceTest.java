package com.example.end.service;

import com.example.end.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FileServiceTest {

    private final FileService fileService;
    private final String uploadsFolder;

    @Autowired
    public FileServiceTest(FileService fileService) {
        this.fileService = fileService;
        this.uploadsFolder = "uploads-test";
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
    public void testValidFileWithoutExtensionIsUploaded() throws Exception {
        Path path = Paths.get("test");
        MockMultipartFile file = new MockMultipartFile("file", "test",
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
        MockMultipartFile file = new MockMultipartFile("file", "test.txt",
                "text/plain", new byte[0]);

        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> fileService.save(file, path))
                .satisfies(ex -> ex.errors.equals(Map.of("file", "file \"test.txt\" is empty")));
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

        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> fileService.saveProfileImage(file, path))
                .matches(ex -> ex.errors.equals(Map.of("file", "image files must have png or jpeg extension")));
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

        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> fileService.delete(path))
                .matches(ex -> ex.errors.equals(Map.of("file", String.format("resourse: %s doesn't exist", path))));
    }
}

