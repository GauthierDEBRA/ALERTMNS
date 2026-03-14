package com.alertmns.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_utilisateur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @JsonIgnore
    @Column(name = "mdp", nullable = false, length = 255)
    private String mdp;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "msg_absence", length = 255)
    private String msgAbsence;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "notif_reunion", nullable = false)
    @Builder.Default
    private Boolean notifyReunions = true;

    @Column(name = "notif_message", nullable = false)
    @Builder.Default
    private Boolean notifyMessages = true;

    @Column(name = "notif_absence", nullable = false)
    @Builder.Default
    private Boolean notifyAbsences = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_structure")
    private Structure structure;
}
