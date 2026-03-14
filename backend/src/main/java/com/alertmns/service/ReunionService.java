package com.alertmns.service;

import com.alertmns.dto.ParticipantReunionDto;
import com.alertmns.dto.ReunionDto;
import com.alertmns.entity.ParticipantReunion;
import com.alertmns.entity.ParticipantReunionId;
import com.alertmns.entity.Reunion;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.ParticipantReunionRepository;
import com.alertmns.repository.ReunionRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReunionService {

    private static final DateTimeFormatter CONFLICT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a' HH:mm");
    private static final DateTimeFormatter NOTIFICATION_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a' HH:mm", Locale.FRENCH);
    private static final long DAY_REMINDER_MINUTES = 24 * 60;
    private static final long DAY_REMINDER_TOLERANCE_MINUTES = 5;
    private static final long HOUR_REMINDER_MINUTES = 60;
    private static final long HOUR_REMINDER_TOLERANCE_MINUTES = 5;
    private static final long THIRTY_MIN_REMINDER_MINUTES = 30;
    private static final long THIRTY_MIN_REMINDER_TOLERANCE_MINUTES = 5;

    private final ReunionRepository reunionRepository;
    private final ParticipantReunionRepository participantReunionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Reunion> getAllReunions() {
        return reunionRepository.findAllOrderByDatePrevue();
    }

    @Transactional(readOnly = true)
    public List<Reunion> getReunionsForUser(Long userId) {
        return reunionRepository.findReunionsForUser(userId);
    }

    @Transactional(readOnly = true)
    public List<ReunionDto> getReunionDtosForUser(Long userId) {
        Map<Long, ParticipantReunion> participationsByReunion = participantReunionRepository.findByIdIdUser(userId)
                .stream()
                .collect(Collectors.toMap(
                        participant -> participant.getId().getIdReunion(),
                        Function.identity(),
                        (first, second) -> first
                ));

        return reunionRepository.findReunionsForUser(userId)
                .stream()
                .map(reunion -> toDto(reunion, participationsByReunion.get(reunion.getIdReunion()), userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Reunion getReunionById(Long id) {
        return reunionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée: " + id));
    }

    @Transactional(readOnly = true)
    public ReunionDto getReunionDtoForUser(Long reunionId, Long userId) {
        Reunion reunion = getManagedReunion(reunionId);
        assertCanAccessReunion(reunion, userId);
        ParticipantReunion participation = participantReunionRepository
                .findByIdIdReunionAndIdIdUser(reunionId, userId)
                .orElse(null);
        return toDto(reunion, participation, userId);
    }

    @Transactional
    public Reunion createReunion(String titre,
                                 String description,
                                 LocalDateTime datePrevue,
                                 Integer dureeMinutes,
                                 String lieu,
                                 String lienVisio,
                                 List<Long> participantIds,
                                 Long createurId) {
        Utilisateur createur = utilisateurRepository.findById(createurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + createurId));

        LocalDateTime normalizedDate = requireDatePrevue(datePrevue);
        int normalizedDuration = requireDureeMinutes(dureeMinutes);
        List<Long> normalizedParticipantIds = sanitizeParticipantIds(participantIds, createurId);
        assertNoScheduleConflict(normalizedDate, normalizedDuration, null, buildAffectedUserIds(createurId, normalizedParticipantIds));

        Reunion reunion = Reunion.builder()
                .titre(titre)
                .description(normalizeNullableString(description))
                .datePrevue(normalizedDate)
                .dureeMinutes(normalizedDuration)
                .lieu(normalizeNullableString(lieu))
                .lienVisio(normalizeNullableString(lienVisio))
                .createur(createur)
                .build();

        reunion = reunionRepository.save(reunion);

        participantReunionRepository.save(buildParticipant(reunion, createur, "organisateur"));

        for (Long participantId : normalizedParticipantIds) {
            Utilisateur utilisateur = utilisateurRepository.findById(participantId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + participantId));
            participantReunionRepository.save(buildParticipant(reunion, utilisateur, "en_attente"));

            notificationService.createNotification(
                    participantId,
                    "REUNION",
                    "Vous avez été invité à la réunion: " + reunion.getTitre(),
                    "reunion",
                    reunion.getIdReunion(),
                    buildReunionTargetRoute(reunion.getIdReunion())
            );
        }

        return reunion;
    }

    @Transactional
    public Reunion updateReunion(Long id,
                                 String titre,
                                 String description,
                                 LocalDateTime datePrevue,
                                 Integer dureeMinutes,
                                 String lieu,
                                 String lienVisio,
                                 Long requesterId) {
        Reunion reunion = getManagedReunion(id);
        assertOrganizer(reunion, requesterId);

        LocalDateTime normalizedDate = requireDatePrevue(datePrevue);
        int normalizedDuration = requireDureeMinutes(dureeMinutes);
        LocalDateTime previousDatePrevue = reunion.getDatePrevue();
        assertNoScheduleConflict(
                normalizedDate,
                normalizedDuration,
                reunion.getIdReunion(),
                participantReunionRepository.findByIdIdReunion(reunion.getIdReunion())
                        .stream()
                        .filter(participant -> !"refuse".equals(normalizeStatus(participant.getStatut())))
                        .map(participant -> participant.getId().getIdUser())
                        .toList()
        );

        reunion.setTitre(titre);
        reunion.setDescription(normalizeNullableString(description));
        reunion.setDatePrevue(normalizedDate);
        reunion.setDureeMinutes(normalizedDuration);
        reunion.setLieu(normalizeNullableString(lieu));
        reunion.setLienVisio(normalizeNullableString(lienVisio));
        refreshAutomaticReminderFlags(reunion, previousDatePrevue);
        Reunion saved = reunionRepository.save(reunion);
        notifyParticipantsOfUpdate(saved, requesterId);
        return saved;
    }

    @Transactional
    public void deleteReunion(Long id, Long requesterId) {
        Reunion reunion = getManagedReunion(id);
        assertOrganizer(reunion, requesterId);
        notifyParticipantsOfCancellation(reunion, requesterId);
        reunionRepository.delete(reunion);
    }

    @Transactional
    public ParticipantReunion inviteParticipant(Long reunionId, Long userId, Long requesterId) {
        Reunion reunion = getManagedReunion(reunionId);
        assertOrganizer(reunion, requesterId);
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));

        if (reunion.getCreateur() != null && reunion.getCreateur().getIdUser().equals(userId)) {
            throw new RuntimeException("L'organisateur est déjà participant à cette réunion");
        }

        ParticipantReunionId participantId = new ParticipantReunionId(reunionId, userId);
        if (participantReunionRepository.existsById(participantId)) {
            throw new RuntimeException("L'utilisateur est déjà invité à cette réunion");
        }

        assertNoScheduleConflict(reunion.getDatePrevue(), reunion.getDureeMinutes(), reunion.getIdReunion(), List.of(userId));

        ParticipantReunion participant = buildParticipant(reunion, utilisateur, "en_attente");

        participant = participantReunionRepository.save(participant);

        // Send notification
        notificationService.createNotification(
                userId,
                "REUNION",
                "Vous avez été invité à la réunion: " + reunion.getTitre(),
                "reunion",
                reunion.getIdReunion(),
                buildReunionTargetRoute(reunion.getIdReunion())
        );

        return participant;
    }

    @Transactional
    public ParticipantReunion updateStatutParticipation(Long reunionId, Long userId, String statut) {
        ParticipantReunion participant = participantReunionRepository
                .findByIdIdReunionAndIdIdUser(reunionId, userId)
                .orElseThrow(() -> new RuntimeException("Participation non trouvée"));
        if ("organisateur".equals(normalizeStatus(participant.getStatut()))) {
            throw new RuntimeException("L'organisateur n'a pas de réponse à enregistrer");
        }

        String normalizedStatus = normalizeEditableStatus(statut);
        participant.setStatut(normalizedStatus);
        participant = participantReunionRepository.save(participant);

        Utilisateur organisateur = participant.getReunion().getCreateur();
        Utilisateur repondant = participant.getUtilisateur();
        if (organisateur != null && repondant != null && !organisateur.getIdUser().equals(repondant.getIdUser())) {
            String action = "accepte".equals(normalizedStatus) ? "accepté" : "refusé";
            notificationService.createNotification(
                    organisateur.getIdUser(),
                    "REUNION",
                    repondant.getPrenom() + " " + repondant.getNom() + " a " + action + " la réunion: " + participant.getReunion().getTitre(),
                    "reunion",
                    participant.getReunion().getIdReunion(),
                    buildReunionTargetRoute(participant.getReunion().getIdReunion())
            );
        }

        return participant;
    }

    @Transactional(readOnly = true)
    public List<ParticipantReunionDto> getParticipants(Long reunionId, Long requesterId) {
        Reunion reunion = getManagedReunion(reunionId);
        assertCanAccessReunion(reunion, requesterId);

        return participantReunionRepository.findByIdIdReunion(reunionId)
                .stream()
                .map(this::toParticipantDto)
                .sorted(Comparator
                        .comparing(ParticipantReunionDto::getIsOrganizer, Comparator.reverseOrder())
                        .thenComparing(ParticipantReunionDto::getPrenom, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ParticipantReunionDto::getNom, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public void removeParticipant(Long reunionId, Long targetUserId, Long requesterId) {
        Reunion reunion = getManagedReunion(reunionId);
        assertOrganizer(reunion, requesterId);

        if (reunion.getCreateur() != null && reunion.getCreateur().getIdUser().equals(targetUserId)) {
            throw new RuntimeException("L'organisateur ne peut pas être retiré de sa réunion");
        }

        ParticipantReunion participant = participantReunionRepository.findByIdIdReunionAndIdIdUser(reunionId, targetUserId)
                .orElseThrow(() -> new RuntimeException("Participant non trouvé pour cette réunion"));

        participantReunionRepository.delete(participant);
        notificationService.createNotification(
                targetUserId,
                "REUNION",
                "Vous avez été retiré(e) de la réunion: " + reunion.getTitre(),
                "reunion",
                reunion.getIdReunion(),
                buildReunionTargetRoute(reunion.getIdReunion())
        );
    }

    @Transactional
    public void sendReminder(Long reunionId, Long requesterId) {
        Reunion reunion = getManagedReunion(reunionId);
        assertOrganizer(reunion, requesterId);

        sendReminderNotification(reunion, false);
    }

    @Transactional
    public int sendPendingAutomaticReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.plusMinutes(THIRTY_MIN_REMINDER_MINUTES - THIRTY_MIN_REMINDER_TOLERANCE_MINUTES);
        LocalDateTime windowEnd = now.plusMinutes(DAY_REMINDER_MINUTES + DAY_REMINDER_TOLERANCE_MINUTES);
        List<Reunion> reunions = reunionRepository.findUpcomingForReminderWindow(windowStart, windowEnd);

        int sentCount = 0;
        for (Reunion reunion : reunions) {
            sentCount += maybeSendAutomaticReminder(reunion, now);
        }
        return sentCount;
    }

    private ReunionDto toDto(Reunion reunion, ParticipantReunion participation, Long userId) {
        Utilisateur createur = reunion.getCreateur();
        Long createurId = createur != null ? createur.getIdUser() : null;

        return ReunionDto.builder()
                .id(reunion.getIdReunion())
                .titre(reunion.getTitre())
                .description(reunion.getDescription())
                .datePrevue(reunion.getDatePrevue())
                .dureeMinutes(reunion.getDureeMinutes())
                .lieu(reunion.getLieu())
                .lienVisio(reunion.getLienVisio())
                .createurId(createurId)
                .organisateur(toUserSummary(createur))
                .myStatus(resolveUserStatus(participation, createurId, userId))
                .build();
    }

    private ReunionDto.UserSummaryDto toUserSummary(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        return ReunionDto.UserSummaryDto.builder()
                .id(utilisateur.getIdUser())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }

    private ParticipantReunionDto toParticipantDto(ParticipantReunion participant) {
        Utilisateur utilisateur = participant.getUtilisateur();
        String normalizedStatus = normalizeStatus(participant.getStatut());

        return ParticipantReunionDto.builder()
                .userId(utilisateur != null ? utilisateur.getIdUser() : participant.getId().getIdUser())
                .nom(utilisateur != null ? utilisateur.getNom() : null)
                .prenom(utilisateur != null ? utilisateur.getPrenom() : null)
                .email(utilisateur != null ? utilisateur.getEmail() : null)
                .role(utilisateur != null ? utilisateur.getRole() : null)
                .structureNom(utilisateur != null && utilisateur.getStructure() != null ? utilisateur.getStructure().getNom() : null)
                .statut(normalizedStatus)
                .isOrganizer("organisateur".equals(normalizedStatus))
                .build();
    }

    private Reunion getManagedReunion(Long reunionId) {
        return reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée: " + reunionId));
    }

    private ParticipantReunion buildParticipant(Reunion reunion, Utilisateur utilisateur, String statut) {
        return ParticipantReunion.builder()
                .id(new ParticipantReunionId(reunion.getIdReunion(), utilisateur.getIdUser()))
                .reunion(reunion)
                .utilisateur(utilisateur)
                .statut(statut)
                .build();
    }

    private LocalDateTime requireDatePrevue(LocalDateTime datePrevue) {
        if (datePrevue == null) {
            throw new RuntimeException("La date de réunion est requise");
        }
        return datePrevue;
    }

    private int requireDureeMinutes(Integer dureeMinutes) {
        int normalized = dureeMinutes == null ? 60 : dureeMinutes;
        if (normalized < 15) {
            throw new RuntimeException("La durée minimale est de 15 minutes");
        }
        if (normalized > 720) {
            throw new RuntimeException("La durée maximale est de 12 heures");
        }
        if (normalized % 15 != 0) {
            throw new RuntimeException("La durée doit être un multiple de 15 minutes");
        }
        return normalized;
    }

    private String normalizeNullableString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<Long> sanitizeParticipantIds(List<Long> participantIds, Long createurId) {
        if (participantIds == null) {
            return List.of();
        }

        return participantIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(id -> !id.equals(createurId))
                .toList();
    }

    private List<Long> buildAffectedUserIds(Long createurId, List<Long> participantIds) {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(createurId), participantIds.stream())
                .distinct()
                .toList();
    }

    private void assertNoScheduleConflict(LocalDateTime datePrevue, Integer dureeMinutes, Long excludedReunionId, List<Long> userIds) {
        LocalDateTime requestedEnd = datePrevue.plusMinutes(dureeMinutes == null ? 60 : dureeMinutes);

        for (Long userId : userIds) {
            Utilisateur utilisateur = utilisateurRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));

            boolean hasConflict = reunionRepository.findReunionsForUser(userId).stream()
                    .anyMatch(reunion ->
                            !reunion.getIdReunion().equals(excludedReunionId)
                                    && reunion.getDatePrevue() != null
                                    && overlaps(
                                    datePrevue,
                                    requestedEnd,
                                    reunion.getDatePrevue(),
                                    reunion.getDatePrevue().plusMinutes(reunion.getDureeMinutes() == null ? 60 : reunion.getDureeMinutes())
                            )
                    );

            if (hasConflict) {
                throw new RuntimeException(
                        "Conflit horaire: " + utilisateur.getPrenom() + " " + utilisateur.getNom()
                                + " a déjà une réunion qui chevauche le créneau du " + datePrevue.format(CONFLICT_FORMATTER) + "."
                );
            }
        }
    }

    private boolean overlaps(LocalDateTime startA, LocalDateTime endA, LocalDateTime startB, LocalDateTime endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    private void notifyParticipantsOfUpdate(Reunion reunion, Long requesterId) {
        String content = "La réunion \"" + reunion.getTitre() + "\" a été mise à jour. Nouveau créneau: " + formatMeetingDate(reunion) + ".";
        notifyActiveParticipants(reunion, requesterId, content);
    }

    private void notifyParticipantsOfCancellation(Reunion reunion, Long requesterId) {
        String content = "La réunion \"" + reunion.getTitre() + "\" prévue le " + formatMeetingDate(reunion) + " a été annulée.";
        notifyActiveParticipants(reunion, requesterId, content);
    }

    private void notifyActiveParticipants(Reunion reunion, Long requesterId, String content) {
        participantReunionRepository.findByIdIdReunion(reunion.getIdReunion())
                .stream()
                .filter(participant -> participant.getUtilisateur() != null)
                .filter(participant -> !Objects.equals(participant.getUtilisateur().getIdUser(), requesterId))
                .filter(participant -> !"refuse".equals(normalizeStatus(participant.getStatut())))
                .forEach(participant -> notificationService.createNotification(
                        participant.getUtilisateur().getIdUser(),
                        "REUNION",
                        content,
                        "reunion",
                        reunion.getIdReunion(),
                        buildReunionTargetRoute(reunion.getIdReunion())
                ));
    }

    private int maybeSendAutomaticReminder(Reunion reunion, LocalDateTime now) {
        if (reunion.getDatePrevue() == null) {
            return 0;
        }

        long minutesUntilMeeting = Duration.between(now, reunion.getDatePrevue()).toMinutes();
        boolean updated = false;

        if (isWithinReminderWindow(minutesUntilMeeting, DAY_REMINDER_MINUTES, DAY_REMINDER_TOLERANCE_MINUTES)
                && reunion.getRappelJ1EnvoyeAt() == null) {
            sendReminderNotification(reunion, true);
            reunion.setRappelJ1EnvoyeAt(now);
            updated = true;
        }

        if (isWithinReminderWindow(minutesUntilMeeting, HOUR_REMINDER_MINUTES, HOUR_REMINDER_TOLERANCE_MINUTES)
                && reunion.getRappel1hEnvoyeAt() == null) {
            sendReminderNotification(reunion, true);
            reunion.setRappel1hEnvoyeAt(now);
            updated = true;
        }

        if (isWithinReminderWindow(minutesUntilMeeting, THIRTY_MIN_REMINDER_MINUTES, THIRTY_MIN_REMINDER_TOLERANCE_MINUTES)
                && reunion.getRappel30mEnvoyeAt() == null) {
            sendReminderNotification(reunion, true);
            reunion.setRappel30mEnvoyeAt(now);
            updated = true;
        }

        if (updated) {
            reunionRepository.save(reunion);
            return 1;
        }

        return 0;
    }

    private boolean isWithinReminderWindow(long minutesUntilMeeting, long targetMinutes, long toleranceMinutes) {
        return minutesUntilMeeting >= (targetMinutes - toleranceMinutes)
                && minutesUntilMeeting <= (targetMinutes + toleranceMinutes);
    }

    private void sendReminderNotification(Reunion reunion, boolean automatic) {
        String prefix = automatic ? "Rappel automatique" : "Rappel";
        String content = prefix + ": réunion \"" + reunion.getTitre() + "\" prévue le " + formatMeetingDate(reunion) + ".";

        participantReunionRepository.findByIdIdReunion(reunion.getIdReunion())
                .stream()
                .filter(participant -> participant.getUtilisateur() != null)
                .filter(participant -> !"refuse".equals(normalizeStatus(participant.getStatut())))
                .map(participant -> participant.getUtilisateur().getIdUser())
                .distinct()
                .forEach(userId -> notificationService.createNotification(
                        userId,
                        "REUNION",
                        content,
                        "reunion",
                        reunion.getIdReunion(),
                        buildReunionTargetRoute(reunion.getIdReunion())
                ));
    }

    private void refreshAutomaticReminderFlags(Reunion reunion, LocalDateTime previousDatePrevue) {
        if (previousDatePrevue == null || Objects.equals(previousDatePrevue, reunion.getDatePrevue())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (shouldResetReminderFlag(reunion.getDatePrevue(), DAY_REMINDER_MINUTES, now)) {
            reunion.setRappelJ1EnvoyeAt(null);
        }
        if (shouldResetReminderFlag(reunion.getDatePrevue(), HOUR_REMINDER_MINUTES, now)) {
            reunion.setRappel1hEnvoyeAt(null);
        }
        if (shouldResetReminderFlag(reunion.getDatePrevue(), THIRTY_MIN_REMINDER_MINUTES, now)) {
            reunion.setRappel30mEnvoyeAt(null);
        }
    }

    private boolean shouldResetReminderFlag(LocalDateTime meetingDate, long targetMinutes, LocalDateTime now) {
        return meetingDate != null && meetingDate.minusMinutes(targetMinutes).isAfter(now);
    }

    private String buildReunionTargetRoute(Long reunionId) {
        return reunionId == null ? "/reunions" : "/reunions?focus=" + reunionId;
    }

    private String formatMeetingDate(Reunion reunion) {
        if (reunion == null || reunion.getDatePrevue() == null) {
            return "à venir";
        }
        return reunion.getDatePrevue().format(NOTIFICATION_FORMATTER);
    }

    private void assertOrganizer(Reunion reunion, Long requesterId) {
        Long organizerId = reunion.getCreateur() != null ? reunion.getCreateur().getIdUser() : null;
        if (organizerId == null || !organizerId.equals(requesterId)) {
            throw new SecurityException("Seul l'organisateur peut effectuer cette action");
        }
    }

    private void assertCanAccessReunion(Reunion reunion, Long requesterId) {
        Long reunionId = reunion.getIdReunion();
        Long organizerId = reunion.getCreateur() != null ? reunion.getCreateur().getIdUser() : null;
        if (organizerId != null && organizerId.equals(requesterId)) {
            return;
        }
        if (!participantReunionRepository.existsByIdIdReunionAndIdIdUser(reunionId, requesterId)) {
            throw new SecurityException("Accès non autorisé à cette réunion");
        }
    }

    private String resolveUserStatus(ParticipantReunion participation, Long createurId, Long userId) {
        if (createurId != null && createurId.equals(userId)) {
            return "organisateur";
        }

        if (participation == null) {
            return "en_attente";
        }

        return normalizeStatus(participation.getStatut());
    }

    private String normalizeEditableStatus(String statut) {
        String normalized = normalizeStatus(statut);
        if (!List.of("accepte", "refuse", "en_attente").contains(normalized)) {
            throw new RuntimeException("Statut de participation invalide");
        }
        return normalized;
    }

    private String normalizeStatus(String statut) {
        if (statut == null || statut.isBlank()) {
            return "en_attente";
        }

        return switch (statut.trim().toLowerCase(Locale.ROOT)) {
            case "organisateur" -> "organisateur";
            case "invite", "invité", "en attente", "en_attente" -> "en_attente";
            case "accepte", "accepté", "acceptee", "acceptée" -> "accepte";
            case "refuse", "refusé", "refusee", "refusée" -> "refuse";
            default -> statut.trim().toLowerCase(Locale.ROOT);
        };
    }
}
