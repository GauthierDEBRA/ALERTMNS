package com.alertmns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private String token;
    private Long userId;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String structureNom;
    private Long structureId;
    private String msgAbsence;
    private Boolean isActive;
    private String avatarUrl;
    private Boolean notifyReunions;
    private Boolean notifyMessages;
    private Boolean notifyAbsences;
}
