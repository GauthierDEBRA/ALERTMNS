package com.alertmns.controller;

import com.alertmns.dto.MessageDto;
import com.alertmns.service.MessageService;
import com.alertmns.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final MessageService messageService;
    private final UtilisateurService utilisateurService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{canalId}")
    public void sendMessage(@DestinationVariable Long canalId,
                            @Payload MessageDto messageDto,
                            SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            if (user == null) return;

            Long userId = utilisateurService.getUserByEmail(user.getName()).getIdUser();
            messageService.sendMessage(canalId, userId, messageDto.getContenu());
        } catch (Exception e) {
            // ignore
        }
    }

    @MessageMapping("/chat.typing/{canalId}")
    public void typing(@DestinationVariable Long canalId,
                       SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        if (user == null) return;

        try {
            var currentUser = utilisateurService.getUserByEmail(user.getName());
            Long userId = currentUser.getIdUser();
            messageService.assertUserCanAccessCanal(canalId, userId);
            messagingTemplate.convertAndSend("/topic/canal/" + canalId + "/typing",
                    java.util.Map.of(
                            "userId", userId,
                            "email", user.getName(),
                            "prenom", currentUser.getPrenom(),
                            "nom", currentUser.getNom()
                    ));
        } catch (Exception e) {
            // ignore
        }
    }
}
