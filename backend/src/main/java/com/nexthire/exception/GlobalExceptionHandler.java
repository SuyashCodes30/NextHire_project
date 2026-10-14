package com.nexthire.exception;

import com.nexthire.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — Centralized exception handling for all REST controllers.
 *
 * Catches all exceptions and converts them to a clean, consistent
 * ApiResponse format instead of Spring's default error pages.
 *
 * Every error response follows this format:
 * {
 *   "success": false,
 *   "message": "Human-readable error message",
 *   "data": null (or validation errors map),
 *   "status": 404
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle Spring 3.x NoResourceFoundException (missing static assets or invalid routes)
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        ApiResponse<Object> response = ApiResponse.error("Resource not found: " + ex.getResourcePath(), 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle 400 — Bad Request
     * Triggered when: invalid category, invalid difficulty, etc.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle 400 — Validation Errors (@Valid annotation failures)
     *
     * Triggered when: LoginRequest has empty email, RegisterRequest has short password, etc.
     *
     * Returns a map of field -> error message:
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "data": { "email": "Email is required", "password": "Must be 8+ chars" },
     *   "status": 400
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                false,
                "Validation failed. Please check the errors and try again.",
                validationErrors,
                400
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle 500 — Generic Unexpected Errors
     * This is a catch-all for any unexpected exception that wasn't handled above.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        System.err.println("[ERROR] Unexpected error: " + ex.getMessage());
        ex.printStackTrace();

        String detail = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        ApiResponse<Object> response = ApiResponse.error(
                "Server error: " + detail,
                500
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
