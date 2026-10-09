package com.coordenapleito.api.dto.publico;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CoordenadorPublicoModel {
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private UUID localTrabalhoCodigo;
    private UUID localVotacaoCodigo;
}
