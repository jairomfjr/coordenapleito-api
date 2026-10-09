package com.coordenapleito.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VinculoItemModel {
    private Integer zona;
    private String nome;
    private long capacidade;
    private long vinculados;
    private long vagasDisponiveis;
    private double percentualOcupacao;
}
