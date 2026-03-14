package com.alertmns.service;

import com.alertmns.entity.RefreshToken;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.RefreshTokenRepository;
import com.alertmns.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(RefreshTokenService.class)
@TestPropertySource(properties = "jwt.refresh-expiration=2592000000")
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void consumeToken_revokesStoredTokenAndReturnsLinkedUser() {
        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin")
                .prenom("Goatze")
                .email("admin.refresh@alertmns.fr")
                .mdp("hash")
                .role("Admin")
                .isActive(true)
                .build());

        String rawToken = refreshTokenService.issueToken(utilisateur);

        Utilisateur refreshedUser = refreshTokenService.consumeToken(rawToken);

        assertThat(refreshedUser.getIdUser()).isEqualTo(utilisateur.getIdUser());
        RefreshToken storedToken = refreshTokenRepository.findAll().get(0);
        assertThat(storedToken.getIsRevoked()).isTrue();
        assertThat(storedToken.getLastUsedAt()).isNotNull();
    }

    @Test
    void revokeToken_marksExistingTokenAsRevoked() {
        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder()
                .nom("Dupont")
                .prenom("Marie")
                .email("marie.refresh@alertmns.fr")
                .mdp("hash")
                .role("RH")
                .isActive(true)
                .build());

        String rawToken = refreshTokenService.issueToken(utilisateur);

        refreshTokenService.revokeToken(rawToken);

        RefreshToken storedToken = refreshTokenRepository.findAll().get(0);
        assertThat(storedToken.getIsRevoked()).isTrue();
        assertThat(storedToken.getLastUsedAt()).isNotNull();
    }
}
