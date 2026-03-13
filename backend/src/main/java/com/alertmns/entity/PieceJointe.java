package com.alertmns.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_piece_jointe")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pj")
    private Long idPj;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_message")
    private Message message;
}
