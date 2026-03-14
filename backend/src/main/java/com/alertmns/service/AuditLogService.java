package com.alertmns.service;

import com.alertmns.dto.AuditLogDto;
import com.alertmns.entity.AuditLog;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.AuditLogRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public void logAction(Long actorId, String action, String targetType, Long targetId, String details) {
        Utilisateur actor = null;
        if (actorId != null) {
            actor = utilisateurRepository.findById(actorId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + actorId));
        }

        AuditLog entry = AuditLog.builder()
                .actor(actor)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getAuditLogs(Long actorId, String action, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        return auditLogRepository.findForAudit(actorId, normalizeBlank(action), start, end)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AuditLogDto toDto(AuditLog entry) {
        Utilisateur actor = entry.getActor();
        return AuditLogDto.builder()
                .id(entry.getIdAuditLog())
                .action(entry.getAction())
                .targetType(entry.getTargetType())
                .targetId(entry.getTargetId())
                .details(entry.getDetails())
                .createdAt(entry.getCreatedAt())
                .actorId(actor != null ? actor.getIdUser() : null)
                .actorPrenom(actor != null ? actor.getPrenom() : null)
                .actorNom(actor != null ? actor.getNom() : null)
                .actorEmail(actor != null ? actor.getEmail() : null)
                .actorRole(actor != null ? actor.getRole() : null)
                .build();
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
