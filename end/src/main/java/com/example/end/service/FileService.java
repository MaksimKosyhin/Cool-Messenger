package com.example.end.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;


public interface FileService {

    public Path replaceProfileImage(MultipartFile file, Path path);

    public Path saveProfileImage(MultipartFile file, Path path);

    public Path replace(MultipartFile file, Path path);

    public Path save(MultipartFile file, Path path);

    public void delete(Path path);
}
