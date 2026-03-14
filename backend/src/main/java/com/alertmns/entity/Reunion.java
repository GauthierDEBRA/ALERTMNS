package com.alertmns.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "t_reunion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reunion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reunion")
    private Long idReunion;

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_prevue")
    private LocalDateTime datePrevue;

    @Column(name = "duree_minutes", nullable = false)
    @Builder.Default
    private Integer dureeMinutes = 60;

    @Column(name = "lieu", length = 255)
    private String lieu;

    @Column(name = "lien_visio", length = 500)
    private String lienVisio;

    @Column(name = "rappel_j1_envoye_at")
    private LocalDateTime rappelJ1EnvoyeAt;

    @Column(name = "rappel_1h_envoye_at")
    private LocalDateTime rappel1hEnvoyeAt;

    @Column(name = "rappel_30m_envoye_at")
    private LocalDateTime rappel30mEnvoyeAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_createur")
    private Utilisateur createur;

    @JsonIgnore
    @OneToMany(mappedBy = "reunion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParticipantReunion> participants;
}
