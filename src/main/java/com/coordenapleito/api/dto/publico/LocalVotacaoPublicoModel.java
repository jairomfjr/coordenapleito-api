package com.coordenapleito.api.dto.publico;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LocalVotacaoPublicoModel {
    private UUID codigo;
    private Integer zona;
    private String localVotacao;
    private String endereco;
    private String bairro;
    private int capacidade;
    private long ocupados;
    private int vagasDisponiveis;
    private boolean esgotado;
}
