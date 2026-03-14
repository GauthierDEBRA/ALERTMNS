package com.alertmns.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_canal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Canal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_canal")
    private Long idCanal;

    @Column(name = "nom", nullable = false, unique = true, length = 150)
    private String nom;

    @Column(name = "est_prive")
    @Builder.Default
    private Boolean estPrive = false;

    @Column(name = "type_canal", nullable = false, length = 30)
    @Builder.Default
    private String typeCanal = "canal";

    @Transient
    private Long membresCount;

    @Transient
    private LocalDateTime lastMessageAt;
}
