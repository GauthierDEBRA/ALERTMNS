package com.alertmns.service;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReunionService {

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
    public Reunion getReunionById(Long id) {
        return reunionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée: " + id));
    }

    @Transactional
    public Reunion createReunion(String titre, String description, LocalDateTime datePrevue, Long createurId) {
        Utilisateur createur = utilisateurRepository.findById(createurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + createurId));

        Reunion reunion = Reunion.builder()
                .titre(titre)
                .description(description)
                .datePrevue(datePrevue)
                .createur(createur)
                .build();

        reunion = reunionRepository.save(reunion);

        // Add creator as participant
        ParticipantReunion participant = ParticipantReunion.builder()
                .id(new ParticipantReunionId(reunion.getIdReunion(), createurId))
                .reunion(reunion)
                .utilisateur(createur)
                .statut("Organisateur")
                .build();
        participantReunionRepository.save(participant);

        return reunion;
    }

    @Transactional
    public Reunion updateReunion(Long id, String titre, String description, LocalDateTime datePrevue) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée: " + id));
        if (titre != null) reunion.setTitre(titre);
        if (description != null) reunion.setDescription(description);
        if (datePrevue != null) reunion.setDatePrevue(datePrevue);
        return reunionRepository.save(reunion);
    }

    @Transactional
    public void deleteReunion(Long id) {
        reunionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée: " + id));
        reunionRepository.deleteById(id);
    }

    @Transactional
    public ParticipantReunion inviteParticipant(Long reunionId, Long userId) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée: " + reunionId));
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));

        ParticipantReunionId participantId = new ParticipantReunionId(reunionId, userId);
        if (participantReunionRepository.existsById(participantId)) {
            throw new RuntimeException("L'utilisateur est déjà invité à cette réunion");
        }

        ParticipantReunion participant = ParticipantReunion.builder()
                .id(participantId)
                .reunion(reunion)
                .utilisateur(utilisateur)
                .statut("Invité")
                .build();

        participant = participantReunionRepository.save(participant);

        // Send notification
        notificationService.createNotification(
                userId,
                "REUNION",
                "Vous avez été invité à la réunion: " + reunion.getTitre()
        );

        return participant;
    }

    @Transactional
    public ParticipantReunion updateStatutParticipation(Long reunionId, Long userId, String statut) {
        ParticipantReunion participant = participantReunionRepository
                .findByIdIdReunionAndIdIdUser(reunionId, userId)
                .orElseThrow(() -> new RuntimeException("Participation non trouvée"));
        participant.setStatut(statut);
        return participantReunionRepository.save(participant);
    }

    @Transactional(readOnly = true)
    public List<ParticipantReunion> getParticipants(Long reunionId) {
        return participantReunionRepository.findByIdIdReunion(reunionId);
    }
}
