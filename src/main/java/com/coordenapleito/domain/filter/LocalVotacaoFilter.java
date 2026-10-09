package com.coordenapleito.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalVotacaoFilter {
    /** Busca unificada: local, endereço, bairro ou zona. */
    private String busca;
    private Integer zona;
    private String bairro;
}
