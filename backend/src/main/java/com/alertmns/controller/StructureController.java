package com.alertmns.controller;

import com.alertmns.dto.StructureRequest;
import com.alertmns.entity.Structure;
import com.alertmns.service.AuditLogService;
import com.alertmns.service.StructureService;
import com.alertmns.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/structures")
@RequiredArgsConstructor
public class StructureController {

    private final StructureService structureService;
    private final UtilisateurService utilisateurService;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('Admin') or hasRole('RH')")
    public ResponseEntity<List<Structure>> getAllStructures() {
        return ResponseEntity.ok(structureService.getAllStructures());
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> createStructure(@Valid @RequestBody StructureRequest request, Authentication authentication) {
        try {
            Structure structure = structureService.createStructure(request);
            Long requesterId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            auditLogService.logAction(
                    requesterId,
                    "STRUCTURE_CREATE",
                    "structure",
                    structure.getIdStructure(),
                    "Création de la structure " + structure.getNom()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(structure);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> updateStructure(@PathVariable Long id, @Valid @RequestBody StructureRequest request, Authentication authentication) {
        try {
            Structure structure = structureService.updateStructure(id, request);
            Long requesterId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            auditLogService.logAction(
                    requesterId,
                    "STRUCTURE_UPDATE",
                    "structure",
                    structure.getIdStructure(),
                    "Mise à jour de la structure " + structure.getNom()
            );
            return ResponseEntity.ok(structure);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> deleteStructure(@PathVariable Long id, Authentication authentication) {
        try {
            structureService.deleteStructure(id);
            Long requesterId = utilisateurService.getUserByEmail(authentication.getName()).getIdUser();
            auditLogService.logAction(
                    requesterId,
                    "STRUCTURE_DELETE",
                    "structure",
                    id,
                    "Suppression de la structure #" + id
            );
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
