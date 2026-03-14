package com.alertmns.service;

import com.alertmns.dto.StructureRequest;
import com.alertmns.entity.Structure;
import com.alertmns.repository.StructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StructureService {

    private final StructureRepository structureRepository;

    @Transactional(readOnly = true)
    public List<Structure> getAllStructures() {
        return structureRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Structure::getNom, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public Structure createStructure(StructureRequest request) {
        String nom = normalizeRequiredValue(request.getNom(), "Le nom est obligatoire");
        if (structureRepository.existsByNomIgnoreCase(nom)) {
            throw new RuntimeException("Une structure avec ce nom existe déjà");
        }

        Structure structure = Structure.builder()
                .nom(nom)
                .typeStructure(normalizeNullableValue(request.getTypeStructure()))
                .build();

        return structureRepository.save(structure);
    }

    @Transactional
    public Structure updateStructure(Long id, StructureRequest request) {
        Structure structure = structureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        String nom = normalizeRequiredValue(request.getNom(), "Le nom est obligatoire");
        structureRepository.findByNomIgnoreCase(nom)
                .filter(existing -> !existing.getIdStructure().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Une structure avec ce nom existe déjà");
                });

        structure.setNom(nom);
        structure.setTypeStructure(normalizeNullableValue(request.getTypeStructure()));
        return structureRepository.save(structure);
    }

    @Transactional
    public void deleteStructure(Long id) {
        Structure structure = structureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));
        structureRepository.delete(structure);
    }

    private String normalizeRequiredValue(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(errorMessage);
        }
        return value.trim();
    }

    private String normalizeNullableValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
