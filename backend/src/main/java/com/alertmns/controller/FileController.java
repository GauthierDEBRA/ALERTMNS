package com.alertmns.controller;

import com.alertmns.entity.Message;
import com.alertmns.entity.PieceJointe;
import com.alertmns.repository.MessageRepository;
import com.alertmns.repository.PieceJointeRepository;
import com.alertmns.service.FileService;
import com.alertmns.service.MessageService;
import com.alertmns.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final MessageRepository messageRepository;
    private final PieceJointeRepository pieceJointeRepository;
    private final MessageService messageService;
    private final UtilisateurService utilisateurService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "messageId", required = false) Long messageId,
                                        Authentication authentication) {
        try {
            String url = fileService.uploadAttachment(file);
            String originalFilename = file.getOriginalFilename();

            // If messageId provided, attach file to message
            if (messageId != null) {
                Long requesterId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
                Message message = messageRepository.findById(messageId)
                        .orElseThrow(() -> new RuntimeException("Message non trouvé: " + messageId));
                messageService.assertUserCanAccessCanal(message.getCanal().getIdCanal(), requesterId);

                PieceJointe pj = PieceJointe.builder()
                        .nomFichier(originalFilename)
                        .url(url)
                        .message(message)
                        .build();
                pj = pieceJointeRepository.save(pj);

                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                        "url", url,
                        "nomFichier", originalFilename,
                        "idPj", pj.getIdPj()
                ));
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "url", url,
                    "nomFichier", originalFilename != null ? originalFilename : "file"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
