package com.alertmns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long idUser;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String msgAbsence;
    private Boolean isActive;
    private String avatarUrl;
    private Long structureId;
    private String structureNom;
    private String structureType;
    private Boolean isPresent;
    private Boolean notifyReunions;
    private Boolean notifyMessages;
    private Boolean notifyAbsences;
}
