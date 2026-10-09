package com.coordenapleito.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CoordenadorModelBasico {
    private Long id;
    private UUID codigo;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private LocalVotacaoModelBasico localTrabalho;
    private LocalVotacaoModelBasico localVotacao;
}
