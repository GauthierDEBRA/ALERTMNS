package com.alertmns.service;

import com.alertmns.dto.LoginRequest;
import com.alertmns.dto.LoginResponse;
import com.alertmns.dto.RegisterRequest;
import com.alertmns.dto.AuthSessionDto;
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

import com.alertmns.entity.UserRole;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final StructureRepository structureRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthSessionDto login(LoginRequest request) {
        authenticate(request);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return buildSession(utilisateur);
    }

    @Transactional
    public AuthSessionDto register(RegisterRequest request) {
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

        return buildSession(utilisateur);
    }

    @Transactional
    public AuthSessionDto refreshSession(String rawRefreshToken) {
        Utilisateur utilisateur = refreshTokenService.consumeToken(rawRefreshToken);
        return buildSession(utilisateur);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeToken(rawRefreshToken);
    }

    private void authenticate(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Authentification impossible");
        }
    }

    private AuthSessionDto buildSession(Utilisateur utilisateur) {
        String token = jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getIdUser(), utilisateur.getRole());
        String refreshToken = refreshTokenService.issueToken(utilisateur);

        return AuthSessionDto.builder()
                .response(LoginResponse.builder()
                        .id(utilisateur.getIdUser())
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
                        .build())
                .refreshToken(refreshToken)
                .build();
    }

    private String normalizeRole(String role) {
        String normalized = (role == null || role.isBlank()) ? UserRole.DEFAULT : role.trim();
        if (!UserRole.ALLOWED_VALUES.contains(normalized)) {
            throw new RuntimeException("Rôle invalide");
        }
        return normalized;
    }
}
