package com.sunfeax.citeria.config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> AUTH_PATHS = Set.of("/api/auth/login", "/api/auth/register");
    private static final List<String> PUBLIC_READ_PREFIXES = List.of("/api/offerings", "/api/specialist-detail");

    private final ObjectMapper objectMapper;

    @Value("${app.ratelimit.auth.capacity:10}")
    private long capacity;

    @Value("${app.ratelimit.auth.refillMinutes:1}")
    private long refillMinutes;

    @Value("${app.ratelimit.public.capacity:120}")
    private long publicCapacity;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isAuthAttempt(request) && !isPublicRead(request);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        boolean authAttempt = isAuthAttempt(request);
        String key = (authAttempt ? "auth:" : "public:") + request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> newBucket(authAttempt ? capacity : publicCapacity));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            writeTooManyRequests(response);
        }
    }

    private boolean isAuthAttempt(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && AUTH_PATHS.contains(request.getServletPath());
    }

    private boolean isPublicRead(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod())
            && PUBLIC_READ_PREFIXES.stream().anyMatch(request.getServletPath()::startsWith);
    }

    private Bucket newBucket(long tokens) {
        Bandwidth limit = Bandwidth.builder()
            .capacity(tokens)
            .refillGreedy(tokens, Duration.ofMinutes(refillMinutes))
            .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS,
            "Too many requests. Please wait a moment and try again."
        );
        problem.setTitle("Too Many Requests");
        problem.setProperty("code", "RATE_LIMITED");
        problem.setProperty("timestamp", Instant.now());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(Duration.ofMinutes(refillMinutes).toSeconds()));
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
