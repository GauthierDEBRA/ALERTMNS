package com.alertmns.controller;

import com.alertmns.dto.PointageStatsDto;
import com.alertmns.dto.UserDto;
import com.alertmns.entity.Pointage;
import com.alertmns.service.PointageService;
import com.alertmns.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pointage")
@RequiredArgsConstructor
public class PointageController {

    private final PointageService pointageService;
    private final UtilisateurService utilisateurService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/arriver")
    public ResponseEntity<?> pointerArrivee(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            Pointage pointage = pointageService.pointerArrivee(userId);
            broadcastPresence();
            return ResponseEntity.status(HttpStatus.CREATED).body(pointage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/partir")
    public ResponseEntity<?> pointerDepart(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            Pointage pointage = pointageService.pointerDepart(userId);
            broadcastPresence();
            return ResponseEntity.ok(pointage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private void broadcastPresence() {
        List<UserDto> presentUsers = pointageService.getPresentUsers();
        messagingTemplate.convertAndSend("/topic/presence", presentUsers);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            PointageStatsDto stats = pointageService.getStats(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/stats/{userId}")
    @PreAuthorize("hasRole('Admin') or hasRole('RH') or hasRole('Responsable')")
    public ResponseEntity<?> getStatsForUser(@PathVariable Long userId) {
        try {
            PointageStatsDto stats = pointageService.getStats(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/presents")
    public ResponseEntity<List<UserDto>> getPresentUsers() {
        return ResponseEntity.ok(pointageService.getPresentUsers());
    }

    @GetMapping("/historique")
    public ResponseEntity<?> getHistorique(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            List<Pointage> pointages = pointageService.getPointagesForUser(userId);
            return ResponseEntity.ok(pointages);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/historique/{userId}")
    @PreAuthorize("hasRole('Admin') or hasRole('RH') or hasRole('Responsable')")
    public ResponseEntity<?> getHistoriqueForUser(@PathVariable Long userId) {
        try {
            List<Pointage> pointages = pointageService.getPointagesForUser(userId);
            return ResponseEntity.ok(pointages);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
