package com.alertmns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CanalDto {
    private Long id;
    private Long idCanal;
    private String nom;
    private Boolean estPrive;
    private String typeCanal;
    private Long membresCount;
    private LocalDateTime lastMessageAt;
    private Long directUserId;
    private String directUserNom;
    private String directUserPrenom;
    private String directUserEmail;
    private String directUserAvatarUrl;
}
