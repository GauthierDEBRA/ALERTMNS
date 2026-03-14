package com.alertmns.service;

import com.alertmns.entity.RefreshToken;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.RefreshTokenRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpirationMs;

    @Transactional
    public String issueToken(Utilisateur utilisateur) {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        String rawToken = generateOpaqueToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(hashToken(rawToken))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000))
                .utilisateur(utilisateur)
                .build();
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public Utilisateur consumeToken(String rawToken) {
        RefreshToken token = findValidToken(rawToken);
        token.setIsRevoked(true);
        token.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);
        return utilisateurRepository.findById(token.getUtilisateur().getIdUser())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable pour ce refresh token"));
    }

    @Transactional
    public void revokeToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hashToken(rawToken)).ifPresent(token -> {
            if (!Boolean.TRUE.equals(token.getIsRevoked())) {
                token.setIsRevoked(true);
                token.setLastUsedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    private RefreshToken findValidToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new RuntimeException("Refresh token manquant");
        }

        RefreshToken token = refreshTokenRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new RuntimeException("Refresh token invalide"));

        if (Boolean.TRUE.equals(token.getIsRevoked())) {
            throw new RuntimeException("Refresh token révoqué");
        }
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expiré");
        }
        return token;
    }

    private String generateOpaqueToken() {
        byte[] randomBytes = new byte[48];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
