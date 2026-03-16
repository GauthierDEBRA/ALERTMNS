package com.alertmns.service;

import com.alertmns.dto.MessageDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.alertmns.repository.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MessageExportTest {

    @Mock MessageRepository messageRepository;
    @Mock CanalRepository canalRepository;
    @Mock UtilisateurRepository utilisateurRepository;
    @Mock MembreCanalRepository membreCanalRepository;
    @Mock LectureEtatRepository lectureEtatRepository;
    @Mock PieceJointeRepository pieceJointeRepository;
    @Mock ReactionMessageRepository reactionMessageRepository;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock NotificationService notificationService;

    @InjectMocks MessageService messageService;

    private List<MessageDto> sampleMessages;

    @BeforeEach
    void setUp() {
        sampleMessages = List.of(
            MessageDto.builder()
                .idMessage(1L)
                .contenu("Bonjour tout le monde !")
                .dateEnvoi(LocalDateTime.of(2025, 1, 15, 10, 30))
                .userPrenom("Alice")
                .userNom("Dupont")
                .build(),
            MessageDto.builder()
                .idMessage(2L)
                .contenu("Réunion à 14h")
                .dateEnvoi(LocalDateTime.of(2025, 1, 15, 11, 0))
                .userPrenom("Bob")
                .userNom("Martin")
                .build()
        );
    }

    // --- XLSX ---

    @Test
    void exportAsXlsx_producesByteArray() throws Exception {
        byte[] result = invokeExportAsXlsx(sampleMessages);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void exportAsXlsx_containsHeaderRow() throws Exception {
        byte[] result = invokeExportAsXlsx(sampleMessages);
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = wb.getSheet("Conversation");
            assertNotNull(sheet, "Sheet 'Conversation' doit exister");
            Row header = sheet.getRow(0);
            assertNotNull(header, "La ligne d'en-tête doit exister");
            assertEquals("ID", header.getCell(0).getStringCellValue());
            assertEquals("Date", header.getCell(1).getStringCellValue());
            assertEquals("Auteur", header.getCell(2).getStringCellValue());
            assertEquals("Message", header.getCell(3).getStringCellValue());
        }
    }

    @Test
    void exportAsXlsx_containsAllMessages() throws Exception {
        byte[] result = invokeExportAsXlsx(sampleMessages);
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = wb.getSheet("Conversation");
            // Row 0 = header, rows 1..n = messages
            assertEquals(sampleMessages.size() + 1, sheet.getPhysicalNumberOfRows());
            Row row1 = sheet.getRow(1);
            assertEquals("Alice Dupont", row1.getCell(2).getStringCellValue());
            assertEquals("Bonjour tout le monde !", row1.getCell(3).getStringCellValue());
        }
    }

    @Test
    void exportAsXlsx_withEmptyList_producesHeaderOnly() throws Exception {
        byte[] result = invokeExportAsXlsx(List.of());
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = wb.getSheet("Conversation");
            assertEquals(1, sheet.getPhysicalNumberOfRows());
        }
    }

    // --- PDF ---

    @Test
    void exportAsPdf_producesByteArray() throws Exception {
        byte[] result = invokeExportAsPdf(sampleMessages, 42L);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void exportAsPdf_startsWith_PdfMagicBytes() throws Exception {
        byte[] result = invokeExportAsPdf(sampleMessages, 42L);
        // PDF files start with %PDF
        String header = new String(result, 0, 4);
        assertEquals("%PDF", header);
    }

    @Test
    void exportAsPdf_withEmptyList_stillProducesValidPdf() throws Exception {
        byte[] result = invokeExportAsPdf(List.of(), 1L);
        assertTrue(result.length > 100, "Un PDF vide doit tout de même avoir une taille minimale");
        assertEquals("%PDF", new String(result, 0, 4));
    }

    // --- helpers reflectifs (méthodes privées) ---

    private byte[] invokeExportAsXlsx(List<MessageDto> messages) throws Exception {
        Method m = MessageService.class.getDeclaredMethod("exportAsXlsx", List.class);
        m.setAccessible(true);
        return (byte[]) m.invoke(messageService, messages);
    }

    private byte[] invokeExportAsPdf(List<MessageDto> messages, Long canalId) throws Exception {
        Method m = MessageService.class.getDeclaredMethod("exportAsPdf", List.class, Long.class);
        m.setAccessible(true);
        return (byte[]) m.invoke(messageService, messages, canalId);
    }
}
