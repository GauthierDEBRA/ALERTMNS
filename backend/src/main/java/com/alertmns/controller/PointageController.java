package com.alertmns.controller;

import com.alertmns.dto.PointageStatsDto;
import com.alertmns.dto.PointageExportRowDto;
import com.alertmns.dto.UserDto;
import com.alertmns.entity.Pointage;
import com.alertmns.service.AuditLogService;
import com.alertmns.service.PointageService;
import com.alertmns.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pointage")
@RequiredArgsConstructor
public class PointageController {

    private final PointageService pointageService;
    private final UtilisateurService utilisateurService;
    private final AuditLogService auditLogService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/arriver")
    public ResponseEntity<?> pointerArrivee(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            Pointage pointage = pointageService.pointerArrivee(userId);
            broadcastPresence();
            return ResponseEntity.status(HttpStatus.CREATED).body(pointage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/partir")
    public ResponseEntity<?> pointerDepart(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            Pointage pointage = pointageService.pointerDepart(userId);
            broadcastPresence();
            return ResponseEntity.ok(pointage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private void broadcastPresence() {
        List<UserDto> presentUsers = pointageService.getPresentUsers();
        messagingTemplate.convertAndSend("/topic/presence", presentUsers);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            PointageStatsDto stats = pointageService.getStats(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/stats/{userId}")
    @PreAuthorize("hasRole('Admin') or hasRole('RH') or hasRole('Responsable')")
    public ResponseEntity<?> getStatsForUser(@PathVariable Long userId) {
        try {
            PointageStatsDto stats = pointageService.getStats(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/presents")
    public ResponseEntity<List<UserDto>> getPresentUsers() {
        return ResponseEntity.ok(pointageService.getPresentUsers());
    }

    @GetMapping("/historique")
    public ResponseEntity<?> getHistorique(Authentication authentication) {
        try {
            Long userId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            List<Pointage> pointages = pointageService.getPointagesForUser(userId);
            return ResponseEntity.ok(pointages);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/historique/{userId}")
    @PreAuthorize("hasRole('Admin') or hasRole('RH') or hasRole('Responsable')")
    public ResponseEntity<?> getHistoriqueForUser(@PathVariable Long userId) {
        try {
            List<Pointage> pointages = pointageService.getPointagesForUser(userId);
            return ResponseEntity.ok(pointages);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/admin/report")
    @PreAuthorize("hasRole('Admin') or hasRole('RH')")
    public ResponseEntity<?> getAdminReport(@RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) String start,
                                            @RequestParam(required = false) String end,
                                            Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            LocalDate startDate = parseDate(start);
            LocalDate endDate = parseDate(end);
            List<PointageExportRowDto> rows = pointageService.getPointagesForAdmin(userId, startDate, endDate);
            auditLogService.logAction(
                    requester.getIdUser(),
                    "POINTAGE_REPORT_VIEW",
                    "pointage",
                    userId,
                    "Consultation du rapport pointage" + buildDateFilterSuffix(startDate, endDate)
            );
            return ResponseEntity.ok(rows);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/admin/export/csv")
    @PreAuthorize("hasRole('Admin') or hasRole('RH')")
    public ResponseEntity<?> exportAdminReportCsv(@RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) String start,
                                                  @RequestParam(required = false) String end,
                                                  Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            LocalDate startDate = parseDate(start);
            LocalDate endDate = parseDate(end);
            String csv = pointageService.exportPointagesCsv(userId, startDate, endDate);
            auditLogService.logAction(
                    requester.getIdUser(),
                    "POINTAGE_EXPORT_CSV",
                    "pointage",
                    userId,
                    "Export CSV des pointages" + buildDateFilterSuffix(startDate, endDate)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "pointages-export.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csv);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/admin/export/xlsx")
    @PreAuthorize("hasRole('Admin') or hasRole('RH')")
    public ResponseEntity<?> exportAdminReportXlsx(@RequestParam(required = false) Long userId,
                                                   @RequestParam(required = false) String start,
                                                   @RequestParam(required = false) String end,
                                                   Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            LocalDate startDate = parseDate(start);
            LocalDate endDate = parseDate(end);
            byte[] xlsx = pointageService.exportPointagesXlsx(userId, startDate, endDate);
            auditLogService.logAction(
                    requester.getIdUser(),
                    "POINTAGE_EXPORT_XLSX",
                    "pointage",
                    userId,
                    "Export XLSX des pointages" + buildDateFilterSuffix(startDate, endDate)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "pointages-export.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(xlsx);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/admin/export/pdf")
    @PreAuthorize("hasRole('Admin') or hasRole('RH')")
    public ResponseEntity<?> exportAdminReportPdf(@RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) String start,
                                                  @RequestParam(required = false) String end,
                                                  Authentication authentication) {
        try {
            UserDto requester = utilisateurService.getUserByEmail(authentication.getName());
            LocalDate startDate = parseDate(start);
            LocalDate endDate = parseDate(end);
            byte[] pdf = pointageService.exportPointagesPdf(userId, startDate, endDate);
            auditLogService.logAction(
                    requester.getIdUser(),
                    "POINTAGE_EXPORT_PDF",
                    "pointage",
                    userId,
                    "Export PDF des pointages" + buildDateFilterSuffix(startDate, endDate)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "pointages-export.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private String buildDateFilterSuffix(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return "";
        }
        return " (du " + (startDate != null ? startDate : "...") + " au " + (endDate != null ? endDate : "...") + ")";
    }
}
