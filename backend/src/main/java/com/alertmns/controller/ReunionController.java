package com.alertmns.controller;

import com.alertmns.entity.ParticipantReunion;
import com.alertmns.entity.Reunion;
import com.alertmns.service.ReunionService;
import com.alertmns.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reunions")
@RequiredArgsConstructor
public class ReunionController {

    private final ReunionService reunionService;
    private final UtilisateurService utilisateurService;

    @GetMapping
    public ResponseEntity<List<Reunion>> getAllReunions() {
        return ResponseEntity.ok(reunionService.getAllReunions());
    }

    @GetMapping("/mes-reunions")
    public ResponseEntity<?> getMesReunions(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            return ResponseEntity.ok(reunionService.getReunionsForUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReunionById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reunionService.getReunionById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createReunion(@RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            String titre = (String) body.get("titre");
            String description = (String) body.get("description");
            String datePrevueStr = (String) body.get("datePrevue");

            if (titre == null || titre.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Le titre est requis"));
            }

            LocalDateTime datePrevue = datePrevueStr != null ? LocalDateTime.parse(datePrevueStr) : null;
            Reunion reunion = reunionService.createReunion(titre, description, datePrevue, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(reunion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReunion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String titre = (String) body.get("titre");
            String description = (String) body.get("description");
            String datePrevueStr = (String) body.get("datePrevue");
            LocalDateTime datePrevue = datePrevueStr != null ? LocalDateTime.parse(datePrevueStr) : null;

            Reunion reunion = reunionService.updateReunion(id, titre, description, datePrevue);
            return ResponseEntity.ok(reunion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReunion(@PathVariable Long id) {
        try {
            reunionService.deleteReunion(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/inviter")
    public ResponseEntity<?> inviteParticipant(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            ParticipantReunion participant = reunionService.inviteParticipant(id, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(participant);
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
    public ResponseEntity<?> getParticipants(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reunionService.getParticipants(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
