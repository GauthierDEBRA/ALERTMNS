package com.alertmns.service;

import com.alertmns.entity.PasswordResetToken;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.PasswordResetTokenRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    private final PasswordResetTokenRepository tokenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationEmailService emailService;

    @Transactional
    public void requestReset(String email) {
        // Always return without error even if email not found (prevents user enumeration)
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email.trim().toLowerCase())
                .orElse(null);
        if (utilisateur == null || !Boolean.TRUE.equals(utilisateur.getIsActive())) {
            log.debug("Reset demandé pour email inconnu/inactif: {}", email);
            return;
        }

        // Invalide les anciens tokens de cet utilisateur
        tokenRepository.deleteByUtilisateurId(utilisateur.getIdUser());

        String rawToken = generateSecureToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(rawToken)
                .utilisateur(utilisateur)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
                .build();
        tokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(utilisateur, rawToken);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 8 caractères");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Lien de réinitialisation invalide ou expiré"));

        if (resetToken.getUsedAt() != null) {
            throw new RuntimeException("Ce lien a déjà été utilisé");
        }
        if (LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
            throw new RuntimeException("Ce lien a expiré. Veuillez en demander un nouveau.");
        }

        Utilisateur utilisateur = resetToken.getUtilisateur();
        utilisateur.setMdp(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(utilisateur);

        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        log.info("Mot de passe réinitialisé pour l'utilisateur {}", utilisateur.getEmail());
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
