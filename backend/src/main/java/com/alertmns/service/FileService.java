package com.alertmns.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ATTACHMENT_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp",
            "pdf",
            "txt", "csv",
            "doc", "docx",
            "xls", "xlsx",
            "ppt", "pptx",
            "odt", "ods"
    );
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            "exe", "msi", "bat", "cmd", "sh", "bash", "zsh", "ps1", "com", "scr",
            "jar", "war", "class", "dll", "so", "dylib",
            "html", "htm", "js", "mjs", "svg", "xml", "xhtml", "php", "jsp"
    );

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadAttachment(MultipartFile file) {
        ValidatedFile validated = validateFile(file, UploadCategory.ATTACHMENT);
        return storeFile(file, validated.extension());
    }

    public String uploadAvatar(MultipartFile file) {
        if (file != null && file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new RuntimeException("Image trop volumineuse. Maximum 5 Mo.");
        }
        ValidatedFile validated = validateFile(file, UploadCategory.AVATAR);
        return storeFile(file, validated.extension());
    }

    public void deleteFile(String fileUrl) {
        try {
            String filename = fileUrl.replace("/uploads/", "");
            Path filePath = Paths.get(uploadDir, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }

    private ValidatedFile validateFile(MultipartFile file, UploadCategory category) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Fichier vide ou nul");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (!StringUtils.hasText(originalFilename) || originalFilename.contains("..")) {
            throw new RuntimeException("Nom de fichier invalide: " + originalFilename);
        }

        String extension = extractExtension(originalFilename);
        if (extension.isBlank()) {
            throw new RuntimeException("Extension de fichier manquante");
        }
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Type de fichier non autorisé");
        }

        Set<String> allowedExtensions = category == UploadCategory.AVATAR ? IMAGE_EXTENSIONS : ATTACHMENT_EXTENSIONS;
        if (!allowedExtensions.contains(extension)) {
            throw new RuntimeException("Extension de fichier non autorisée");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de lire le fichier uploadé");
        }

        FileKind detectedKind = detectKind(bytes);
        if (detectedKind == FileKind.EXECUTABLE || detectedKind == FileKind.ELF || detectedKind == FileKind.MACH_O) {
            throw new RuntimeException("Fichier exécutable interdit");
        }

        if (category == UploadCategory.AVATAR) {
            if (!isMatchingAvatar(extension, detectedKind)) {
                throw new RuntimeException("Le fichier ne correspond pas à une image valide");
            }
        } else if (!isMatchingAttachment(extension, detectedKind, bytes)) {
            throw new RuntimeException("Le type réel du fichier ne correspond pas à son extension");
        }

        return new ValidatedFile(originalFilename, extension, detectedKind);
    }

    private String storeFile(MultipartFile file, String extension) {
        String uniqueFilename = UUID.randomUUID() + "." + extension;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetPath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier: " + e.getMessage());
        }
    }

    private boolean isMatchingAvatar(String extension, FileKind detectedKind) {
        return switch (extension) {
            case "jpg", "jpeg" -> detectedKind == FileKind.JPEG;
            case "png" -> detectedKind == FileKind.PNG;
            case "gif" -> detectedKind == FileKind.GIF;
            case "webp" -> detectedKind == FileKind.WEBP;
            default -> false;
        };
    }

    private boolean isMatchingAttachment(String extension, FileKind detectedKind, byte[] bytes) {
        return switch (extension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> isMatchingAvatar(extension, detectedKind);
            case "pdf" -> detectedKind == FileKind.PDF;
            case "doc", "xls", "ppt" -> detectedKind == FileKind.OLE2;
            case "docx", "xlsx", "pptx", "odt", "ods" -> detectedKind == FileKind.ZIP;
            case "txt", "csv" -> detectedKind == FileKind.TEXT || detectedKind == FileKind.UNKNOWN && looksLikeText(bytes);
            default -> false;
        };
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private FileKind detectKind(byte[] bytes) {
        if (startsWith(bytes, (byte) 0xFF, (byte) 0xD8, (byte) 0xFF)) return FileKind.JPEG;
        if (startsWith(bytes, (byte) 0x89, 'P', 'N', 'G', (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A)) return FileKind.PNG;
        if (startsWithAscii(bytes, "GIF87a") || startsWithAscii(bytes, "GIF89a")) return FileKind.GIF;
        if (startsWithAscii(bytes, "RIFF") && containsAsciiAt(bytes, 8, "WEBP")) return FileKind.WEBP;
        if (startsWithAscii(bytes, "%PDF-")) return FileKind.PDF;
        if (startsWith(bytes, 'P', 'K', 0x03, 0x04) || startsWith(bytes, 'P', 'K', 0x05, 0x06) || startsWith(bytes, 'P', 'K', 0x07, 0x08)) return FileKind.ZIP;
        if (startsWith(bytes, (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1)) return FileKind.OLE2;
        if (startsWithAscii(bytes, "MZ")) return FileKind.EXECUTABLE;
        if (startsWith(bytes, 0x7F, 'E', 'L', 'F')) return FileKind.ELF;
        if (startsWith(bytes, (byte) 0xFE, (byte) 0xED, (byte) 0xFA, (byte) 0xCE)
                || startsWith(bytes, (byte) 0xFE, (byte) 0xED, (byte) 0xFA, (byte) 0xCF)
                || startsWith(bytes, (byte) 0xCF, (byte) 0xFA, (byte) 0xED, (byte) 0xFE)
                || startsWith(bytes, (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE)) {
            return FileKind.MACH_O;
        }
        if (looksLikeText(bytes)) return FileKind.TEXT;
        return FileKind.UNKNOWN;
    }

    private boolean looksLikeText(byte[] bytes) {
        int length = Math.min(bytes.length, 512);
        if (length == 0) {
            return false;
        }

        int printable = 0;
        for (int i = 0; i < length; i++) {
            int value = bytes[i] & 0xFF;
            if (value == 9 || value == 10 || value == 13 || (value >= 32 && value <= 126)) {
                printable++;
            }
        }

        if (printable < length * 0.9) {
            return false;
        }

        String sample = new String(bytes, 0, Math.min(bytes.length, 256), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        return !sample.contains("<svg") && !sample.contains("<html") && !sample.contains("<script");
    }

    private boolean startsWithAscii(byte[] bytes, String value) {
        return startsWith(bytes, value.getBytes(StandardCharsets.US_ASCII));
    }

    private boolean containsAsciiAt(byte[] bytes, int offset, String value) {
        byte[] expected = value.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (bytes[offset + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean startsWith(byte[] bytes, int... values) {
        if (bytes.length < values.length) {
            return false;
        }
        for (int i = 0; i < values.length; i++) {
            if ((bytes[i] & 0xFF) != (values[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }

    private boolean startsWith(byte[] bytes, byte... values) {
        if (bytes.length < values.length) {
            return false;
        }
        for (int i = 0; i < values.length; i++) {
            if (bytes[i] != values[i]) {
                return false;
            }
        }
        return true;
    }

    private record ValidatedFile(String originalFilename, String extension, FileKind detectedKind) {
    }

    private enum UploadCategory {
        AVATAR,
        ATTACHMENT
    }

    private enum FileKind {
        JPEG,
        PNG,
        GIF,
        WEBP,
        PDF,
        ZIP,
        OLE2,
        TEXT,
        EXECUTABLE,
        ELF,
        MACH_O,
        UNKNOWN
    }
}
