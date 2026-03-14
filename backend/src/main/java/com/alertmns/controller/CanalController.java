package com.alertmns.controller;

import com.alertmns.dto.CanalDto;
import com.alertmns.entity.Canal;
import com.alertmns.entity.MembreCanal;
import com.alertmns.entity.Utilisateur;
import com.alertmns.service.AuditLogService;
import com.alertmns.service.CanalService;
import com.alertmns.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/canaux")
@RequiredArgsConstructor
public class CanalController {

    private final CanalService canalService;
    private final UtilisateurService utilisateurService;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<CanalDto>> getAllCanaux() {
        return ResponseEntity.ok(canalService.getAllCanaux().stream().map(this::toDto).toList());
    }

    @GetMapping("/mes-canaux")
    public ResponseEntity<?> getMesCanaux(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            List<Canal> canaux = canalService.getCanauxForUser(userId);
            return ResponseEntity.ok(canaux.stream().map(canal -> toDto(canal, userId)).toList());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCanalById(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            return ResponseEntity.ok(toDto(canalService.getCanalByIdForUser(id, userId), userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> createCanal(@RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            String nom = (String) body.get("nom");
            Boolean estPrive = body.get("estPrive") != null ? (Boolean) body.get("estPrive") : false;
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();

            Canal canal = canalService.createCanal(nom, estPrive, userId);
            auditLogService.logAction(
                    userId,
                    "CHANNEL_CREATE",
                    "canal",
                    canal.getIdCanal(),
                    "Création du canal " + canal.getNom()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(canal));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/direct/{userId}")
    public ResponseEntity<?> createDirectCanal(@PathVariable Long userId, Authentication authentication) {
        try {
            Long requesterId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            Canal canal = canalService.createDirectCanal(requesterId, userId);
            return ResponseEntity.ok(toDto(canal, requesterId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> updateCanal(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            String nom = (String) body.get("nom");
            Boolean estPrive = body.get("estPrive") != null ? (Boolean) body.get("estPrive") : null;
            Canal canal = canalService.updateCanal(id, nom, estPrive);
            Long requesterId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            auditLogService.logAction(
                    requesterId,
                    "CHANNEL_UPDATE",
                    "canal",
                    canal.getIdCanal(),
                    "Mise à jour du canal " + canal.getNom()
            );
            return ResponseEntity.ok(toDto(canal));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> deleteCanal(@PathVariable Long id, Authentication authentication) {
        try {
            canalService.deleteCanal(id);
            Long requesterId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            auditLogService.logAction(
                    requesterId,
                    "CHANNEL_DELETE",
                    "canal",
                    id,
                    "Suppression du canal #" + id
            );
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/membres")
    public ResponseEntity<?> getMembres(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            return ResponseEntity.ok(canalService.getMembres(id, userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/membres")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> addMember(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String role = body.get("role") != null ? (String) body.get("role") : "membre";
            MembreCanal membre = canalService.addMember(id, userId, role);
            Long requesterId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            auditLogService.logAction(
                    requesterId,
                    "CHANNEL_MEMBER_ADD",
                    "canal",
                    id,
                    "Ajout de l'utilisateur #" + userId + " au canal #" + id + " avec le rôle " + role
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(membre);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/membres/{userId}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> updateMemberRole(@PathVariable Long id,
                                              @PathVariable Long userId,
                                              @RequestBody Map<String, Object> body,
                                              Authentication authentication) {
        try {
            String role = body.get("role") != null ? (String) body.get("role") : "membre";
            MembreCanal membre = canalService.updateMemberRole(id, userId, role);
            Long requesterId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            auditLogService.logAction(
                    requesterId,
                    "CHANNEL_MEMBER_ROLE_UPDATE",
                    "canal",
                    id,
                    "Changement du rôle de l'utilisateur #" + userId + " dans le canal #" + id + " vers " + role
            );
            return ResponseEntity.ok(membre);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/membres/{userId}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> removeMember(@PathVariable Long id, @PathVariable Long userId, Authentication authentication) {
        try {
            canalService.removeMember(id, userId);
            Long requesterId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            auditLogService.logAction(
                    requesterId,
                    "CHANNEL_MEMBER_REMOVE",
                    "canal",
                    id,
                    "Retrait de l'utilisateur #" + userId + " du canal #" + id
            );
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private CanalDto toDto(Canal canal) {
        return toDto(canal, null);
    }

    private CanalDto toDto(Canal canal, Long requesterId) {
        CanalDto.CanalDtoBuilder builder = CanalDto.builder()
                .id(canal.getIdCanal())
                .idCanal(canal.getIdCanal())
                .nom(canal.getNom())
                .estPrive(canal.getEstPrive())
                .typeCanal(canal.getTypeCanal())
                .membresCount(canal.getMembresCount());

        if (requesterId != null && "direct".equalsIgnoreCase(canal.getTypeCanal())) {
            Utilisateur directPeer = canalService.getDirectPeer(canal.getIdCanal(), requesterId);
            if (directPeer != null) {
                builder.directUserId(directPeer.getIdUser())
                        .directUserNom(directPeer.getNom())
                        .directUserPrenom(directPeer.getPrenom())
                        .directUserEmail(directPeer.getEmail())
                        .directUserAvatarUrl(directPeer.getAvatarUrl());
            }
        }

        return builder.build();
    }
}
