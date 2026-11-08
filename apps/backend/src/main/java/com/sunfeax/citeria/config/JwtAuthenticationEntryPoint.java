package com.sunfeax.citeria.config;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        String jwtErrorCode = (String) request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTRIBUTE);

        String code;
        String title;
        String detail;

        if ("EXPIRED".equals(jwtErrorCode)) {
            code = "TOKEN_EXPIRED";
            title = "Token Expired";
            detail = "Your access token has expired. Please log in again to obtain a new one.";
        } else if ("INVALID".equals(jwtErrorCode)) {
            code = "TOKEN_INVALID";
            title = "Invalid Token";
            detail = "The provided access token is malformed or invalid.";
        } else {
            code = "AUTHENTICATION_REQUIRED";
            title = "Unauthorized";
            detail = "Authentication is required to access this resource.";
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
