package com.alertmns.service;

import com.alertmns.repository.PasswordResetTokenRepository;
import com.alertmns.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Nettoyage périodique des tokens expirés.
 * Évite l'accumulation infinie de lignes mortes en base.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /** Toutes les nuits à 3h00. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        int deletedReset = passwordResetTokenRepository.deleteExpiredOrUsed(now);
        int deletedRefresh = refreshTokenRepository.deleteExpired(now);

        if (deletedReset > 0 || deletedRefresh > 0) {
            log.info("Nettoyage tokens : {} reset supprimés, {} refresh supprimés",
                    deletedReset, deletedRefresh);
        }
    }
}
