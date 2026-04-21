package ru.job4j.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /* Обработка ResponseStatusException - возвращаем правильный статус */
    @ExceptionHandler(value = { ResponseStatusException.class })
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException e, HttpServletRequest request) {
        LOGGER.error("ResponseStatusException at: " + request.getRequestURI(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getReason());
        errorResponse.put("status", e.getStatusCode().value());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }

    /* Обработка NullPointerException */
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

    /* Обработка IllegalArgumentException */
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

    /* Обработка всех остальных исключений */
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