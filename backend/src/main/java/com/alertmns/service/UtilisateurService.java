package com.alertmns.service;

import com.alertmns.dto.CreateUserRequest;
import com.alertmns.dto.ProfilePreferencesRequest;
import com.alertmns.dto.ProfileUpdateRequest;
import com.alertmns.dto.UserDto;
import com.alertmns.entity.Structure;
import com.alertmns.entity.Utilisateur;
import com.alertmns.service.FileService;
import com.alertmns.repository.PointageRepository;
import com.alertmns.repository.StructureRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private static final Set<String> ALLOWED_ROLES = Set.of("Admin", "RH", "Responsable", "Collaborateur");

    private final UtilisateurRepository utilisateurRepository;
    private final StructureRepository structureRepository;
    private final PointageRepository pointageRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

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
    public UserDto createUser(CreateUserRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        Structure structure = resolveStructure(request.getStructureId());
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(normalizeRequiredValue(request.getNom(), "Le nom est obligatoire"))
                .prenom(normalizeRequiredValue(request.getPrenom(), "Le prénom est obligatoire"))
                .email(email)
                .mdp(passwordEncoder.encode(request.getPassword()))
                .role(normalizeRole(request.getRole()))
                .isActive(true)
                .structure(structure)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, false);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));

        if (userDto.getNom() != null) utilisateur.setNom(normalizeRequiredValue(userDto.getNom(), "Le nom est obligatoire"));
        if (userDto.getPrenom() != null) utilisateur.setPrenom(normalizeRequiredValue(userDto.getPrenom(), "Le prénom est obligatoire"));
        if (userDto.getEmail() != null) {
            String email = normalizeEmail(userDto.getEmail());
            if (!email.equalsIgnoreCase(utilisateur.getEmail()) && utilisateurRepository.existsByEmail(email)) {
                throw new RuntimeException("Un utilisateur avec cet email existe déjà");
            }
            utilisateur.setEmail(email);
        }
        if (userDto.getRole() != null) utilisateur.setRole(normalizeRole(userDto.getRole()));
        if (userDto.getMsgAbsence() != null) utilisateur.setMsgAbsence(userDto.getMsgAbsence());

        utilisateur.setStructure(resolveStructure(userDto.getStructureId()));

        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, false);
    }

    @Transactional
    public UserDto updateOwnProfile(Long id, ProfileUpdateRequest request, Long requesterId, boolean isAdmin) {
        assertSelfOrAdmin(id, requesterId, isAdmin, "Vous ne pouvez modifier que votre propre profil");
        Utilisateur utilisateur = getManagedUser(id);

        utilisateur.setPrenom(normalizeRequiredValue(request.getPrenom(), "Le prénom est obligatoire"));
        utilisateur.setNom(normalizeRequiredValue(request.getNom(), "Le nom est obligatoire"));

        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, isUserPresent(utilisateur.getIdUser()));
    }

    @Transactional
    public UserDto updateNotificationPreferences(Long id,
                                                 ProfilePreferencesRequest request,
                                                 Long requesterId,
                                                 boolean isAdmin) {
        assertSelfOrAdmin(id, requesterId, isAdmin, "Vous ne pouvez modifier que vos propres préférences");
        Utilisateur utilisateur = getManagedUser(id);

        utilisateur.setNotifyReunions(request.getNotifyReunions() != null ? request.getNotifyReunions() : Boolean.TRUE);
        utilisateur.setNotifyMessages(request.getNotifyMessages() != null ? request.getNotifyMessages() : Boolean.TRUE);
        utilisateur.setNotifyAbsences(request.getNotifyAbsences() != null ? request.getNotifyAbsences() : Boolean.TRUE);

        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, isUserPresent(utilisateur.getIdUser()));
    }

    @Transactional
    public UserDto updateAvatar(Long id, String avatarUrl, Long requesterId, boolean isAdmin) {
        assertSelfOrAdmin(id, requesterId, isAdmin, "Vous ne pouvez modifier que votre propre avatar");
        Utilisateur utilisateur = getManagedUser(id);

        String previousAvatar = utilisateur.getAvatarUrl();
        utilisateur.setAvatarUrl(avatarUrl);
        utilisateur = utilisateurRepository.save(utilisateur);
        deleteAvatarFileIfNeeded(previousAvatar, avatarUrl);

        return toDto(utilisateur, isUserPresent(utilisateur.getIdUser()));
    }

    @Transactional
    public UserDto removeAvatar(Long id, Long requesterId, boolean isAdmin) {
        assertSelfOrAdmin(id, requesterId, isAdmin, "Vous ne pouvez modifier que votre propre avatar");
        Utilisateur utilisateur = getManagedUser(id);

        String previousAvatar = utilisateur.getAvatarUrl();
        utilisateur.setAvatarUrl(null);
        utilisateur = utilisateurRepository.save(utilisateur);
        deleteAvatarFileIfNeeded(previousAvatar, null);

        return toDto(utilisateur, isUserPresent(utilisateur.getIdUser()));
    }

    @Transactional
    public void deleteUser(Long id, Long requesterId) {
        if (requesterId != null && requesterId.equals(id)) {
            throw new RuntimeException("Vous ne pouvez pas supprimer votre propre compte");
        }
        utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateurRepository.deleteById(id);
    }

    @Transactional
    public UserDto activateUser(Long id, Long requesterId) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateur.setIsActive(true);
        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, false);
    }

    @Transactional
    public UserDto deactivateUser(Long id, Long requesterId) {
        if (requesterId != null && requesterId.equals(id)) {
            throw new RuntimeException("Vous ne pouvez pas désactiver votre propre compte");
        }
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateur.setIsActive(false);
        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, false);
    }

    @Transactional
    public UserDto updateAbsenceMessage(Long id, String message, Long requesterId, boolean isAdmin) {
        if (!isAdmin && (requesterId == null || !requesterId.equals(id))) {
            throw new SecurityException("Vous ne pouvez modifier que votre propre message d'absence");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateur.setMsgAbsence(message);
        utilisateur = utilisateurRepository.save(utilisateur);
        return toDto(utilisateur, false);
    }

    @Transactional
    public UserDto updatePassword(Long id, String currentPassword, String newPassword, Long requesterId, boolean isAdmin) {
        if (!isAdmin && (requesterId == null || !requesterId.equals(id))) {
            throw new SecurityException("Vous ne pouvez modifier que votre propre mot de passe");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));

        if (!isAdmin) {
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new RuntimeException("Mot de passe actuel requis");
            }
            if (!passwordEncoder.matches(currentPassword, utilisateur.getMdp())) {
                throw new RuntimeException("Mot de passe actuel incorrect");
            }
        }

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
                .id(u.getIdUser())
                .idUser(u.getIdUser())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .role(u.getRole())
                .msgAbsence(u.getMsgAbsence())
                .isActive(u.getIsActive())
                .avatarUrl(u.getAvatarUrl())
                .structureId(u.getStructure() != null ? u.getStructure().getIdStructure() : null)
                .structureNom(u.getStructure() != null ? u.getStructure().getNom() : null)
                .structureType(u.getStructure() != null ? u.getStructure().getTypeStructure() : null)
                .isPresent(isPresent)
                .notifyReunions(u.getNotifyReunions())
                .notifyMessages(u.getNotifyMessages())
                .notifyAbsences(u.getNotifyAbsences())
                .build();
    }

    private Utilisateur getManagedUser(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
    }

    private void assertSelfOrAdmin(Long id, Long requesterId, boolean isAdmin, String errorMessage) {
        if (!isAdmin && (requesterId == null || !requesterId.equals(id))) {
            throw new SecurityException(errorMessage);
        }
    }

    private boolean isUserPresent(Long userId) {
        return pointageRepository.findPresentUserIds().contains(userId);
    }

    private void deleteAvatarFileIfNeeded(String previousAvatar, String nextAvatar) {
        if (previousAvatar == null || previousAvatar.isBlank() || previousAvatar.equals(nextAvatar)) {
            return;
        }

        if (!previousAvatar.startsWith("/uploads/")) {
            return;
        }

        try {
            fileService.deleteFile(previousAvatar);
        } catch (RuntimeException ignored) {
            // Avoid failing the profile update when only the old file cleanup fails.
        }
    }

    private Structure resolveStructure(Long structureId) {
        if (structureId == null) {
            return null;
        }

        return structureRepository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));
    }

    private String normalizeRequiredValue(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(errorMessage);
        }
        return value.trim();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("L'email est obligatoire");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String role) {
        String normalized = (role == null || role.isBlank()) ? "Collaborateur" : role.trim();
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new RuntimeException("Rôle invalide");
        }
        return normalized;
    }
}
