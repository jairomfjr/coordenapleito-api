package com.coordenapleito.api.dto.publico;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CoordenadorPublicoModel {
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private UUID localTrabalhoCodigo;
    private UUID localVotacaoCodigo;
}
