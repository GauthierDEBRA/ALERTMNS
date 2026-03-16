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
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

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

    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Retourne une page de messages pour un canal, du plus récent au plus ancien.
     *
     * @param canalId          identifiant du canal
     * @param beforeId         curseur : ne retourner que les messages dont l'ID est &lt; beforeId
     *                         (null pour charger les derniers messages)
     * @param limit            nombre de messages demandés (plafonné à MAX_PAGE_SIZE)
     * @param requesterUserId  utilisateur qui fait la demande
     * @return map contenant {@code messages} (List&lt;MessageDto&gt;, ordre ASC) et
     *         {@code hasMore} (boolean)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPagedMessages(Long canalId, Long beforeId, int limit, Long requesterUserId) {
        assertUserCanAccessCanal(canalId, requesterUserId);
        int safeLimit = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        // On demande safeLimit + 1 pour détecter s'il y a d'autres messages.
        var pageable = PageRequest.of(0, safeLimit + 1);

        List<Message> raw = beforeId != null
                ? messageRepository.findBeforeIdPaged(canalId, beforeId, pageable)
                : messageRepository.findLatestByIdCanalPaged(canalId, pageable);

        boolean hasMore = raw.size() > safeLimit;
        List<Message> page = hasMore ? raw.subList(0, safeLimit) : raw;

        // Les requêtes retournent les messages du plus récent au plus ancien (DESC).
        // On inverse pour afficher en ordre chronologique (ASC).
        List<MessageDto> dtos = new ArrayList<>(page.stream().map(this::toDto).toList());
        Collections.reverse(dtos);

        return Map.of("messages", dtos, "hasMore", hasMore);
    }

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
        notifyDirectRecipients(message, canal, utilisateur, message.getContenu(), pieceJointeUrl != null && !pieceJointeUrl.isBlank());
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

    @Transactional(readOnly = true)
    public byte[] exportConversationBinary(Long canalId, String format, Long requesterUserId) {
        assertUserCanAccessCanal(canalId, requesterUserId);
        List<Message> messages = messageRepository.findByIdCanalOrderByDateEnvoi(canalId);
        List<MessageDto> dtos = messages.stream().map(this::toDto).collect(Collectors.toList());

        return switch (format.toLowerCase()) {
            case "pdf" -> exportAsPdf(dtos, canalId);
            case "xlsx" -> exportAsXlsx(dtos);
            default -> throw new RuntimeException("Format binaire non supporte: " + format);
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
                .id(message.getIdMessage())
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

    private void notifyDirectRecipients(Message message, Canal canal, Utilisateur sender, String contenu, boolean hasAttachment) {
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
                        "Nouveau message prive de " + senderName + " : " + preview,
                        "message",
                        message != null ? message.getIdMessage() : null,
                        "/chat/canal/" + canal.getIdCanal() + (message != null && message.getIdMessage() != null ? "?message=" + message.getIdMessage() : "")
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

    private byte[] exportAsXlsx(List<MessageDto> messages) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Conversation");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"ID", "Date", "Auteur", "Message"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowIdx = 1;
            for (MessageDto m : messages) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(m.getIdMessage() != null ? m.getIdMessage() : 0);
                row.createCell(1).setCellValue(m.getDateEnvoi() != null ? fmt.format(m.getDateEnvoi()) : "");
                String auteur = ((m.getUserPrenom() != null ? m.getUserPrenom() : "") + " "
                        + (m.getUserNom() != null ? m.getUserNom() : "")).trim();
                row.createCell(2).setCellValue(auteur);
                row.createCell(3).setCellValue(m.getContenu() != null ? m.getContenu() : "");
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.setColumnWidth(3, 15000);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de générer le fichier XLSX", e);
        }
    }

    private byte[] exportAsPdf(List<MessageDto> messages, Long canalId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Export conversation — canal #" + canalId,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(8f);
            document.add(title);

            Paragraph subtitle = new Paragraph(messages.size() + " message(s)",
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(14f);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(new float[]{1f, 2f, 2.5f, 6f});
            table.setWidthPercentage(100f);
            table.setHeaderRows(1);

            for (String h : new String[]{"ID", "Date", "Auteur", "Message"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h,
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                cell.setBackgroundColor(new java.awt.Color(30, 64, 175));
                cell.setPadding(5f);
                table.addCell(cell);
            }

            for (MessageDto m : messages) {
                table.addCell(new Phrase(String.valueOf(m.getIdMessage() != null ? m.getIdMessage() : ""),
                        FontFactory.getFont(FontFactory.HELVETICA, 9)));
                table.addCell(new Phrase(m.getDateEnvoi() != null ? fmt.format(m.getDateEnvoi()) : "",
                        FontFactory.getFont(FontFactory.HELVETICA, 9)));
                String auteur = ((m.getUserPrenom() != null ? m.getUserPrenom() : "") + " "
                        + (m.getUserNom() != null ? m.getUserNom() : "")).trim();
                table.addCell(new Phrase(auteur, FontFactory.getFont(FontFactory.HELVETICA, 9)));
                String contenu = m.getContenu() != null ? m.getContenu() : "";
                table.addCell(new Phrase(contenu.length() > 500 ? contenu.substring(0, 497) + "..." : contenu,
                        FontFactory.getFont(FontFactory.HELVETICA, 9)));
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Généré le " + fmt.format(java.time.LocalDateTime.now()),
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9));
            footer.setSpacingBefore(12f);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Impossible de générer le fichier PDF", e);
        }
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
                .id(message.getIdMessage())
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
