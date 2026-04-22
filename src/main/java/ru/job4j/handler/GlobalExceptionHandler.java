package ru.job4j.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /* ========== ОБРАБОТКА ВАЛИДАЦИИ (НОВЫЙ МЕТОД) ========== */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        LOGGER.error("Validation error at: " + request.getRequestURI(), ex);

        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::createErrorMap)
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    private Map<String, String> createErrorMap(FieldError error) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("field", error.getField());
        errorMap.put("message", error.getDefaultMessage());
        errorMap.put("rejectedValue", String.valueOf(error.getRejectedValue()));
        return errorMap;
    }

    /* ========== Обработка ResponseStatusException ========== */

    @ExceptionHandler(value = { ResponseStatusException.class })
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException e, HttpServletRequest request) {
        LOGGER.error("ResponseStatusException at: " + request.getRequestURI(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getReason());
        errorResponse.put("status", e.getStatusCode().value());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }

    /* ========== Обработка NullPointerException ========== */

    @ExceptionHandler(value = { NullPointerException.class })
    public ResponseEntity<Map<String, Object>> handleNullPointer(NullPointerException e, HttpServletRequest request) {
        LOGGER.error("NullPointerException at: " + request.getRequestURI(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Required field is missing or null");
        errorResponse.put("details", e.getMessage());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("path", request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /* ========== Обработка IllegalArgumentException ========== */

    @ExceptionHandler(value = { IllegalArgumentException.class })
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        LOGGER.error("IllegalArgumentException at: " + request.getRequestURI(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Invalid input data");
        errorResponse.put("details", e.getMessage());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("path", request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /* ========== Обработка всех остальных исключений ========== */

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception e, HttpServletRequest request) {
        LOGGER.error("Unexpected error at: " + request.getRequestURI(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Internal server error");
        errorResponse.put("details", e.getMessage());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("path", request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}