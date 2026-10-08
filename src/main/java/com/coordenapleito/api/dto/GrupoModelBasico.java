package com.coordenapleito.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GrupoModelBasico {
    private Long id;
    private UUID codigo;
    private String nome;
    private List<PermissaoModelBasico> permissoes;
}