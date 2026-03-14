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
public class MessageSearchResultDto {

    private Long idMessage;
    private Long canalId;
    private String conversationName;
    private String typeCanal;
    private String extrait;
    private LocalDateTime dateEnvoi;
    private Long userId;
    private String userNom;
    private String userPrenom;
    private String userAvatarUrl;
}
