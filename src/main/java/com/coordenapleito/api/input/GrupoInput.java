package com.coordenapleito.api.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GrupoInput {

    @NotBlank
    private String nome;

    /** Códigos das permissões a serem vinculadas ao grupo (opcional). */
    private List<UUID> permissoesCodigos;
}