package com.coordenapleito.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UsuarioModelBasico {
    private Long id;
    private UUID codigo;
    private String nome;
    private String cpf;
    private OffsetDateTime dataNascimento;
    private ContatoModelBasico contato;
    private String cargo;
    private Boolean ativo;
    private Boolean recebeEmail;
    private List<GrupoModelBasico> grupos = new ArrayList<>();
}