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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UtilisateurService utilisateurService;

    @GetMapping("/{canalId}")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable Long canalId) {
        return ResponseEntity.ok(messageService.getMessagesByCanal(canalId));
    }

    @PostMapping("/{canalId}")
    public ResponseEntity<?> sendMessage(@PathVariable Long canalId,
                                          @RequestBody Map<String, String> body,
                                          Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            String contenu = body.get("contenu");
            if (contenu == null || contenu.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Contenu du message requis"));
            }
            MessageDto message = messageService.sendMessage(canalId, userId, contenu);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
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
            return ResponseEntity.ok(Map.of("message", "Messages marqués comme lus"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{canalId}/export/{format}")
    public ResponseEntity<?> exportConversation(@PathVariable Long canalId,
                                                  @PathVariable String format) {
        try {
            String content = messageService.exportConversation(canalId, format);

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
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
