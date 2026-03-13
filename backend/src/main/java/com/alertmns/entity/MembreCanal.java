package com.alertmns.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_membre_canal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembreCanal {

    @EmbeddedId
    private MembreCanalId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUser")
    @JoinColumn(name = "id_user")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idCanal")
    @JoinColumn(name = "id_canal")
    private Canal canal;

    @Column(name = "role", length = 50)
    private String role;
}
