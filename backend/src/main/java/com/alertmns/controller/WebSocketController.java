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
import java.util.HashMap;
import java.util.Map;

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
            Map<String, Object> typingPayload = new HashMap<>();
            typingPayload.put("userId", userId);
            typingPayload.put("email", user.getName() != null ? user.getName() : "");
            typingPayload.put("prenom", currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
            typingPayload.put("nom", currentUser.getNom() != null ? currentUser.getNom() : "");
            messagingTemplate.convertAndSend("/topic/canal/" + canalId + "/typing", typingPayload);
        } catch (Exception e) {
            // ignore
        }
    }
}
