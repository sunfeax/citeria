package com.sunfeax.citeria.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_DETAIL = "No additional details provided";
    private static final String DATA_INTEGRITY_DETAIL = "Data integrity violation: duplicate or linked record.";
    private static final String OVERLAP_DETAIL = "The specialist is already booked for the selected time period.";
    private static final String INVALID_JSON_DETAIL = "Invalid JSON format or value.";
    private static final String VALIDATION_DETAIL = "Request contains invalid fields.";
    private static final String INTERNAL_ERROR_DETAIL = "An unexpected error occurred.";
    private static final Map<String, String> EMPTY_ERRORS = Map.of();

    private ProblemDetail createDetail(
        HttpStatus status,
        String code,
        String title,
        String detail,
        Map<String, String> errors
    ) {
        String safeDetail = Optional.ofNullable(detail).orElse(DEFAULT_DETAIL);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, safeDetail);
        pd.setTitle(title);
        pd.setProperty("code", code);
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("errors", normalizeErrors(errors));
        return pd;
    }

    // 404 Not Found
    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class, NoHandlerFoundException.class})
    public ProblemDetail handleNotFound(Exception ex) {
        return createDetail(
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND",
            "Resource Not Found",
            ex.getMessage(),
            EMPTY_ERRORS
        );
    }

    @ExceptionHandler({UserNotFoundException.class})
    public ProblemDetail handleUserNotFound(Exception ex) {
        return createDetail(
            HttpStatus.NOT_FOUND,
            "USER_NOT_FOUND",
            "User Not Found",
            ex.getMessage(),
            EMPTY_ERRORS
        );
    }

    // 409 Conflict
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.debug("Data integrity violation", ex);

        Throwable specificCause = ex.getMostSpecificCause();
        
        String message = (specificCause != null) ? specificCause.getMessage() : ex.getMessage();

        if (message != null && message.contains("exclude_overlapping_appointments")) {
            return createDetail(
                HttpStatus.CONFLICT,
                "SLOT_ALREADY_BOOKED",
                "Slot already booked",
                OVERLAP_DETAIL,
                Map.of("time", OVERLAP_DETAIL)
            );
        }

        return createDetail(
            HttpStatus.CONFLICT,
            "CONFLICT",
            "Conflict",
            DATA_INTEGRITY_DETAIL,
            EMPTY_ERRORS
        );
    }

    // 400 Bad Request (Validation)
    @ExceptionHandler({
        MethodArgumentNotValidException.class, 
        ConstraintViolationException.class, 
        RequestValidationException.class
    })
    public ProblemDetail handleValidationExceptions(Exception ex) {
        Map<String, String> errors = extractValidationErrors(ex);
        return createDetail(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Validation Failed",
            VALIDATION_DETAIL,
            errors
        );
    }

    private Map<String, String> extractValidationErrors(Exception ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        switch (ex) {
            case MethodArgumentNotValidException e -> 
                e.getBindingResult().getFieldErrors().forEach(err -> 
                    errors.putIfAbsent(err.getField(), err.getDefaultMessage()));
            
            case ConstraintViolationException e -> 
                e.getConstraintViolations().forEach(v -> 
                    errors.putIfAbsent(resolveConstraintField(v), v.getMessage()));
            
            case RequestValidationException e -> 
                errors.putAll(e.getErrors());
            
            default -> errors.put("request", VALIDATION_DETAIL);
        }

        return errors;
    }

    private String resolveConstraintField(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }

    private Map<String, String> normalizeErrors(Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) {
            return EMPTY_ERRORS;
        }
        return Map.copyOf(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleInvalidJson(HttpMessageNotReadableException ex) {
        log.debug("Unreadable request payload", ex);
        return createDetail(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST_BODY",
            "Bad Request",
            INVALID_JSON_DETAIL,
            Map.of("request", INVALID_JSON_DETAIL)
        );
    }
    
    // 401 Unauthorized
    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
        return createDetail(
            HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED",
            "Unauthorized",
            ex.getMessage(),
            EMPTY_ERRORS
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        return createDetail(
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_FAILED",
            "Authentication Failed", 
            "Invalid email or password",
            EMPTY_ERRORS
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        return createDetail(
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_FAILED",
            "Authentication Failed",
            "Authentication was not successful",
            EMPTY_ERRORS
        );
    }

    // 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        return createDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "Internal Server Error",
            INTERNAL_ERROR_DETAIL,
            EMPTY_ERRORS
        );
    }
}
