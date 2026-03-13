package com.alertmns.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_participant_reunion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ParticipantReunion {

    @EmbeddedId
    private ParticipantReunionId id;

    @JsonIgnoreProperties({"participants", "createur", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idReunion")
    @JoinColumn(name = "id_reunion")
    private Reunion reunion;

    @JsonIgnoreProperties({"mdp", "structure", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUser")
    @JoinColumn(name = "id_user")
    private Utilisateur utilisateur;

    @Column(name = "statut", length = 50)
    private String statut;
}
