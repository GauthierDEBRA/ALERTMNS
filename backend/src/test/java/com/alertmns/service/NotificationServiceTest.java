package com.alertmns.service;

import com.alertmns.entity.Notification;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.NotificationRepository;
import com.alertmns.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(NotificationService.class)
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private NotificationEmailService notificationEmailService;

    private Utilisateur saveUser(boolean notifyReunions, boolean notifyMessages, boolean notifyAbsences) {
        return utilisateurRepository.save(Utilisateur.builder()
                .nom("Test")
                .prenom("User")
                .email("user_" + System.nanoTime() + "@alertmns.fr")
                .mdp("hash")
                .role("Collaborateur")
                .isActive(true)
                .notifyReunions(notifyReunions)
                .notifyMessages(notifyMessages)
                .notifyAbsences(notifyAbsences)
                .build());
    }

    @Test
    void createNotification_persistsNotificationWithCorrectFields() {
        Utilisateur user = saveUser(true, true, true);

        Notification result = notificationService.createNotification(
                user.getIdUser(), "REUNION", "Vous êtes invité à une réunion",
                "reunion", 5L, "/reunions?focus=5");

        assertThat(result).isNotNull();
        assertThat(result.getIdNotif()).isNotNull();
        assertThat(result.getType()).isEqualTo("REUNION");
        assertThat(result.getContenu()).isEqualTo("Vous êtes invité à une réunion");
        assertThat(result.getTargetType()).isEqualTo("reunion");
        assertThat(result.getTargetId()).isEqualTo(5L);
        assertThat(result.getTargetRoute()).isEqualTo("/reunions?focus=5");
        assertThat(result.getIsLu()).isFalse();
    }

    @Test
    void createNotification_returnsNullWhenReunionNotificationsDisabled() {
        Utilisateur user = saveUser(false, true, true);

        Notification result = notificationService.createNotification(
                user.getIdUser(), "REUNION", "Invitation réunion");

        assertThat(result).isNull();
        assertThat(notificationRepository.count()).isZero();
    }

    @Test
    void createNotification_returnsNullWhenMessageNotificationsDisabled() {
        Utilisateur user = saveUser(true, false, true);

        Notification result = notificationService.createNotification(
                user.getIdUser(), "MESSAGE", "Nouveau message");

        assertThat(result).isNull();
    }

    @Test
    void createNotification_returnsNullWhenAbsenceNotificationsDisabled() {
        Utilisateur user = saveUser(true, true, false);

        Notification result = notificationService.createNotification(
                user.getIdUser(), "ABSENCE", "Absence signalée");

        assertThat(result).isNull();
    }

    @Test
    void createNotification_alwaysSendsInfoTypeRegardlessOfPreferences() {
        // INFO type has no user preference — always sent
        Utilisateur user = saveUser(false, false, false);

        Notification result = notificationService.createNotification(
                user.getIdUser(), "INFO", "Message d'information");

        assertThat(result).isNotNull();
    }

    @Test
    void createNotification_typeIsCaseInsensitive() {
        Utilisateur user = saveUser(true, true, true);

        Notification result = notificationService.createNotification(
                user.getIdUser(), "reunion", "Minuscules");

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("reunion");
    }
}
