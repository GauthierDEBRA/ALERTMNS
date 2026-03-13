package com.alertmns.controller;

import com.alertmns.entity.Canal;
import com.alertmns.entity.MembreCanal;
import com.alertmns.service.CanalService;
import com.alertmns.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<Canal>> getAllCanaux() {
        return ResponseEntity.ok(canalService.getAllCanaux());
    }

    @GetMapping("/mes-canaux")
    public ResponseEntity<?> getMesCanaux(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            List<Canal> canaux = canalService.getCanauxForUser(userId);
            return ResponseEntity.ok(canaux);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCanalById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(canalService.getCanalById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCanal(@RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            String nom = (String) body.get("nom");
            Boolean estPrive = body.get("estPrive") != null ? (Boolean) body.get("estPrive") : false;
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();

            Canal canal = canalService.createCanal(nom, estPrive, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(canal);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCanal(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String nom = (String) body.get("nom");
            Boolean estPrive = body.get("estPrive") != null ? (Boolean) body.get("estPrive") : null;
            Canal canal = canalService.updateCanal(id, nom, estPrive);
            return ResponseEntity.ok(canal);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCanal(@PathVariable Long id) {
        try {
            canalService.deleteCanal(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/membres")
    public ResponseEntity<?> getMembres(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(canalService.getMembres(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/membres")
    public ResponseEntity<?> addMember(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String role = body.get("role") != null ? (String) body.get("role") : "member";
            MembreCanal membre = canalService.addMember(id, userId, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(membre);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/membres/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        try {
            canalService.removeMember(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
