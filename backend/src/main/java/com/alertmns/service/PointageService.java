package com.alertmns.service;

import com.alertmns.dto.PointageStatsDto;
import com.alertmns.dto.PointageExportRowDto;
import com.alertmns.dto.UserDto;
import com.alertmns.entity.Pointage;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.PointageRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

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

    @Transactional(readOnly = true)
    public List<PointageExportRowDto> getPointagesForAdmin(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        return pointageRepository.findForAdminExport(userId, start, end)
                .stream()
                .map(this::toExportRow)
                .toList();
    }

    @Transactional(readOnly = true)
    public String exportPointagesCsv(Long userId, LocalDate startDate, LocalDate endDate) {
        List<PointageExportRowDto> rows = getPointagesForAdmin(userId, startDate, endDate);
        StringBuilder sb = new StringBuilder();
        sb.append("pointage_id,user_id,prenom,nom,email,role,structure,date_debut,date_fin,duree_minutes,duree_formatee,en_cours\n");

        for (PointageExportRowDto row : rows) {
            sb.append(safeCsv(row.getPointageId())).append(',')
                    .append(safeCsv(row.getUserId())).append(',')
                    .append(safeCsv(row.getPrenom())).append(',')
                    .append(safeCsv(row.getNom())).append(',')
                    .append(safeCsv(row.getEmail())).append(',')
                    .append(safeCsv(row.getRole())).append(',')
                    .append(safeCsv(row.getStructureNom())).append(',')
                    .append(safeCsv(row.getDateDebut())).append(',')
                    .append(safeCsv(row.getDateFin())).append(',')
                    .append(safeCsv(row.getDureeMinutes())).append(',')
                    .append(safeCsv(row.getDureeFormatted())).append(',')
                    .append(safeCsv(row.getEnCours()))
                    .append('\n');
        }

        return sb.toString();
    }

    @Transactional(readOnly = true)
    public byte[] exportPointagesXlsx(Long userId, LocalDate startDate, LocalDate endDate) {
        List<PointageExportRowDto> rows = getPointagesForAdmin(userId, startDate, endDate);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Pointages");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            String[] headers = {
                    "Pointage ID", "User ID", "Prenom", "Nom", "Email", "Role", "Structure",
                    "Date debut", "Date fin", "Duree minutes", "Duree formattee", "En cours"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (PointageExportRowDto row : rows) {
                Row excelRow = sheet.createRow(rowIndex++);
                writeCell(excelRow, 0, row.getPointageId());
                writeCell(excelRow, 1, row.getUserId());
                writeCell(excelRow, 2, row.getPrenom());
                writeCell(excelRow, 3, row.getNom());
                writeCell(excelRow, 4, row.getEmail());
                writeCell(excelRow, 5, row.getRole());
                writeCell(excelRow, 6, row.getStructureNom());
                writeCell(excelRow, 7, row.getDateDebut(), dateStyle);
                writeCell(excelRow, 8, row.getDateFin(), dateStyle);
                writeCell(excelRow, 9, row.getDureeMinutes());
                writeCell(excelRow, 10, row.getDureeFormatted());
                writeCell(excelRow, 11, Boolean.TRUE.equals(row.getEnCours()) ? "Oui" : "Non");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de générer le fichier XLSX", e);
        }
    }

    private PointageExportRowDto toExportRow(Pointage pointage) {
        Utilisateur utilisateur = pointage.getUtilisateur();
        LocalDateTime end = pointage.getDateFin() != null ? pointage.getDateFin() : LocalDateTime.now();
        long dureeMinutes = java.time.Duration.between(pointage.getDateDebut(), end).toMinutes();

        return PointageExportRowDto.builder()
                .pointageId(pointage.getIdPointage())
                .userId(utilisateur != null ? utilisateur.getIdUser() : null)
                .prenom(utilisateur != null ? utilisateur.getPrenom() : null)
                .nom(utilisateur != null ? utilisateur.getNom() : null)
                .email(utilisateur != null ? utilisateur.getEmail() : null)
                .role(utilisateur != null ? utilisateur.getRole() : null)
                .structureNom(utilisateur != null && utilisateur.getStructure() != null ? utilisateur.getStructure().getNom() : null)
                .dateDebut(pointage.getDateDebut())
                .dateFin(pointage.getDateFin())
                .dureeMinutes(dureeMinutes)
                .dureeFormatted(PointageStatsDto.formatMinutes(dureeMinutes))
                .enCours(pointage.getDateFin() == null)
                .build();
    }

    private String safeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private void writeCell(Row row, int columnIndex, Object value) {
        writeCell(row, columnIndex, value, null);
    }

    private void writeCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (style != null) {
            cell.setCellStyle(style);
        }

        if (value == null) {
            cell.setBlank();
            return;
        }

        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }

        if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(java.sql.Timestamp.valueOf(dateTime));
            return;
        }

        cell.setCellValue(String.valueOf(value));
    }
}
