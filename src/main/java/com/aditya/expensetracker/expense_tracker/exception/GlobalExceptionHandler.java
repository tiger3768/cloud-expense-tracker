package com.aditya.expensetracker.expense_tracker.exception;

import com.aditya.expensetracker.expense_tracker.dto.ErrorResponse;
import com.aditya.expensetracker.expense_tracker.dto.ValidationErrorResponse;

import jakarta.persistence.OptimisticLockException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {

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

        ValidationErrorResponse response =
                ValidationErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .errors(errors)
                        .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex) {

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage());
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationToken(
            InvalidVerificationTokenException ex) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage());
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordResetToken(
            InvalidPasswordResetTokenException ex) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage());
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ErrorResponse> handleEmailDelivery(
            EmailDeliveryException ex) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage());
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(
            StorageException ex) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage());
    }

    @ExceptionHandler({
        OptimisticLockException.class,
        ObjectOptimisticLockingFailureException.class
})
public ResponseEntity<ErrorResponse> handleOptimisticLocking(Exception ex) {

    return buildErrorResponse(
            HttpStatus.CONFLICT,
            "This expense was modified by another user. Please refresh and try again.");
}

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(message)
                .build();

        return ResponseEntity.status(status).body(response);
    }
    
    @ExceptionHandler(InvalidAnalyticsRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAnalyticsRequest(
            InvalidAnalyticsRequestException ex) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }
}