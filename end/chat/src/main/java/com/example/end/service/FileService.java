package com.example.end.service;

import org.springframework.web.multipart.MultipartFile;


public interface FileService {
    public void uploadFile(String bucketName, String key, MultipartFile file);
    public byte[] getFile(String bucketName, String key);
    public void deleteFile(String bucketName, String key);
}
