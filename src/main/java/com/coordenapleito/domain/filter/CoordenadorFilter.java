package com.coordenapleito.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoordenadorFilter {
    /** Busca unificada: nome, CPF, e-mail, telefone ou local. */
    private String busca;
}
