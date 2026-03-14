package com.alertmns.service;

import com.alertmns.dto.MessageDto;
import com.alertmns.dto.MessageSearchResultDto;
import com.alertmns.entity.Canal;
import com.alertmns.entity.LectureEtat;
import com.alertmns.entity.LectureEtatId;
import com.alertmns.entity.MembreCanal;
import com.alertmns.entity.Message;
import com.alertmns.entity.PieceJointe;
import com.alertmns.entity.ReactionMessage;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.CanalRepository;
import com.alertmns.repository.LectureEtatRepository;
import com.alertmns.repository.MembreCanalRepository;
import com.alertmns.repository.MessageRepository;
import com.alertmns.repository.PieceJointeRepository;
import com.alertmns.repository.ReactionMessageRepository;
import com.alertmns.repository.UtilisateurRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final CanalRepository canalRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MembreCanalRepository membreCanalRepository;
    private final LectureEtatRepository lectureEtatRepository;
    private final PieceJointeRepository pieceJointeRepository;
    private final ReactionMessageRepository reactionMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final FileService fileService;
    private static final Set<String> ALLOWED_REACTIONS = Set.of("👍", "❤️", "😂", "🎉", "✅", "👀");

    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByCanal(Long canalId, Long requesterUserId) {
        assertUserCanAccessCanal(canalId, requesterUserId);
        return messageRepository.findByIdCanalOrderByDateEnvoi(canalId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageSearchResultDto> searchMessages(String query, Long requesterUserId) {
        String normalizedQuery = query != null ? query.trim() : "";
        if (normalizedQuery.length() < 2) {
            throw new RuntimeException("La recherche doit contenir au moins 2 caractères");
        }

        Map<Long, String> conversationNameCache = new HashMap<>();
        Map<Long, String> conversationTypeCache = new HashMap<>();

        return messageRepository.searchAccessibleMessages(requesterUserId, normalizedQuery).stream()
                .limit(50)
                .map(message -> toSearchDto(message, normalizedQuery, requesterUserId, conversationNameCache, conversationTypeCache))
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDto sendMessage(Long canalId, Long userId, String contenu) {
        return sendMessage(canalId, userId, contenu, null, null);
    }

    @Transactional
    public MessageDto sendMessage(Long canalId, Long userId, String contenu, String pieceJointeUrl, String pieceJointeNom) {
        assertUserCanAccessCanal(canalId, userId);

        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new RuntimeException("Canal non trouve: " + canalId));
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve: " + userId));

        Message message = Message.builder()
                .contenu(contenu != null ? contenu : "")
                .dateEnvoi(LocalDateTime.now())
                .utilisateur(utilisateur)
                .canal(canal)
                .build();

        message = messageRepository.save(message);

        if (pieceJointeUrl != null && !pieceJointeUrl.isBlank()) {
            PieceJointe pieceJointe = PieceJointe.builder()
                    .nomFichier(pieceJointeNom != null && !pieceJointeNom.isBlank() ? pieceJointeNom : "piece-jointe")
                    .url(pieceJointeUrl)
                    .message(message)
                    .build();
            pieceJointe = pieceJointeRepository.save(pieceJointe);
            message.setPiecesJointes(new ArrayList<>(List.of(pieceJointe)));
        }

        MessageDto dto = toDto(message);
        broadcastMessageUpdate(dto, canal.getIdCanal());
        notifyDirectRecipients(canal, utilisateur, message.getContenu(), pieceJointeUrl != null && !pieceJointeUrl.isBlank());
        return dto;
    }

    @Transactional
    public MessageDto updateMessage(Long messageId, Long userId, String contenu) {
        if (contenu == null || contenu.isBlank()) {
            throw new RuntimeException("Contenu du message requis");
        }

        Message message = getManagedMessage(messageId);
        assertUserCanAccessCanal(message.getCanal().getIdCanal(), userId);
        assertAuthor(message, userId, "Vous ne pouvez modifier que vos propres messages");

        if (Boolean.TRUE.equals(message.getIsDeleted())) {
            throw new RuntimeException("Impossible de modifier un message supprime");
        }

        message.setContenu(contenu.trim());
        message.setDateModification(LocalDateTime.now());
        message = messageRepository.save(message);

        MessageDto dto = toDto(message);
        broadcastMessageUpdate(dto, message.getCanal().getIdCanal());
        return dto;
    }

    @Transactional
    public MessageDto deleteMessage(Long messageId, Long userId) {
        Message message = getManagedMessage(messageId);
        assertUserCanAccessCanal(message.getCanal().getIdCanal(), userId);
        assertAuthor(message, userId, "Vous ne pouvez supprimer que vos propres messages");

        if (!Boolean.TRUE.equals(message.getIsDeleted())) {
            deleteAttachments(messageId);
            deleteReactions(messageId);
            message.setContenu("Message supprime");
            message.setIsDeleted(true);
            message.setDateModification(LocalDateTime.now());
            if (message.getPiecesJointes() != null) {
                message.getPiecesJointes().clear();
            }
            if (message.getReactions() != null) {
                message.getReactions().clear();
            }
            message = messageRepository.save(message);
        }

        MessageDto dto = toDto(message);
        broadcastMessageUpdate(dto, message.getCanal().getIdCanal());
        return dto;
    }

    @Transactional
    public MessageDto toggleReaction(Long messageId, Long userId, String emoji) {
        String normalizedEmoji = emoji != null ? emoji.trim() : "";
        if (!ALLOWED_REACTIONS.contains(normalizedEmoji)) {
            throw new RuntimeException("Reaction non supportee");
        }

        Message message = getManagedMessage(messageId);
        assertUserCanAccessCanal(message.getCanal().getIdCanal(), userId);

        if (Boolean.TRUE.equals(message.getIsDeleted())) {
            throw new RuntimeException("Impossible de reagir a un message supprime");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve: " + userId));

        reactionMessageRepository.findByMessageIdMessageAndUtilisateurIdUserAndEmoji(messageId, userId, normalizedEmoji)
                .ifPresentOrElse(
                        reactionMessageRepository::delete,
                        () -> reactionMessageRepository.save(ReactionMessage.builder()
                                .emoji(normalizedEmoji)
                                .message(message)
                                .utilisateur(utilisateur)
                                .build())
                );

        Message refreshed = getManagedMessage(messageId);
        MessageDto dto = toDto(refreshed);
        broadcastMessageUpdate(dto, refreshed.getCanal().getIdCanal());
        return dto;
    }

    @Transactional
    public void markAsRead(Long canalId, Long userId, Long lastMessageId) {
        assertUserCanAccessCanal(canalId, userId);

        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new RuntimeException("Canal non trouve"));
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));

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
    public String exportConversation(Long canalId, String format, Long requesterUserId) {
        assertUserCanAccessCanal(canalId, requesterUserId);
        List<Message> messages = messageRepository.findByIdCanalOrderByDateEnvoi(canalId);
        List<MessageDto> dtos = messages.stream().map(this::toDto).collect(Collectors.toList());

        return switch (format.toLowerCase()) {
            case "json" -> exportAsJson(dtos);
            case "csv" -> exportAsCsv(dtos);
            case "xml" -> exportAsXml(dtos);
            default -> throw new RuntimeException("Format non supporte: " + format);
        };
    }

    public void assertUserCanAccessCanal(Long canalId, Long userId) {
        if (!canalRepository.existsById(canalId)) {
            throw new RuntimeException("Canal non trouve: " + canalId);
        }
        if (userId == null || !membreCanalRepository.existsByIdIdUserAndIdIdCanal(userId, canalId)) {
            throw new SecurityException("Acces refuse a cette conversation");
        }
    }

    private Message getManagedMessage(Long messageId) {
        return messageRepository.findDetailedById(messageId)
                .orElseThrow(() -> new RuntimeException("Message non trouve: " + messageId));
    }

    private void assertAuthor(Message message, Long userId, String errorMessage) {
        if (message.getUtilisateur() == null || userId == null || !userId.equals(message.getUtilisateur().getIdUser())) {
            throw new SecurityException(errorMessage);
        }
    }

    private void deleteAttachments(Long messageId) {
        List<PieceJointe> pieces = pieceJointeRepository.findByMessageIdMessage(messageId);
        for (PieceJointe piece : pieces) {
            if (piece.getUrl() != null && !piece.getUrl().isBlank()) {
                fileService.deleteFile(piece.getUrl());
            }
        }
        if (!pieces.isEmpty()) {
            pieceJointeRepository.deleteAll(pieces);
        }
    }

    private void deleteReactions(Long messageId) {
        reactionMessageRepository.deleteByMessageIdMessage(messageId);
    }

    private MessageSearchResultDto toSearchDto(Message message,
                                               String query,
                                               Long requesterUserId,
                                               Map<Long, String> conversationNameCache,
                                               Map<Long, String> conversationTypeCache) {
        Long canalId = message.getCanal() != null ? message.getCanal().getIdCanal() : null;
        String conversationName = canalId != null
                ? conversationNameCache.computeIfAbsent(canalId, ignored -> resolveConversationName(message.getCanal(), requesterUserId))
                : "Conversation";
        String conversationType = canalId != null
                ? conversationTypeCache.computeIfAbsent(canalId, ignored -> message.getCanal() != null ? message.getCanal().getTypeCanal() : "canal")
                : "canal";

        return MessageSearchResultDto.builder()
                .idMessage(message.getIdMessage())
                .canalId(canalId)
                .conversationName(conversationName)
                .typeCanal(conversationType)
                .extrait(buildExcerpt(message.getContenu(), query))
                .dateEnvoi(message.getDateEnvoi())
                .userId(message.getUtilisateur() != null ? message.getUtilisateur().getIdUser() : null)
                .userNom(message.getUtilisateur() != null ? message.getUtilisateur().getNom() : null)
                .userPrenom(message.getUtilisateur() != null ? message.getUtilisateur().getPrenom() : null)
                .userAvatarUrl(message.getUtilisateur() != null ? message.getUtilisateur().getAvatarUrl() : null)
                .build();
    }

    private String resolveConversationName(Canal canal, Long requesterUserId) {
        if (canal == null) {
            return "Conversation";
        }

        if (!"direct".equalsIgnoreCase(canal.getTypeCanal())) {
            String canalName = canal.getNom() != null ? canal.getNom() : "canal";
            return canalName.startsWith("#") ? canalName : "#" + canalName;
        }

        return membreCanalRepository.findByCanalId(canal.getIdCanal()).stream()
                .map(MembreCanal::getUtilisateur)
                .filter(utilisateur -> utilisateur != null && !utilisateur.getIdUser().equals(requesterUserId))
                .findFirst()
                .map(utilisateur -> (utilisateur.getPrenom() + " " + utilisateur.getNom()).trim())
                .filter(name -> !name.isBlank())
                .orElse("Message privé");
    }

    private String buildExcerpt(String contenu, String query) {
        if (contenu == null || contenu.isBlank()) {
            return "";
        }

        String text = contenu.replaceAll("\\s+", " ").trim();
        String normalizedText = text.toLowerCase();
        String normalizedQuery = query.toLowerCase();
        int matchIndex = normalizedText.indexOf(normalizedQuery);

        if (matchIndex == -1 || text.length() <= 120) {
            return text.length() <= 120 ? text : text.substring(0, 117) + "...";
        }

        int start = Math.max(0, matchIndex - 30);
        int end = Math.min(text.length(), matchIndex + query.length() + 50);
        String excerpt = text.substring(start, end).trim();

        if (start > 0) {
            excerpt = "..." + excerpt;
        }
        if (end < text.length()) {
            excerpt = excerpt + "...";
        }

        return excerpt;
    }

    private void broadcastMessageUpdate(MessageDto dto, Long canalId) {
        messagingTemplate.convertAndSend("/topic/canal/" + canalId, dto);
    }

    private void notifyDirectRecipients(Canal canal, Utilisateur sender, String contenu, boolean hasAttachment) {
        if (canal == null || sender == null || !"direct".equalsIgnoreCase(canal.getTypeCanal())) {
            return;
        }

        String senderName = (sender.getPrenom() + " " + sender.getNom()).trim();
        String preview;
        if (contenu != null && !contenu.isBlank()) {
            String trimmed = contenu.trim();
            preview = trimmed.length() > 80 ? trimmed.substring(0, 77) + "..." : trimmed;
        } else if (hasAttachment) {
            preview = "vous a envoye une piece jointe";
        } else {
            preview = "vous a envoye un message";
        }

        for (MembreCanal member : membreCanalRepository.findByCanalId(canal.getIdCanal())) {
            Long recipientId = member.getUtilisateur().getIdUser();
            if (recipientId != null && !recipientId.equals(sender.getIdUser())) {
                notificationService.createNotification(
                        recipientId,
                        "MESSAGE",
                        "Nouveau message prive de " + senderName + " : " + preview
                );
            }
        }
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
            sb.append("\"").append((m.getContenu() != null ? m.getContenu() : "").replace("\"", "\"\"")).append("\"");
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
        if (!Boolean.TRUE.equals(message.getIsDeleted()) && message.getPiecesJointes() != null) {
            pjDtos = message.getPiecesJointes().stream()
                    .map(pj -> MessageDto.PieceJointeDto.builder()
                            .idPj(pj.getIdPj())
                            .nomFichier(pj.getNomFichier())
                            .url(pj.getUrl())
                            .build())
                    .collect(Collectors.toList());
        }

        List<MessageDto.ReactionDto> reactionDtos = null;
        List<ReactionMessage> detailedReactions = null;
        if (!Boolean.TRUE.equals(message.getIsDeleted()) && message.getIdMessage() != null) {
            detailedReactions = reactionMessageRepository.findDetailedByMessageIdMessage(message.getIdMessage());
        }

        if (detailedReactions != null && !detailedReactions.isEmpty()) {
            Map<String, List<Long>> groupedReactions = new LinkedHashMap<>();
            for (ReactionMessage reaction : detailedReactions) {
                if (reaction.getEmoji() == null || reaction.getUtilisateur() == null || reaction.getUtilisateur().getIdUser() == null) {
                    continue;
                }
                groupedReactions
                        .computeIfAbsent(reaction.getEmoji(), ignored -> new ArrayList<>())
                        .add(reaction.getUtilisateur().getIdUser());
            }

            reactionDtos = groupedReactions.entrySet().stream()
                    .map(entry -> MessageDto.ReactionDto.builder()
                            .emoji(entry.getKey())
                            .count(entry.getValue().size())
                            .userIds(entry.getValue())
                            .build())
                    .collect(Collectors.toList());
        }

        return MessageDto.builder()
                .idMessage(message.getIdMessage())
                .contenu(message.getContenu())
                .dateEnvoi(message.getDateEnvoi())
                .dateModification(message.getDateModification())
                .isDeleted(message.getIsDeleted())
                .canalId(message.getCanal() != null ? message.getCanal().getIdCanal() : null)
                .userId(message.getUtilisateur() != null ? message.getUtilisateur().getIdUser() : null)
                .userNom(message.getUtilisateur() != null ? message.getUtilisateur().getNom() : null)
                .userPrenom(message.getUtilisateur() != null ? message.getUtilisateur().getPrenom() : null)
                .userEmail(message.getUtilisateur() != null ? message.getUtilisateur().getEmail() : null)
                .userAvatarUrl(message.getUtilisateur() != null ? message.getUtilisateur().getAvatarUrl() : null)
                .piecesJointes(pjDtos)
                .reactions(reactionDtos)
                .build();
    }
}
