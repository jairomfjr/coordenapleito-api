package com.coordenapleito.api.dto.publico;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoordenadorVerificacaoModel {
    private String tokenAtualizacao;
    private CoordenadorPublicoModel cadastro;
}
