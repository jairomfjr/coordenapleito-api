package com.coordenapleito.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioFilter {
    /** Campo único da barra de busca (nome, CPF parcial e/ou e-mail); comparação sem distinção de maiúsculas. */
    private String busca;
    private String nome;
    private String cpf;
    private String email;
    private Boolean ativo;
}