package com.alertmns.security;

import com.alertmns.repository.CanalRepository;
import com.alertmns.repository.MembreCanalRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final String SESSION_EMAIL_KEY = "wsEmail";

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UtilisateurRepository utilisateurRepository;
    private final CanalRepository canalRepository;
    private final MembreCanalRepository membreCanalRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Principal existingPrincipal = accessor.getUser();
            if (existingPrincipal == null) {
                existingPrincipal = restorePrincipal(accessor);
            }
            if (existingPrincipal == null) {
                authenticate(accessor);
            }
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        Principal principal = accessor.getUser();
        if (principal == null) {
            principal = restorePrincipal(accessor);
        }
        if (principal == null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            return message;
        }
        if (principal == null) {
            throw new AccessDeniedException("Connexion WebSocket non authentifiée");
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
            authorizeDestination(accessor, principal);
        }

        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = firstHeader(accessor, "Authorization");
        String token = extractBearerToken(authHeader);
        if (token == null || !jwtUtil.isTokenValid(token)) {
            throw new AccessDeniedException("Jeton WebSocket invalide ou manquant");
        }

        String email = jwtUtil.extractEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                token,
                userDetails.getAuthorities()
        );
        accessor.setUser(authentication);
        if (accessor.getSessionAttributes() != null) {
            accessor.getSessionAttributes().put(SESSION_EMAIL_KEY, email);
        }
    }

    private void authorizeDestination(StompHeaderAccessor accessor, Principal principal) {
        String destination = accessor.getDestination();
        if (destination == null || destination.isBlank()) {
            return;
        }

        Long userId = utilisateurRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("Utilisateur WebSocket introuvable"))
                .getIdUser();

        if ("/topic/presence".equals(destination) || destination.startsWith("/user/queue/notifications")) {
            return;
        }

        Long canalId = extractCanalId(destination);
        if (canalId != null) {
            assertUserCanAccessCanal(canalId, userId);
            return;
        }

        if (destination.startsWith("/queue/notifications/")) {
            throw new AccessDeniedException("Destination de notifications obsolète non autorisée");
        }
    }

    private Long extractCanalId(String destination) {
        List<String> prefixes = List.of("/topic/canal/", "/app/chat.typing/", "/app/chat.sendMessage/");
        for (String prefix : prefixes) {
            if (destination.startsWith(prefix)) {
                String remainder = destination.substring(prefix.length());
                String idPart = remainder.contains("/") ? remainder.substring(0, remainder.indexOf('/')) : remainder;
                try {
                    return Long.parseLong(idPart);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private void assertUserCanAccessCanal(Long canalId, Long userId) {
        if (!canalRepository.existsById(canalId)) {
            throw new AccessDeniedException("Canal WebSocket introuvable");
        }
        if (userId == null || !membreCanalRepository.existsByIdIdUserAndIdIdCanal(userId, canalId)) {
            throw new AccessDeniedException("Accès WebSocket refusé à cette conversation");
        }
    }

    private String firstHeader(StompHeaderAccessor accessor, String headerName) {
        String value = accessor.getFirstNativeHeader(headerName);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return accessor.getFirstNativeHeader(headerName.toLowerCase());
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    private Principal restorePrincipal(StompHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() == null) {
            return null;
        }

        Object emailValue = accessor.getSessionAttributes().get(SESSION_EMAIL_KEY);
        if (!(emailValue instanceof String email) || email.isBlank()) {
            return null;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        accessor.setUser(authentication);
        return authentication;
    }
}
