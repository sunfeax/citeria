package com.sunfeax.citeria.config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    private static final Set<String> LIMITED_PATHS = Set.of("/api/auth/login", "/api/auth/register");

    private final ObjectMapper objectMapper;

    @Value("${app.ratelimit.auth.capacity:10}")
    private long capacity;

    @Value("${app.ratelimit.auth.refillMinutes:1}")
    private long refillMinutes;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && LIMITED_PATHS.contains(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        Bucket bucket = buckets.computeIfAbsent(clientIp(request), key -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            writeTooManyRequests(response);
        }
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(capacity)
            .refillGreedy(capacity, Duration.ofMinutes(refillMinutes))
            .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS,
            "Too many attempts. Please wait a moment and try again."
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
