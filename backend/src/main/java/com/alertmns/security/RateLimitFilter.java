package com.alertmns.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LOGIN_LIMIT = 10;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(5);

    private static final int FILE_UPLOAD_LIMIT = 30;
    private static final Duration FILE_UPLOAD_WINDOW = Duration.ofMinutes(10);

    private static final int AVATAR_UPLOAD_LIMIT = 15;
    private static final Duration AVATAR_UPLOAD_WINDOW = Duration.ofMinutes(10);

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        RateLimitRule rule = resolveRule(request);
        if (rule != null) {
            String key = rule.prefix + ":" + resolveClientKey(request);
            boolean allowed = rateLimitService.allowRequest(key, rule.limit, rule.window);
            if (!allowed) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"message\":\"" + rule.message + "\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitRule resolveRule(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("POST".equalsIgnoreCase(method) && "/api/auth/login".equals(path)) {
            return new RateLimitRule("login", LOGIN_LIMIT, LOGIN_WINDOW,
                    "Trop de tentatives de connexion. Réessayez dans quelques minutes.");
        }

        if ("POST".equalsIgnoreCase(method) && "/api/files/upload".equals(path)) {
            return new RateLimitRule("file-upload", FILE_UPLOAD_LIMIT, FILE_UPLOAD_WINDOW,
                    "Trop d'uploads de fichiers. Réessayez dans quelques minutes.");
        }

        if ("PUT".equalsIgnoreCase(method) && path.matches("/api/users/\\d+/avatar")) {
            return new RateLimitRule("avatar-upload", AVATAR_UPLOAD_LIMIT, AVATAR_UPLOAD_WINDOW,
                    "Trop de modifications d'avatar. Réessayez dans quelques minutes.");
        }

        return null;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record RateLimitRule(String prefix, int limit, Duration window, String message) {
    }
}
