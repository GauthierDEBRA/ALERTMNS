package com.alertmns.controller;

import com.alertmns.entity.Notification;
import com.alertmns.service.NotificationService;
import com.alertmns.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurService utilisateurService;

    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication authentication,
                                               @RequestParam(required = false) Boolean nonLues) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            List<Notification> notifications;
            if (Boolean.TRUE.equals(nonLues)) {
                notifications = notificationService.getUnreadNotifications(userId);
            } else {
                notifications = notificationService.getAllNotifications(userId);
            }
            return ResponseEntity.ok(notifications);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> countUnread(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            Long count = notificationService.countUnread(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/lire")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            Notification notification = notificationService.markAsRead(id, userId);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/lire-tout")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(Map.of("message", "Toutes les notifications ont été marquées comme lues"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
