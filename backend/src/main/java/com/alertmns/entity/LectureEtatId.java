package com.alertmns.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LectureEtatId implements Serializable {

    @Column(name = "id_user")
    private Long idUser;

    @Column(name = "id_canal")
    private Long idCanal;
}
