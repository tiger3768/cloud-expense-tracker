package com.aditya.expensetracker.expense_tracker.exception;

import com.aditya.expensetracker.expense_tracker.dto.ErrorResponse;
import com.aditya.expensetracker.expense_tracker.dto.ValidationErrorResponse;
import com.aditya.expensetracker.expense_tracker.dto.ValidationField;
import com.aditya.expensetracker.expense_tracker.entity.Category;
import com.aditya.expensetracker.expense_tracker.entity.ExpenseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {

        log.warn("Resource not found: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()));

        ex.getBindingResult()
                .getGlobalErrors()
                .forEach(error -> {
                    String field = switch (error.getObjectName()) {
                        case "expenseFilterRequest" ->
                                error.getDefaultMessage() != null && error.getDefaultMessage().contains("amount")
                                        ? "maxAmount" : "endDate";
                        case "registerRequest" -> "confirmPassword";
                        default -> null;
                    };
                    if (field != null && !errors.containsKey(field)) {
                        errors.put(field, error.getDefaultMessage());
                    }
                });

        log.warn("Validation failed for fields: {}", errors.keySet());

        Map<String, ValidationField> fields = new HashMap<>();

        errors.forEach((field, message) -> {
            boolean required = message != null && message.toLowerCase().contains("required");
            List<String> allowedValues = switch (field) {
                case "type" -> Arrays.stream(ExpenseType.values())
                        .map(Enum::name)
                        .toList();
                case "category" -> Arrays.stream(Category.values())
                        .map(Enum::name)
                        .toList();
                default -> List.of();
            };

            fields.put(
                    field,
                    ValidationField.builder()
                            .message(message)
                            .required(required)
                            .allowedValues(allowedValues)
                            .build());
        });

        ValidationErrorResponse response =
                ValidationErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .errors(errors)
                        .type("VALIDATION_ERROR")
                        .message("Request contains missing or invalid fields.")
                        .fields(fields)
                        .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        log.warn("Authentication failed: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex) {

        log.warn("Invalid refresh token presented: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage());
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationToken(
            InvalidVerificationTokenException ex) {

        log.warn("Invalid email verification token: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex) {

        log.warn("Registration attempted with duplicate email: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage());
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordResetToken(
            InvalidPasswordResetTokenException ex) {

        log.warn("Invalid password reset token: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage());
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ErrorResponse> handleEmailDelivery(
            EmailDeliveryException ex) {

        log.error("Email delivery failed", ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage());
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(
            StorageException ex) {

        log.error("File storage operation failed", ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex) {

        log.warn("Optimistic locking conflict: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "This expense was changed by another request in the meantime. "
                        + "Please reload it and try again.");
    }

    @ExceptionHandler(InvalidAnalyticsRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAnalyticsRequest(
            InvalidAnalyticsRequestException ex) {

        log.warn("Invalid analytics request: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }
    
    @ExceptionHandler(OptimisticLockConflictException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockConflict(
            OptimisticLockConflictException ex) {
    	
    	log.warn("Optimistic locking conflict : {}", ex.getMessage());

        return buildErrorResponse(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                );
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        log.warn("Malformed or unreadable request body: {}", ex.getMessage());

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormat) {
            String field = invalidFormat.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(java.util.Objects::nonNull)
                    .reduce((first, second) -> second)
                    .orElse("request");

            List<String> allowedValues = List.of();
            if (invalidFormat.getTargetType() != null
                    && invalidFormat.getTargetType().isEnum()) {
                allowedValues = Arrays.stream(
                                invalidFormat.getTargetType().getEnumConstants())
                        .map(value -> ((Enum<?>) value).name())
                        .toList();
            }

            String message = "Invalid value for " + field + ".";
            Map<String, String> errors = Map.of(field, message);
            Map<String, ValidationField> fields = Map.of(
                    field,
                    ValidationField.builder()
                            .message(message)
                            .required(false)
                            .allowedValues(allowedValues)
                            .build());

            return ResponseEntity.badRequest().body(
                    ValidationErrorResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .errors(errors)
                            .type("VALIDATION_ERROR")
                            .message("Request contains an invalid value.")
                            .fields(fields)
                            .build());
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or malformed");
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyConflict(
            IdempotencyConflictException ex) {

        log.warn("Idempotency conflict: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {

        log.error("Unhandled exception", ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(message)
                .type(status == HttpStatus.CONFLICT ? "CONFLICT" : "ERROR")
                .build();

        return ResponseEntity.status(status).body(response);
    }
}