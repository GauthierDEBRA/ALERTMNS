package com.alertmns.service;

import com.alertmns.dto.MessageDto;
import com.alertmns.entity.*;
import com.alertmns.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final CanalRepository canalRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LectureEtatRepository lectureEtatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByCanal(Long canalId) {
        return messageRepository.findByIdCanalOrderByDateEnvoi(canalId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDto sendMessage(Long canalId, Long userId, String contenu) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new RuntimeException("Canal non trouvé: " + canalId));
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));

        Message message = Message.builder()
                .contenu(contenu)
                .dateEnvoi(LocalDateTime.now())
                .utilisateur(utilisateur)
                .canal(canal)
                .build();

        message = messageRepository.save(message);
        MessageDto dto = toDto(message);

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/canal/" + canalId, dto);

        return dto;
    }

    @Transactional
    public void markAsRead(Long canalId, Long userId, Long lastMessageId) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new RuntimeException("Canal non trouvé"));
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        LectureEtatId id = new LectureEtatId(userId, canalId);
        LectureEtat etat = lectureEtatRepository.findByIdIdUserAndIdIdCanal(userId, canalId)
                .orElse(LectureEtat.builder()
                        .id(id)
                        .utilisateur(utilisateur)
                        .canal(canal)
                        .build());

        etat.setDernierIdMsgLu(lastMessageId);
        lectureEtatRepository.save(etat);
    }

    @Transactional(readOnly = true)
    public String exportConversation(Long canalId, String format) {
        List<Message> messages = messageRepository.findByIdCanalOrderByDateEnvoi(canalId);
        List<MessageDto> dtos = messages.stream().map(this::toDto).collect(Collectors.toList());

        return switch (format.toLowerCase()) {
            case "json" -> exportAsJson(dtos);
            case "csv" -> exportAsCsv(dtos);
            case "xml" -> exportAsXml(dtos);
            default -> throw new RuntimeException("Format non supporté: " + format);
        };
    }

    private String exportAsJson(List<MessageDto> messages) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(messages);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'export JSON: " + e.getMessage());
        }
    }

    private String exportAsCsv(List<MessageDto> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,date,auteur,contenu\n");
        for (MessageDto m : messages) {
            sb.append(m.getIdMessage()).append(",");
            sb.append(m.getDateEnvoi()).append(",");
            sb.append("\"").append(m.getUserPrenom()).append(" ").append(m.getUserNom()).append("\"").append(",");
            sb.append("\"").append(m.getContenu().replace("\"", "\"\"")).append("\"");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String exportAsXml(List<MessageDto> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<conversation>\n");
        for (MessageDto m : messages) {
            sb.append("  <message>\n");
            sb.append("    <id>").append(m.getIdMessage()).append("</id>\n");
            sb.append("    <date>").append(m.getDateEnvoi()).append("</date>\n");
            sb.append("    <auteur>").append(m.getUserPrenom()).append(" ").append(m.getUserNom()).append("</auteur>\n");
            sb.append("    <contenu><![CDATA[").append(m.getContenu()).append("]]></contenu>\n");
            sb.append("  </message>\n");
        }
        sb.append("</conversation>");
        return sb.toString();
    }

    public MessageDto toDto(Message message) {
        List<MessageDto.PieceJointeDto> pjDtos = null;
        if (message.getPiecesJointes() != null) {
            pjDtos = message.getPiecesJointes().stream()
                    .map(pj -> MessageDto.PieceJointeDto.builder()
                            .idPj(pj.getIdPj())
                            .nomFichier(pj.getNomFichier())
                            .url(pj.getUrl())
                            .build())
                    .collect(Collectors.toList());
        }

        return MessageDto.builder()
                .idMessage(message.getIdMessage())
                .contenu(message.getContenu())
                .dateEnvoi(message.getDateEnvoi())
                .canalId(message.getCanal() != null ? message.getCanal().getIdCanal() : null)
                .userId(message.getUtilisateur() != null ? message.getUtilisateur().getIdUser() : null)
                .userNom(message.getUtilisateur() != null ? message.getUtilisateur().getNom() : null)
                .userPrenom(message.getUtilisateur() != null ? message.getUtilisateur().getPrenom() : null)
                .userEmail(message.getUtilisateur() != null ? message.getUtilisateur().getEmail() : null)
                .piecesJointes(pjDtos)
                .build();
    }
}
