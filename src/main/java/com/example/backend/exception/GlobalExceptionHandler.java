package com.example.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(400).body("File quá lớn! Vui lòng chọn file nhỏ hơn 10MB.");
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateResourceException(DuplicateResourceException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", e.getMessage());
        return ResponseEntity.status(400).body(errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(400).body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> errors = new HashMap<>();
        String fullMessage = e.getMessage();
        System.err.println("IllegalArgumentException: " + fullMessage);

        if (fullMessage != null && fullMessage.contains(":")) {
            String[] parts = fullMessage.split(":", 2);
            errors.put(parts[0], parts[1]);
            errors.put("message", parts[1]); // General message for the toast
        } else {
            errors.put("message", fullMessage);
        }
        return ResponseEntity.status(400).body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message",
                "Dữ liệu gửi lên không đúng định dạng. Chi tiết: " + e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(400).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace(); // Log lỗi ra console server
        return ResponseEntity.status(500).body("Lỗi máy chủ: " + e.getMessage());
    }
}
