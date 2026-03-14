package com.alertmns.controller;

import com.alertmns.dto.ParticipantReunionDto;
import com.alertmns.dto.ReunionDto;
import com.alertmns.dto.UserDto;
import com.alertmns.entity.ParticipantReunion;
import com.alertmns.entity.Reunion;
import com.alertmns.service.AuditLogService;
import com.alertmns.service.ReunionService;
import com.alertmns.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reunions")
@RequiredArgsConstructor
public class ReunionController {

    private final ReunionService reunionService;
    private final UtilisateurService utilisateurService;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<Reunion>> getAllReunions() {
        return ResponseEntity.ok(reunionService.getAllReunions());
    }

    @GetMapping("/mes-reunions")
    public ResponseEntity<?> getMesReunions(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            List<ReunionDto> reunions = reunionService.getReunionDtosForUser(userId);
            return ResponseEntity.ok(reunions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReunionById(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            return ResponseEntity.ok(reunionService.getReunionDtoForUser(id, userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createReunion(@RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            Long userId = requester.getIdUser();
            String titre = (String) body.get("titre");
            String description = (String) body.get("description");
            String datePrevueStr = (String) body.get("datePrevue");
            Integer dureeMinutes = parseDuration(body.get("dureeMinutes"));
            String lieu = (String) body.get("lieu");
            String lienVisio = (String) body.get("lienVisio");
            List<Long> participantIds = parseParticipantIds(body.get("participantIds"));

            if (titre == null || titre.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Le titre est requis"));
            }

            if (datePrevueStr == null || datePrevueStr.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "La date et l'heure sont requises"));
            }

            LocalDateTime datePrevue = datePrevueStr != null ? LocalDateTime.parse(datePrevueStr) : null;
            Reunion reunion = reunionService.createReunion(titre, description, datePrevue, dureeMinutes, lieu, lienVisio, participantIds, userId);
            logAdminRhAction(requester, "REUNION_CREATE", reunion.getIdReunion(), "Création de la réunion " + reunion.getTitre());
            return ResponseEntity.status(HttpStatus.CREATED).body(reunion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReunion(@PathVariable Long id,
                                           @RequestBody Map<String, Object> body,
                                           Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            Long userId = requester.getIdUser();
            String titre = (String) body.get("titre");
            String description = (String) body.get("description");
            String datePrevueStr = (String) body.get("datePrevue");
            Integer dureeMinutes = parseDuration(body.get("dureeMinutes"));
            String lieu = (String) body.get("lieu");
            String lienVisio = (String) body.get("lienVisio");
            LocalDateTime datePrevue = datePrevueStr != null ? LocalDateTime.parse(datePrevueStr) : null;

            Reunion reunion = reunionService.updateReunion(id, titre, description, datePrevue, dureeMinutes, lieu, lienVisio, userId);
            logAdminRhAction(requester, "REUNION_UPDATE", reunion.getIdReunion(), "Mise à jour de la réunion " + reunion.getTitre());
            return ResponseEntity.ok(reunion);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReunion(@PathVariable Long id, Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            Long userId = requester.getIdUser();
            reunionService.deleteReunion(id, userId);
            logAdminRhAction(requester, "REUNION_DELETE", id, "Suppression de la réunion #" + id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/inviter")
    public ResponseEntity<?> inviteParticipant(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body,
                                               Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            Long requesterId = requester.getIdUser();
            Long userId = Long.valueOf(body.get("userId").toString());
            ParticipantReunion participant = reunionService.inviteParticipant(id, userId, requesterId);
            logAdminRhAction(requester, "REUNION_INVITE", id, "Invitation de l'utilisateur #" + userId + " à la réunion #" + id);
            return ResponseEntity.status(HttpStatus.CREATED).body(participant);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/statut")
    public ResponseEntity<?> updateStatut(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            String statut = body.get("statut");
            if (statut == null || statut.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Statut requis"));
            }
            ParticipantReunion participant = reunionService.updateStatutParticipation(id, userId, statut);
            return ResponseEntity.ok(participant);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            List<ParticipantReunionDto> participants = reunionService.getParticipants(id, userId);
            return ResponseEntity.ok(participants);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/participants/{userId}")
    public ResponseEntity<?> removeParticipant(@PathVariable Long id,
                                               @PathVariable Long userId,
                                               Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            Long requesterId = requester.getIdUser();
            reunionService.removeParticipant(id, userId, requesterId);
            logAdminRhAction(requester, "REUNION_REMOVE_PARTICIPANT", id, "Retrait de l'utilisateur #" + userId + " de la réunion #" + id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/rappel")
    public ResponseEntity<?> sendReminder(@PathVariable Long id, Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            Long requesterId = requester.getIdUser();
            reunionService.sendReminder(id, requesterId);
            logAdminRhAction(requester, "REUNION_MANUAL_REMINDER", id, "Envoi manuel d'un rappel pour la réunion #" + id);
            return ResponseEntity.ok(Map.of("message", "Rappel envoyé"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private List<Long> parseParticipantIds(Object rawParticipantIds) {
        if (!(rawParticipantIds instanceof List<?> rawList)) {
            return List.of();
        }

        List<Long> participantIds = new ArrayList<>();
        for (Object item : rawList) {
            if (item == null) {
                continue;
            }
            participantIds.add(Long.valueOf(item.toString()));
        }
        return participantIds;
    }

    private Integer parseDuration(Object rawDuration) {
        if (rawDuration == null) {
            return null;
        }
        return Integer.valueOf(rawDuration.toString());
    }

    private void logAdminRhAction(UserDto requester, String action, Long targetId, String details) {
        if (requester == null || requester.getRole() == null) {
            return;
        }
        if (!List.of("Admin", "RH").contains(requester.getRole())) {
            return;
        }
        auditLogService.logAction(requester.getIdUser(), action, "reunion", targetId, details);
    }
}
