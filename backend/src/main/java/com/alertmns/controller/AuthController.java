package com.alertmns.controller;

import com.alertmns.dto.LoginRequest;
import com.alertmns.dto.LoginResponse;
import com.alertmns.dto.RegisterRequest;
import com.alertmns.dto.AuthSessionDto;
import com.alertmns.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpirationMs;

    @Value("${jwt.refresh-cookie-name}")
    private String refreshCookieName;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthSessionDto session = authService.login(request);
            return withRefreshCookie(ResponseEntity.ok(), session).body(session.getResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Identifiants invalides: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthSessionDto session = authService.register(request);
            return withRefreshCookie(ResponseEntity.status(HttpStatus.CREATED), session).body(session.getResponse());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        try {
            String refreshToken = extractRefreshToken(request);
            AuthSessionDto session = authService.refreshSession(refreshToken);
            return withRefreshCookie(ResponseEntity.ok(), session).body(session.getResponse());
        } catch (RuntimeException e) {
            return clearRefreshCookie(ResponseEntity.status(HttpStatus.UNAUTHORIZED))
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        authService.logout(extractRefreshToken(request));
        return clearRefreshCookie(ResponseEntity.ok())
                .body(Map.of("message", "Déconnexion effectuée"));
    }

    private ResponseEntity.BodyBuilder withRefreshCookie(ResponseEntity.BodyBuilder builder, AuthSessionDto session) {
        return builder.header(HttpHeaders.SET_COOKIE, buildRefreshCookie(session.getRefreshToken()).toString());
    }

    private ResponseEntity.BodyBuilder clearRefreshCookie(ResponseEntity.BodyBuilder builder) {
        return builder.header(HttpHeaders.SET_COOKIE, buildExpiredRefreshCookie().toString());
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(refreshCookieName, refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(refreshExpirationMs / 1000)
                .build();
    }

    private ResponseCookie buildExpiredRefreshCookie() {
        return ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (refreshCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
