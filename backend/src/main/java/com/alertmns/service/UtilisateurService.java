package com.alertmns.service;

import com.alertmns.dto.UserDto;
import com.alertmns.entity.Structure;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.PointageRepository;
import com.alertmns.repository.StructureRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final StructureRepository structureRepository;
    private final PointageRepository pointageRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<Long> presentIds = pointageRepository.findPresentUserIds();
        Set<Long> presentSet = Set.copyOf(presentIds);
        return utilisateurRepository.findAll().stream()
                .map(u -> toDto(u, presentSet.contains(u.getIdUser())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getActiveUsers() {
        List<Long> presentIds = pointageRepository.findPresentUserIds();
        Set<Long> presentSet = Set.copyOf(presentIds);
        return utilisateurRepository.findByIsActiveTrue().stream()
                .map(u -> toDto(u, presentSet.contains(u.getIdUser())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        List<Long> presentIds = pointageRepository.findPresentUserIds();
        return toDto(u, presentIds.contains(u.getIdUser()));
    }

    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));
        List<Long> presentIds = pointageRepository.findPresentUserIds();
        return toDto(u, presentIds.contains(u.getIdUser()));
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));

        if (userDto.getNom() != null) utilisateur.setNom(userDto.getNom());
        if (userDto.getPrenom() != null) utilisateur.setPrenom(userDto.getPrenom());
        if (userDto.getEmail() != null) utilisateur.setEmail(userDto.getEmail());
        if (userDto.getRole() != null) utilisateur.setRole(userDto.getRole());
        if (userDto.getMsgAbsence() != null) utilisateur.setMsgAbsence(userDto.getMsgAbsence());

        if (userDto.getStructureId() != null) {
            Structure structure = structureRepository.findById(userDto.getStructureId())
                    .orElseThrow(() -> new RuntimeException("Structure non trouvée"));
            utilisateur.setStructure(structure);
        }

        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, false);
    }

    @Transactional
    public void deleteUser(Long id) {
        utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateurRepository.deleteById(id);
    }

    @Transactional
    public UserDto activateUser(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateur.setIsActive(true);
        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, false);
    }

    @Transactional
    public UserDto deactivateUser(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateur.setIsActive(false);
        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, false);
    }

    @Transactional
    public UserDto updateAbsenceMessage(Long id, String message) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateur.setMsgAbsence(message);
        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, false);
    }

    @Transactional
    public UserDto updatePassword(Long id, String newPassword) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateur.setMdp(passwordEncoder.encode(newPassword));
        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, false);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getPresentUsers() {
        List<Long> presentIds = pointageRepository.findPresentUserIds();
        return presentIds.stream()
                .map(id -> utilisateurRepository.findById(id).orElse(null))
                .filter(u -> u != null && Boolean.TRUE.equals(u.getIsActive()))
                .map(u -> toDto(u, true))
                .collect(Collectors.toList());
    }

    public UserDto toDto(Utilisateur u, boolean isPresent) {
        return UserDto.builder()
                .idUser(u.getIdUser())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .role(u.getRole())
                .msgAbsence(u.getMsgAbsence())
                .isActive(u.getIsActive())
                .structureId(u.getStructure() != null ? u.getStructure().getIdStructure() : null)
                .structureNom(u.getStructure() != null ? u.getStructure().getNom() : null)
                .structureType(u.getStructure() != null ? u.getStructure().getTypeStructure() : null)
                .isPresent(isPresent)
                .build();
    }
}
