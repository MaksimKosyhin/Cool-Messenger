package com.example.end.service;

import com.example.end.exception.ApiException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

@Service
public class FileServiceImpl implements FileService{
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("image/png", "image/jpeg");

    @Value("${cool-messenger.uploads-folder}")
    private String uploadsFolder;

    @Override
    public Path replaceProfileImage(MultipartFile file, Path path) {
        delete(path);
        return saveProfileImage(file, path.getParent());
    }

    @Override
    public Path saveProfileImage(MultipartFile file, Path path) {
        if(!IMAGE_EXTENSIONS.contains(file.getContentType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "image files must have png or jpeg extension");
        }

        return save(file, path);
    }

    @Override
    public Path replace(MultipartFile file, Path path) {
        delete(path);
        return save(file, path.getParent());
    }

    @Override
    public Path save(MultipartFile file, Path path) {
        if (file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    String.format("file: \"%s\" is empty", file.getOriginalFilename()));
        }

        var destinationFile = generatePath(Paths.get(uploadsFolder).resolve(path), file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(destinationFile.getParent());
            Files.copy(inputStream, destinationFile);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return destinationFile;
    }

    private Path generatePath(Path path, String fileName) {
        var newFile = path.resolve(fileName);

        var extensionPosition = fileName.indexOf('.');
        var withoutExtension = extensionPosition == -1 ? fileName : fileName.substring(0, extensionPosition);
        var extension = extensionPosition == -1 ? "" : fileName.substring(extensionPosition);

        for(int i = 0; newFile.toFile().exists(); i++) {
            var newName = withoutExtension + String.format(" (%d)", i) + extension;
            newFile = newFile.getParent().resolve(newName);
        }

        return newFile;
    }

    @Override
    public void delete(Path path) {
        var file = path.toFile();

        if (!file.exists()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    String.format("resourse: %s doesn't exist", path));
        }

        if(file.isDirectory()) {
            try {
                FileUtils.cleanDirectory(file);
            } catch (IOException e) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        file.delete();
    }
}
