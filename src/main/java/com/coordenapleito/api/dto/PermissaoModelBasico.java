package com.coordenapleito.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PermissaoModelBasico {
    private Long id;
    private UUID codigo;
    private String nome;
    private String descricao;
    private String chave;
    private String modulo;
    private String recurso;
    private String acao;
}