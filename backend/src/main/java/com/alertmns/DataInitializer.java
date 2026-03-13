package com.alertmns;

import com.alertmns.entity.*;
import com.alertmns.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final StructureRepository structureRepository;
    private final CanalRepository canalRepository;
    private final MembreCanalRepository membreCanalRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initStructures();
        initUsers();
        initCanaux();
    }

    private void initStructures() {
        if (structureRepository.count() == 0) {
            structureRepository.saveAll(List.of(
                Structure.builder().nom("Direction Générale").typeStructure("Direction").build(),
                Structure.builder().nom("Agence Paris").typeStructure("Agence").build(),
                Structure.builder().nom("Equipe Dev").typeStructure("Equipe").build()
            ));
            log.info("Structures par défaut créées.");
        }
    }

    private void initUsers() {
        if (utilisateurRepository.count() > 0) {
            log.info("Des utilisateurs existent déjà, initialisation ignorée.");
            return;
        }

        Structure direction = structureRepository.findByNom("Direction Générale")
                .orElseGet(() -> structureRepository.save(
                    Structure.builder().nom("Direction Générale").typeStructure("Direction").build()
                ));

        Structure equipe = structureRepository.findByNom("Equipe Dev")
                .orElseGet(() -> structureRepository.save(
                    Structure.builder().nom("Equipe Dev").typeStructure("Equipe").build()
                ));

        utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin").prenom("ALERTMNS")
                .email("admin@alertmns.fr")
                .mdp(passwordEncoder.encode("Admin1234!"))
                .role("Admin").isActive(true).structure(direction).build());

        utilisateurRepository.save(Utilisateur.builder()
                .nom("Dupont").prenom("Marie")
                .email("marie@alertmns.fr")
                .mdp(passwordEncoder.encode("Marie1234!"))
                .role("RH").isActive(true).structure(direction).build());

        utilisateurRepository.save(Utilisateur.builder()
                .nom("Martin").prenom("Lucas")
                .email("lucas@alertmns.fr")
                .mdp(passwordEncoder.encode("Lucas1234!"))
                .role("Collaborateur").isActive(true).structure(equipe).build());

        log.info("Utilisateurs par défaut créés. admin@alertmns.fr / Admin1234!");
    }

    private void initCanaux() {
        if (canalRepository.count() > 0) {
            return;
        }

        Canal general = canalRepository.save(Canal.builder().nom("#general").estPrive(false).build());
        Canal dev     = canalRepository.save(Canal.builder().nom("#dev").estPrive(false).build());
        Canal rh      = canalRepository.save(Canal.builder().nom("#rh").estPrive(true).build());

        List<Utilisateur> tous = utilisateurRepository.findAll();

        for (Utilisateur u : tous) {
            String roleGeneral = "Admin".equals(u.getRole()) ? "admin" : "membre";
            addMembre(u, general, roleGeneral);

            if ("Admin".equals(u.getRole()) || "Collaborateur".equals(u.getRole()) || "Responsable".equals(u.getRole())) {
                addMembre(u, dev, "Admin".equals(u.getRole()) ? "admin" : "membre");
            }

            if ("Admin".equals(u.getRole()) || "RH".equals(u.getRole())) {
                addMembre(u, rh, "Admin".equals(u.getRole()) ? "admin" : "membre");
            }
        }

        log.info("Canaux par défaut créés: #general, #dev, #rh");
    }

    private void addMembre(Utilisateur u, Canal c, String role) {
        MembreCanalId id = new MembreCanalId(u.getIdUser(), c.getIdCanal());
        if (!membreCanalRepository.existsById(id)) {
            membreCanalRepository.save(MembreCanal.builder()
                    .id(id).utilisateur(u).canal(c).role(role).build());
        }
    }
}
