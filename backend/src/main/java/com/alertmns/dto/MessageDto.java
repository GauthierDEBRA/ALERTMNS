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

    private Long id;
    private Long idMessage;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateModification;
    private Boolean isDeleted;
    private Long canalId;
    private Long userId;
    private String userNom;
    private String userPrenom;
    private String userEmail;
    private String userAvatarUrl;
    private List<PieceJointeDto> piecesJointes;
    private List<ReactionDto> reactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieceJointeDto {
        private Long idPj;
        private String nomFichier;
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionDto {
        private String emoji;
        private Integer count;
        private List<Long> userIds;
    }
}
