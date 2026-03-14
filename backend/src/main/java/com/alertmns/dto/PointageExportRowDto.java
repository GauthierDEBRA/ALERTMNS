package com.alertmns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointageExportRowDto {

    private Long pointageId;
    private Long userId;
    private String prenom;
    private String nom;
    private String email;
    private String role;
    private String structureNom;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Long dureeMinutes;
    private String dureeFormatted;
    private Boolean enCours;
}
