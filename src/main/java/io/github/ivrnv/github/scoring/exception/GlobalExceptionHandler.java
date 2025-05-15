package io.github.ivrnv.github.scoring.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for standardizing API error responses.
 * Provides consistent error handling for common exceptions in the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        logger.error("Request parameter '{}' is missing", name);
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Missing Parameter");
        body.put("message", "Required parameter '" + name + "' is missing");
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        logger.error("Validation error: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation Error");
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({DateTimeParseException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleParseExceptions(Exception ex) {
        logger.error("Parameter parsing error: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Invalid Parameter");
        body.put("message", "Invalid parameter format: " + ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<Object> handleGitHubApiException(GitHubApiException ex) {
        HttpStatus status = ex.getStatusCode() != null 
            ? HttpStatus.valueOf(ex.getStatusCode().value()) 
            : HttpStatus.SERVICE_UNAVAILABLE;
        
        logger.error("GitHub API error: {} - {}", status, ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "GitHub API Error");
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(InvalidRepositoryDataException.class)
    public ResponseEntity<Object> handleInvalidRepositoryDataException(InvalidRepositoryDataException ex) {
        logger.error("Invalid repository data: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Invalid Repository Data");
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralExceptions(Exception ex) {
        logger.error("Unhandled exception", ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred");
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
