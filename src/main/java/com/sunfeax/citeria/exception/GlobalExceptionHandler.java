package com.sunfeax.citeria.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        pd.setTitle("Resource Not Found");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorizedException(UnauthorizedException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        pd.setTitle("Unauthorized");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    @ExceptionHandler(PhoneAlreadyBusyException.class)
    public ProblemDetail handlePhoneAlreadyBusyException(PhoneAlreadyBusyException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);

        pd.setTitle("Conflict");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);

        pd.setTitle("Conflict");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        pd.setTitle("Validation Failed");
        pd.setDetail("Request contains invalid fields.");
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        pd.setTitle("Validation Failed");
        pd.setDetail("Request contains invalid JSON or enum value.");
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getConstraintViolations()
            .forEach(violation -> errors.putIfAbsent(
                violation.getPropertyPath().toString(),
                violation.getMessage())
            );

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        pd.setTitle("Validation Failed");
        pd.setDetail("Request contains invalid data.");
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandledException(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        
        pd.setTitle("Internal Server Error");
        pd.setDetail("An unexpected error occurred.");
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }
}
