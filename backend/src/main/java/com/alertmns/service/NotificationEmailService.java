package com.alertmns.service;

import com.alertmns.entity.Notification;
import com.alertmns.entity.Utilisateur;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class NotificationEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${alertmns.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${alertmns.mail.from:no-reply@alertmns.local}")
    private String fromAddress;

    @Value("${alertmns.mail.from-name:ALERTMNS}")
    private String fromName;

    @Value("${alertmns.app-base-url:http://localhost}")
    private String appBaseUrl;

    public NotificationEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void sendNotificationEmail(Notification notification) {
        if (!mailEnabled || notification == null || notification.getUtilisateur() == null) {
            return;
        }

        Utilisateur recipient = notification.getUtilisateur();
        if (recipient.getEmail() == null || recipient.getEmail().isBlank()) {
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.debug("Mail sender unavailable, skip email notification for user {}", recipient.getIdUser());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient.getEmail());
            message.setFrom(fromAddress);
            message.setSubject(buildSubject(notification));
            message.setText(buildBody(recipient, notification));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send notification email to {}", recipient.getEmail(), e);
        }
    }

    private String buildSubject(Notification notification) {
        String normalizedType = notification.getType() == null
                ? "NOTIFICATION"
                : notification.getType().trim().toUpperCase(Locale.ROOT);

        return switch (normalizedType) {
            case "MESSAGE" -> fromName + " - Nouveau message";
            case "REUNION" -> fromName + " - Mise a jour reunion";
            case "ABSENCE" -> fromName + " - Notification absence";
            default -> fromName + " - Nouvelle notification";
        };
    }

    private String buildBody(Utilisateur recipient, Notification notification) {
        String prenom = recipient.getPrenom() != null && !recipient.getPrenom().isBlank()
                ? recipient.getPrenom()
                : "Bonjour";
        String targetUrl = buildTargetUrl(notification);

        return """
                Bonjour %s,

                Vous avez une nouvelle notification dans ALERTMNS :

                %s

                Ouvrir directement : %s

                Cet email a ete envoye automatiquement par ALERTMNS.
                """.formatted(prenom, safeContent(notification.getContenu()), targetUrl);
    }

    private String safeContent(String content) {
        return content == null || content.isBlank()
                ? "Une nouvelle activite vous attend dans l'application."
                : content;
    }

    private String buildTargetUrl(Notification notification) {
        if (notification == null || notification.getTargetRoute() == null || notification.getTargetRoute().isBlank()) {
            return appBaseUrl;
        }

        if (notification.getTargetRoute().startsWith("http://") || notification.getTargetRoute().startsWith("https://")) {
            return notification.getTargetRoute();
        }

        return appBaseUrl + notification.getTargetRoute();
    }
}
