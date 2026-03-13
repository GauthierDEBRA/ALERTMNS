package com.alertmns.service;

import com.alertmns.entity.Notification;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.NotificationRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Notification createNotification(Long userId, String type, String contenu) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));

        Notification notification = Notification.builder()
                .type(type)
                .contenu(contenu)
                .isLu(false)
                .utilisateur(utilisateur)
                .build();

        notification = notificationRepository.save(notification);

        // Push via WebSocket
        try {
            messagingTemplate.convertAndSend("/queue/notifications/" + userId, notification);
        } catch (Exception e) {
            // WebSocket might not be available, ignore silently
        }

        return notification;
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByIdUserAndIsLu(userId, false);
    }

    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Long countUnread(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public Notification markAsRead(Long notifId, Long userId) {
        Notification notification = notificationRepository.findById(notifId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée: " + notifId));

        if (!notification.getUtilisateur().getIdUser().equals(userId)) {
            throw new RuntimeException("Accès non autorisé à cette notification");
        }

        notification.setIsLu(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByIdUserAndIsLu(userId, false);
        notifications.forEach(n -> n.setIsLu(true));
        notificationRepository.saveAll(notifications);
    }
}
