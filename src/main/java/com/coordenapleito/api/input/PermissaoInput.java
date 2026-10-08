package com.coordenapleito.api.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissaoInput {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String descricao;
}
