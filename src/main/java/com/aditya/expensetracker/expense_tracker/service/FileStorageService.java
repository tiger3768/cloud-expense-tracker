package com.aditya.expensetracker.expense_tracker.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String uploadFile(MultipartFile file);

    void deleteFile(String key);

    String generatePresignedUrl(String key);
}