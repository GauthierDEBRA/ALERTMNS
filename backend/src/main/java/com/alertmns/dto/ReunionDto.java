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
public class ReunionDto {

    private Long id;
    private String titre;
    private String description;
    private LocalDateTime datePrevue;
    private Integer dureeMinutes;
    private String lieu;
    private String lienVisio;
    private Long createurId;
    private UserSummaryDto organisateur;
    private String myStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDto {
        private Long id;
        private String nom;
        private String prenom;
    }
}
