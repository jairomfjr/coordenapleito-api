package com.coordenapleito.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LocalVotacaoModelBasico {
    private Long id;
    private UUID codigo;
    private Integer zona;
    private String localVotacao;
    private String endereco;
    private String bairro;
    private Integer qtdSecoes;
    private Integer qtdEleitores;
    private Integer qtdCoordenadores;
}
