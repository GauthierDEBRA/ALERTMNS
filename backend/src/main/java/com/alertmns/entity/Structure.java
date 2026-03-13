package com.alertmns.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_structure")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_structure")
    private Long idStructure;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "type_structure", length = 50)
    private String typeStructure;
}
