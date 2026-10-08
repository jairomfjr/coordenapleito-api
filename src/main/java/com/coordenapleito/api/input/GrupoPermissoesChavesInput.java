package com.coordenapleito.api.input;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GrupoPermissoesChavesInput {
    @NotNull
    private List<String> permissoes;
}
