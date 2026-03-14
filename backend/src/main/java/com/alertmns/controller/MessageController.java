package com.alertmns.controller;

import com.alertmns.dto.MessageDto;
import com.alertmns.service.MessageService;
import com.alertmns.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UtilisateurService utilisateurService;

    @GetMapping("/{canalId}")
    public ResponseEntity<?> getMessages(@PathVariable Long canalId, Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            return ResponseEntity.ok(messageService.getMessagesByCanal(canalId, userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchMessages(@RequestParam("q") String query, Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            return ResponseEntity.ok(messageService.searchMessages(query, userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{canalId}")
    public ResponseEntity<?> sendMessage(@PathVariable Long canalId,
                                         @RequestBody Map<String, Object> body,
                                         Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            String contenu = body.get("contenu") != null ? body.get("contenu").toString() : null;
            String pieceJointeUrl = body.get("pieceJointeUrl") != null ? body.get("pieceJointeUrl").toString() : null;
            String pieceJointeNom = body.get("pieceJointeNom") != null ? body.get("pieceJointeNom").toString() : null;

            boolean hasContent = contenu != null && !contenu.isBlank();
            boolean hasAttachment = pieceJointeUrl != null && !pieceJointeUrl.isBlank();

            if (!hasContent && !hasAttachment) {
                return ResponseEntity.badRequest().body(Map.of("message", "Contenu du message requis"));
            }

            MessageDto message = messageService.sendMessage(canalId, userId, contenu, pieceJointeUrl, pieceJointeNom);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/item/{messageId}")
    public ResponseEntity<?> updateMessage(@PathVariable Long messageId,
                                           @RequestBody Map<String, Object> body,
                                           Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            String contenu = body.get("contenu") != null ? body.get("contenu").toString() : null;
            return ResponseEntity.ok(messageService.updateMessage(messageId, userId, contenu));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/item/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId, Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            return ResponseEntity.ok(messageService.deleteMessage(messageId, userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/item/{messageId}/reactions")
    public ResponseEntity<?> toggleReaction(@PathVariable Long messageId,
                                            @RequestBody Map<String, Object> body,
                                            Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            String emoji = body.get("emoji") != null ? body.get("emoji").toString() : null;
            return ResponseEntity.ok(messageService.toggleReaction(messageId, userId, emoji));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{canalId}/lire")
    public ResponseEntity<?> markAsRead(@PathVariable Long canalId,
                                        @RequestBody Map<String, Long> body,
                                        Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            Long lastMessageId = body.get("lastMessageId");
            if (lastMessageId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "lastMessageId requis"));
            }
            messageService.markAsRead(canalId, userId, lastMessageId);
            return ResponseEntity.ok(Map.of("message", "Messages marques comme lus"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{canalId}/export/{format}")
    public ResponseEntity<?> exportConversation(@PathVariable Long canalId,
                                                @PathVariable String format,
                                                Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            String content = messageService.exportConversation(canalId, format, userId);

            HttpHeaders headers = new HttpHeaders();
            String filename = "conversation_canal_" + canalId + "." + format.toLowerCase();
            headers.setContentDispositionFormData("attachment", filename);

            MediaType mediaType = switch (format.toLowerCase()) {
                case "json" -> MediaType.APPLICATION_JSON;
                case "csv" -> MediaType.parseMediaType("text/csv");
                case "xml" -> MediaType.APPLICATION_XML;
                default -> MediaType.TEXT_PLAIN;
            };

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(content);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
