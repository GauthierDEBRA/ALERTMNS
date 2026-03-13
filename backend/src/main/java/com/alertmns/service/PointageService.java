package com.alertmns.service;

import com.alertmns.dto.PointageStatsDto;
import com.alertmns.dto.UserDto;
import com.alertmns.entity.Pointage;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.PointageRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointageService {

    private final PointageRepository pointageRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;

    @Transactional
    public Pointage pointerArrivee(Long userId) {
        utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));

        pointageRepository.findOpenPointage(userId).ifPresent(p -> {
            throw new RuntimeException("Un pointage est déjà ouvert pour cet utilisateur");
        });

        Utilisateur utilisateur = utilisateurRepository.findById(userId).get();

        Pointage pointage = Pointage.builder()
                .dateDebut(LocalDateTime.now())
                .utilisateur(utilisateur)
                .build();

        return pointageRepository.save(pointage);
    }

    @Transactional
    public Pointage pointerDepart(Long userId) {
        Pointage pointage = pointageRepository.findOpenPointage(userId)
                .orElseThrow(() -> new RuntimeException("Aucun pointage ouvert trouvé pour cet utilisateur"));

        pointage.setDateFin(LocalDateTime.now());
        return pointageRepository.save(pointage);
    }

    @Transactional(readOnly = true)
    public PointageStatsDto getStats(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // Daily: today
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.atTime(LocalTime.MAX);

        // Weekly: Monday to Sunday of current week
        LocalDateTime weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);

        // Monthly: first to last day of current month
        LocalDateTime monthStart = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime monthEnd = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        long dailyMinutes = calculateMinutes(userId, dayStart, dayEnd);
        long weeklyMinutes = calculateMinutes(userId, weekStart, weekEnd);
        long monthlyMinutes = calculateMinutes(userId, monthStart, monthEnd);

        boolean isPresent = pointageRepository.findOpenPointage(userId).isPresent();
        String arrivedAt = null;
        if (isPresent) {
            arrivedAt = pointageRepository.findOpenPointage(userId)
                    .map(p -> p.getDateDebut().toString())
                    .orElse(null);
        }

        return PointageStatsDto.builder()
                .dailyMinutes(dailyMinutes)
                .weeklyMinutes(weeklyMinutes)
                .monthlyMinutes(monthlyMinutes)
                .dailyFormatted(PointageStatsDto.formatMinutes(dailyMinutes))
                .weeklyFormatted(PointageStatsDto.formatMinutes(weeklyMinutes))
                .monthlyFormatted(PointageStatsDto.formatMinutes(monthlyMinutes))
                .isCurrentlyPresent(isPresent)
                .arrivedAt(arrivedAt)
                .build();
    }

    private long calculateMinutes(Long userId, LocalDateTime start, LocalDateTime end) {
        List<Pointage> pointages = pointageRepository.findByUserAndDateRange(userId, start, end);
        long totalMinutes = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Pointage p : pointages) {
            LocalDateTime debut = p.getDateDebut();
            LocalDateTime fin = p.getDateFin() != null ? p.getDateFin() : now;
            if (debut.isBefore(start)) debut = start;
            if (fin.isAfter(end)) fin = end;
            if (fin.isAfter(debut)) {
                totalMinutes += java.time.Duration.between(debut, fin).toMinutes();
            }
        }
        return totalMinutes;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getPresentUsers() {
        return utilisateurService.getPresentUsers();
    }

    @Transactional(readOnly = true)
    public List<Pointage> getPointagesForUser(Long userId) {
        return pointageRepository.findByIdUserOrderByDateDebut(userId);
    }

    @Transactional(readOnly = true)
    public boolean isPresent(Long userId) {
        return pointageRepository.findOpenPointage(userId).isPresent();
    }
}
