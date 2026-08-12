package com.aditya.expensetracker.expense_tracker.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component
public class S3HealthIndicator implements HealthIndicator {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3HealthIndicator(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public Health health() {

        try {
            s3Client.headBucket(
                    HeadBucketRequest.builder()
                            .bucket(bucketName)
                            .build());

            return Health.up()
                    .withDetail("bucket", bucketName)
                    .build();

        } catch (Exception ex) {

            return Health.down()
                    .withDetail("bucket", bucketName)
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}