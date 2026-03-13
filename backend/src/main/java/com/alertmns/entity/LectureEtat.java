package com.alertmns.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_lecture_etat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureEtat {

    @EmbeddedId
    private LectureEtatId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUser")
    @JoinColumn(name = "id_user")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idCanal")
    @JoinColumn(name = "id_canal")
    private Canal canal;

    @Column(name = "dernier_id_msg_lu")
    private Long dernierIdMsgLu;
}
