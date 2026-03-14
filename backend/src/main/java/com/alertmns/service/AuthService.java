package com.alertmns.service;

import com.alertmns.dto.LoginRequest;
import com.alertmns.dto.LoginResponse;
import com.alertmns.dto.RegisterRequest;
import com.alertmns.entity.Structure;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.StructureRepository;
import com.alertmns.repository.UtilisateurRepository;
import com.alertmns.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<String> ALLOWED_ROLES = Set.of("Admin", "RH", "Responsable", "Collaborateur");

    private final UtilisateurRepository utilisateurRepository;
    private final StructureRepository structureRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String token = jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getIdUser(), utilisateur.getRole());

        return LoginResponse.builder()
                .token(token)
                .userId(utilisateur.getIdUser())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .structureId(utilisateur.getStructure() != null ? utilisateur.getStructure().getIdStructure() : null)
                .structureNom(utilisateur.getStructure() != null ? utilisateur.getStructure().getNom() : null)
                .msgAbsence(utilisateur.getMsgAbsence())
                .isActive(utilisateur.getIsActive())
                .avatarUrl(utilisateur.getAvatarUrl())
                .notifyReunions(utilisateur.getNotifyReunions())
                .notifyMessages(utilisateur.getNotifyMessages())
                .notifyAbsences(utilisateur.getNotifyAbsences())
                .build();
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        Structure structure = null;
        if (request.getStructureId() != null) {
            structure = structureRepository.findById(request.getStructureId())
                    .orElseThrow(() -> new RuntimeException("Structure non trouvée"));
        }

        String role = normalizeRole(request.getRole());

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .mdp(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(true)
                .structure(structure)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        String token = jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getIdUser(), utilisateur.getRole());

        return LoginResponse.builder()
                .token(token)
                .userId(utilisateur.getIdUser())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .structureId(structure != null ? structure.getIdStructure() : null)
                .structureNom(structure != null ? structure.getNom() : null)
                .isActive(true)
                .avatarUrl(utilisateur.getAvatarUrl())
                .notifyReunions(utilisateur.getNotifyReunions())
                .notifyMessages(utilisateur.getNotifyMessages())
                .notifyAbsences(utilisateur.getNotifyAbsences())
                .build();
    }

    private String normalizeRole(String role) {
        String normalized = (role == null || role.isBlank()) ? "Collaborateur" : role.trim();
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new RuntimeException("Rôle invalide");
        }
        return normalized;
    }
}
