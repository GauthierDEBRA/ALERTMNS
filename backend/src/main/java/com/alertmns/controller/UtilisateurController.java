package com.alertmns.controller;

import com.alertmns.dto.UserDto;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    @PreAuthorize("hasRole('Admin') or hasRole('RH') or hasRole('Responsable')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(utilisateurService.getAllUsers());
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        return ResponseEntity.ok(utilisateurService.getActiveUsers());
    }

    @GetMapping("/presence")
    public ResponseEntity<List<UserDto>> getPresentUsers() {
        return ResponseEntity.ok(utilisateurService.getPresentUsers());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        try {
            UserDto user = utilisateurService.getUserByEmail(authentication.getName());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(utilisateurService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        try {
            return ResponseEntity.ok(utilisateurService.updateUser(id, userDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String newPassword = body.get("password");
            if (newPassword == null || newPassword.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mot de passe requis"));
            }
            return ResponseEntity.ok(utilisateurService.updatePassword(id, newPassword));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            utilisateurService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activer")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(utilisateurService.activateUser(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/desactiver")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(utilisateurService.deactivateUser(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/absence")
    public ResponseEntity<?> updateAbsenceMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String message = body.get("message");
            return ResponseEntity.ok(utilisateurService.updateAbsenceMessage(id, message));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
