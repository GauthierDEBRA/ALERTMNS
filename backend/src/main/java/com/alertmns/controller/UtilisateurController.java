package com.alertmns.controller;

import com.alertmns.dto.CreateUserRequest;
import com.alertmns.dto.ProfilePreferencesRequest;
import com.alertmns.dto.ProfileUpdateRequest;
import com.alertmns.dto.UserDto;
import com.alertmns.service.AuditLogService;
import com.alertmns.service.FileService;
import com.alertmns.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final FileService fileService;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('Admin') or hasRole('RH')")
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
    @PreAuthorize("hasRole('Admin') or hasRole('RH')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(utilisateurService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        try {
            UserDto createdUser = utilisateurService.createUser(request);
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            auditLogService.logAction(
                    requester.getIdUser(),
                    "USER_CREATE",
                    "user",
                    createdUser.getIdUser(),
                    "Création du compte " + createdUser.getEmail() + " (" + createdUser.getRole() + ")"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto, Authentication authentication) {
        try {
            UserDto updatedUser = utilisateurService.updateUser(id, userDto);
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            auditLogService.logAction(
                    requester.getIdUser(),
                    "USER_UPDATE",
                    "user",
                    updatedUser.getIdUser(),
                    "Mise à jour du compte " + updatedUser.getEmail()
            );
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/profil")
    public ResponseEntity<?> updateProfile(@PathVariable Long id,
                                           @Valid @RequestBody ProfileUpdateRequest request,
                                           Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            boolean isAdmin = "Admin".equals(requester.getRole());
            return ResponseEntity.ok(utilisateurService.updateOwnProfile(id, request, requester.getIdUser(), isAdmin));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/preferences")
    public ResponseEntity<?> updateNotificationPreferences(@PathVariable Long id,
                                                           @RequestBody ProfilePreferencesRequest request,
                                                           Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            boolean isAdmin = "Admin".equals(requester.getRole());
            return ResponseEntity.ok(
                    utilisateurService.updateNotificationPreferences(id, request, requester.getIdUser(), isAdmin)
            );
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/avatar")
    public ResponseEntity<?> updateAvatar(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file,
                                          Authentication authentication) {
        String uploadedUrl = null;
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Image requise"));
            }

            uploadedUrl = fileService.uploadAvatar(file);
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            boolean isAdmin = "Admin".equals(requester.getRole());
            return ResponseEntity.ok(utilisateurService.updateAvatar(id, uploadedUrl, requester.getIdUser(), isAdmin));
        } catch (SecurityException e) {
            if (uploadedUrl != null) {
                fileService.deleteFile(uploadedUrl);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            if (uploadedUrl != null) {
                fileService.deleteFile(uploadedUrl);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<?> deleteAvatar(@PathVariable Long id, Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            boolean isAdmin = "Admin".equals(requester.getRole());
            return ResponseEntity.ok(utilisateurService.removeAvatar(id, requester.getIdUser(), isAdmin));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id,
                                            @RequestBody Map<String, String> body,
                                            Authentication authentication) {
        try {
            String newPassword = body.get("password");
            String currentPassword = body.get("currentPassword");
            if (newPassword == null || newPassword.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mot de passe requis"));
            }
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            boolean isAdmin = "Admin".equals(requester.getRole());
            UserDto updatedUser = utilisateurService.updatePassword(id, currentPassword, newPassword, requester.getIdUser(), isAdmin);
            if (isAdmin) {
                auditLogService.logAction(
                        requester.getIdUser(),
                        "USER_PASSWORD_RESET",
                        "user",
                        updatedUser.getIdUser(),
                        "Réinitialisation du mot de passe pour " + updatedUser.getEmail()
                );
            }
            return ResponseEntity.ok(updatedUser);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            utilisateurService.deleteUser(id, requester.getIdUser());
            auditLogService.logAction(
                    requester.getIdUser(),
                    "USER_DELETE",
                    "user",
                    id,
                    "Suppression du compte utilisateur #" + id
            );
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activer")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> activateUser(@PathVariable Long id, Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            UserDto updatedUser = utilisateurService.activateUser(id, requester.getIdUser());
            auditLogService.logAction(
                    requester.getIdUser(),
                    "USER_ACTIVATE",
                    "user",
                    updatedUser.getIdUser(),
                    "Activation du compte " + updatedUser.getEmail()
            );
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/desactiver")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id, Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            UserDto updatedUser = utilisateurService.deactivateUser(id, requester.getIdUser());
            auditLogService.logAction(
                    requester.getIdUser(),
                    "USER_DEACTIVATE",
                    "user",
                    updatedUser.getIdUser(),
                    "Désactivation du compte " + updatedUser.getEmail()
            );
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/absence")
    public ResponseEntity<?> updateAbsenceMessage(@PathVariable Long id,
                                                  @RequestBody Map<String, String> body,
                                                  Authentication authentication) {
        try {
            String message = body.get("message");
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            boolean isAdmin = "Admin".equals(requester.getRole());
            return ResponseEntity.ok(utilisateurService.updateAbsenceMessage(id, message, requester.getIdUser(), isAdmin));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
