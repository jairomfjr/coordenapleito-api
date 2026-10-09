package com.coordenapleito.api.dto.publico;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoordenadorCpfConsultaModel {
    private boolean existe;
    private String contatoMascarado;
    private String mensagem;
}
