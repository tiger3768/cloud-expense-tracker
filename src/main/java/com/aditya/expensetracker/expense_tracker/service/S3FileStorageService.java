package com.aditya.expensetracker.expense_tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aditya.expensetracker.expense_tracker.exception.StorageException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) {

        validateFile(file);

        String key = generateObjectKey(file);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            log.debug("Uploaded {} to bucket {}", key, bucketName);

            return key;

        } catch (IOException e) {
            log.warn("Failed to read uploaded file for key {}", key, e);
            throw new RuntimeException("Failed to read uploaded file.", e);

        } catch (S3Exception e) {
        	log.warn("S3 upload failed for key {} in bucket {}", key, bucketName, e);
        	throw new StorageException("Failed to upload file.", e);
        }
    }

    @Override
    public void deleteFile(String key) {

        if (key == null || key.isBlank()) {
            return;
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {

            s3Client.deleteObject(request);
            log.debug("Deleted {} from bucket {}", key, bucketName);

        } catch (S3Exception e) {
        	log.warn("S3 delete failed for key {} in bucket {}", key, bucketName, e);
        	throw new StorageException("Failed to delete file.", e);
        }
    }

    @Override
    public String generatePresignedUrl(String key) {

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(objectRequest)
                        .build();

        return s3Presigner
                .presignGetObject(presignRequest)
                .url()
                .toString();
    }

    private String generateObjectKey(MultipartFile file) {

        return "receipts/"
                + UUID.randomUUID()
                + getExtension(file.getOriginalFilename());
    }

    private String getExtension(String fileName) {

        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf("."));
    }

    private static final long MAX_FILE_SIZE_MB = 10;
    private static final long MAX_FILE_SIZE = MAX_FILE_SIZE_MB * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size must not exceed " + MAX_FILE_SIZE_MB + " MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Allowed types: JPEG, PNG, WEBP, PDF.");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException(
                    "File name must not be blank.");
        }
    }
}