package com.ecommerce.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler that intercepts exceptions thrown anywhere in
 * the controller layer and returns structured JSON error responses.
 *
 * <p>Handles:</p>
 * <ul>
 *   <li>{@link EntityNotFoundException} → 404 Not Found</li>
 *   <li>{@link DataIntegrityViolationException} → 400 Bad Request
 *       (e.g. duplicate unique key, FK constraint violation)</li>
 *   <li>{@link MethodArgumentNotValidException} → 422 Unprocessable Entity
 *       (Bean Validation failures on {@code @RequestBody})</li>
 *   <li>{@link Exception} → 500 Internal Server Error (catch-all)</li>
 * </ul>
 *
 * @author  Your Name
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ─────────────────────────────────────────────────────
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 400 Bad Request (DB constraint violations) ─────────────────────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {
        String message = "Database constraint violation. "
                + "Check for duplicate values or missing required fields.";
        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    // ── 422 Unprocessable Entity (validation errors) ──────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildError(HttpStatus.UNPROCESSABLE_ENTITY, errors);
    }

    // ── 500 Internal Server Error (catch-all) ─────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage());
    }

    // ── Helper ─────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
