package com.alertmns.service;

import com.alertmns.entity.PasswordResetToken;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.PasswordResetTokenRepository;
import com.alertmns.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock UtilisateurRepository utilisateurRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock NotificationEmailService emailService;

    @InjectMocks PasswordResetService passwordResetService;

    @Test
    void requestReset_withUnknownEmail_doesNothing() {
        when(utilisateurRepository.findByEmail(any())).thenReturn(Optional.empty());
        passwordResetService.requestReset("unknown@test.com");
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void requestReset_withKnownEmail_savesTokenAndSendsEmail() {
        Utilisateur user = Utilisateur.builder().idUser(1L).email("user@test.com").isActive(true).build();
        when(utilisateurRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.requestReset("user@test.com");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq(user), any(String.class));
    }

    @Test
    void resetPassword_withValidToken_updatesPassword() {
        Utilisateur user = Utilisateur.builder().idUser(1L).email("user@test.com").isActive(true).build();
        PasswordResetToken token = PasswordResetToken.builder()
                .token("valid-token")
                .utilisateur(user)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        passwordResetService.resetPassword("valid-token", "newpassword123");

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        assertEquals("hashed", captor.getValue().getMdp());
        assertNotNull(token.getUsedAt());
    }

    @Test
    void resetPassword_withExpiredToken_throws() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("expired")
                .utilisateur(Utilisateur.builder().idUser(1L).build())
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> passwordResetService.resetPassword("expired", "newpassword123"));
        assertTrue(ex.getMessage().contains("expiré"));
    }

    @Test
    void resetPassword_withUsedToken_throws() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("used")
                .utilisateur(Utilisateur.builder().idUser(1L).build())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .usedAt(LocalDateTime.now().minusMinutes(5))
                .build();
        when(tokenRepository.findByToken("used")).thenReturn(Optional.of(token));

        assertThrows(RuntimeException.class,
                () -> passwordResetService.resetPassword("used", "newpassword123"));
    }

    @Test
    void resetPassword_withShortPassword_throws() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> passwordResetService.resetPassword("anytoken", "short"));
        assertTrue(ex.getMessage().contains("8 caractères"));
    }
}
