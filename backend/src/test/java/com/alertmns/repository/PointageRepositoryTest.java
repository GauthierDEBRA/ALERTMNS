package com.alertmns.repository;

import com.alertmns.entity.Pointage;
import com.alertmns.entity.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PointageRepositoryTest {

    @Autowired
    private PointageRepository pointageRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void findByUserAndDateRange_includesOvernightPointageOverlappingRequestedDay() {
        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder()
                .nom("Dupont")
                .prenom("Marie")
                .email("marie.test@alertmns.fr")
                .mdp("hash")
                .role("RH")
                .isActive(true)
                .build());

        Pointage overnight = pointageRepository.save(Pointage.builder()
                .utilisateur(utilisateur)
                .dateDebut(LocalDateTime.of(2026, 3, 13, 23, 15))
                .dateFin(LocalDateTime.of(2026, 3, 14, 8, 45))
                .build());

        pointageRepository.save(Pointage.builder()
                .utilisateur(utilisateur)
                .dateDebut(LocalDateTime.of(2026, 3, 10, 9, 0))
                .dateFin(LocalDateTime.of(2026, 3, 10, 17, 0))
                .build());

        LocalDateTime start = LocalDateTime.of(2026, 3, 14, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 14, 23, 59, 59);

        List<Pointage> results = pointageRepository.findByUserAndDateRange(utilisateur.getIdUser(), start, end);

        assertThat(results)
                .extracting(Pointage::getIdPointage)
                .contains(overnight.getIdPointage());
    }

    @Test
    void findForAdminExport_includesOvernightPointageOverlappingRequestedRange() {
        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder()
                .nom("Martin")
                .prenom("Lucas")
                .email("lucas.test@alertmns.fr")
                .mdp("hash")
                .role("Collaborateur")
                .isActive(true)
                .build());

        Pointage overnight = pointageRepository.save(Pointage.builder()
                .utilisateur(utilisateur)
                .dateDebut(LocalDateTime.of(2026, 3, 13, 22, 30))
                .dateFin(LocalDateTime.of(2026, 3, 14, 7, 30))
                .build());

        LocalDateTime start = LocalDateTime.of(2026, 3, 14, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 14, 23, 59, 59);

        List<Pointage> results = pointageRepository.findForAdminExport(utilisateur.getIdUser(), start, end);

        assertThat(results)
                .extracting(Pointage::getIdPointage)
                .contains(overnight.getIdPointage());
    }
}
