package com.alertmns.security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Intercepteur de handshake WebSocket.
 *
 * L'authentification n'est PAS effectuée au niveau HTTP/handshake car les
 * navigateurs ne peuvent pas ajouter d'en-têtes Authorization sur une
 * connexion WebSocket native, et passer le JWT en query string expose le
 * token dans les logs serveur, l'historique du navigateur et les en-têtes
 * Referer.
 *
 * L'authentification est déléguée entièrement au niveau STOMP CONNECT via
 * {@link WebSocketAuthChannelInterceptor}, qui lit l'en-tête
 * {@code Authorization: Bearer <token>} du frame CONNECT.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    /** Clé d'attribut de session utilisée par JwtPrincipalHandshakeHandler (rétrocompatibilité). */
    public static final String WS_EMAIL_ATTRIBUTE = "wsEmail";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        // Handshake toujours autorisé — l'auth JWT se fait dans WebSocketAuthChannelInterceptor.
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }
}
