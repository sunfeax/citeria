package com.sunfeax.citeria.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_DETAIL = "No additional details provided";
    private static final String DATA_INTEGRITY_DETAIL = "Data integrity violation: duplicate or linked record.";
    private static final String INVALID_JSON_DETAIL = "Invalid JSON format or value.";
    private static final String VALIDATION_DETAIL = "Request contains invalid fields.";
    private static final String INTERNAL_ERROR_DETAIL = "An unexpected error occurred.";

    private ProblemDetail createDetail(HttpStatus status, String title, String detail, Map<String, Object> properties) {
        String safeDetail = Optional.ofNullable(detail).orElse(DEFAULT_DETAIL);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, safeDetail);
        pd.setTitle(title);
        pd.setProperty("timestamp", Instant.now());
        if (properties != null && !properties.isEmpty()) {
            properties.forEach(pd::setProperty);
        }
        return pd;
    }

    // 404 Not Found
    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class, NoHandlerFoundException.class})
    public ProblemDetail handleNotFound(Exception ex) {
        return createDetail(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), null);
    }

    // 409 Conflict
    @ExceptionHandler({PhoneAlreadyBusyException.class, UserAlreadyExistsException.class})
    public ProblemDetail handleConflict(Exception ex) {
        return createDetail(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.debug("Data integrity violation", ex);
        return createDetail(HttpStatus.CONFLICT, "Conflict", DATA_INTEGRITY_DETAIL, null);
    }

    // Validation
    @ExceptionHandler({
        MethodArgumentNotValidException.class, 
        ConstraintViolationException.class, 
        RequestValidationException.class
    })
    public ProblemDetail handleValidationExceptions(Exception ex) {
        Map<String, String> errors = extractValidationErrors(ex);
        return createDetail(HttpStatus.BAD_REQUEST, "Validation Failed", VALIDATION_DETAIL, Map.of("errors", errors));
    }

    private Map<String, String> extractValidationErrors(Exception ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        switch (ex) {
            case MethodArgumentNotValidException e -> 
                e.getBindingResult().getFieldErrors().forEach(err -> 
                    errors.putIfAbsent(err.getField(), err.getDefaultMessage()));
            
            case ConstraintViolationException e -> 
                e.getConstraintViolations().forEach(v -> 
                    errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
            
            case RequestValidationException e -> 
                errors.putAll(e.getErrors());
            
            default -> errors.put("request", VALIDATION_DETAIL);
        }

        return errors;
    }

    // 400 Bad Request
    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleBadRequest(InvalidPasswordException ex) {
        return createDetail(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleInvalidJson(HttpMessageNotReadableException ex) {
        log.debug("Unreadable request payload", ex);
        return createDetail(HttpStatus.BAD_REQUEST, "Bad Request", INVALID_JSON_DETAIL, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
        return createDetail(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), null);
    }

    // 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        return createDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", INTERNAL_ERROR_DETAIL, null);
    }
}
