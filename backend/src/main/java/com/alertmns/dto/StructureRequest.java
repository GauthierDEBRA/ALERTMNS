package com.alertmns.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StructureRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String typeStructure;
}
