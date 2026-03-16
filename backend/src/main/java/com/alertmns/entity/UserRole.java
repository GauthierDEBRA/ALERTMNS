package com.alertmns.entity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rôles possibles pour un utilisateur ALERTMNS.
 * Source unique de vérité — utilisée dans AuthService et UtilisateurService.
 */
public enum UserRole {

    ADMIN("Admin"),
    RH("RH"),
    RESPONSABLE("Responsable"),
    COLLABORATEUR("Collaborateur");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /** Ensemble des valeurs String autorisées (pour validation rapide). */
    public static final Set<String> ALLOWED_VALUES = Arrays.stream(values())
            .map(UserRole::getValue)
            .collect(Collectors.toUnmodifiableSet());

    /** Valeur par défaut pour un nouvel utilisateur. */
    public static final String DEFAULT = COLLABORATEUR.value;

    @Override
    public String toString() {
        return value;
    }
}
