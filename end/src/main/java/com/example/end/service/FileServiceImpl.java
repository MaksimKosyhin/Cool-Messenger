package com.example.end.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService{
    private final MinioClient minioClient;

    @Override
    public void uploadFile(String bucketName, String key, MultipartFile file) {
        InputStream input;

        try {
            input = file.getInputStream();
        } catch (IOException e) {
            log.error("exception getting input stream from file to be uploaded", e);
            throw new RuntimeException(e);
        }

        var putObjectArgs = PutObjectArgs
                .builder()
                .bucket(bucketName)
                .object(key)
                .stream(input, file.getSize(), -1)
                .build();

        try {
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            log.error("exception trying to upload file", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getFile(String bucketName, String key) {
        var getObjectArgs = GetObjectArgs
                .builder()
                .bucket(bucketName)
                .object(key)
                .build();

        GetObjectResponse response;

        try {
            response = minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            log.error(String.format("exception trying to retrieve file: %s from bucket: %s", key, bucketName), e);
            throw new RuntimeException(e);
        }

        try {
            return response.readAllBytes();
        } catch (IOException e) {
            log.error(String.format("exception trying to read bytes of file: %s from bucket: %s", key, bucketName), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteFile(String bucketName, String key) {
        var removeObjectArgs = RemoveObjectArgs
                .builder()
                .bucket(bucketName)
                .object(key)
                .build();

        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.info(String.format("exception while removing file: %s from bucket: %s", key, bucketName));
            throw new RuntimeException(e);
        }
    }
}
