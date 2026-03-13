package com.alertmns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    private Long idMessage;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private Long canalId;
    private Long userId;
    private String userNom;
    private String userPrenom;
    private String userEmail;
    private List<PieceJointeDto> piecesJointes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieceJointeDto {
        private Long idPj;
        private String nomFichier;
        private String url;
    }
}
