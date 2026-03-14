package com.alertmns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantReunionDto {

    private Long userId;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String structureNom;
    private String statut;
    private Boolean isOrganizer;
}
